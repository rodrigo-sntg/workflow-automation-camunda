import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as sns from 'aws-cdk-lib/aws-sns';
import * as sqs from 'aws-cdk-lib/aws-sqs';
import * as subscriptions from 'aws-cdk-lib/aws-sns-subscriptions';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as path from 'path';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as lambdaEventSources from 'aws-cdk-lib/aws-lambda-event-sources';
import * as cloudwatch from 'aws-cdk-lib/aws-cloudwatch';

export class MonorepoStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // VPC
    const vpc = new ec2.Vpc(this, 'VPC', {
      maxAzs: 2
    });

    // Security Group
    const securityGroup = new ec2.SecurityGroup(this, 'SecurityGroup', {
      vpc,
      description: 'Allow ssh and http',
      allowAllOutbound: true
    });
    securityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(22), 'allow ssh access from anywhere');
    securityGroup.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.tcp(80), 'allow http access from anywhere');

    // IAM Role
    const role = new iam.Role(this, 'InstanceSSMRole', {
      assumedBy: new iam.ServicePrincipal('ec2.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonSSMManagedInstanceCore'),
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonEC2ContainerRegistryReadOnly')
      ]
    });
    role.addToPolicy(new iam.PolicyStatement({
      actions: ['sns:Publish', 'ssm:GetParameter'],
      resources: ['*'], // Você pode restringir isso ao ARN específico do tópico SNS, se preferir
    }));

    const notificationTopic = new sns.Topic(this, 'NotificationTopic');

    const emailQueueDLQ = new sqs.Queue(this, 'EmailQueueDLQ', {
      retentionPeriod: cdk.Duration.days(14)
    });

    const emailQueue = new sqs.Queue(this, 'EmailQueue', {
      deadLetterQueue: {
        queue: emailQueueDLQ,
        maxReceiveCount: 5
      }
    });

    notificationTopic.addSubscription(new subscriptions.SqsSubscription(emailQueue));

    // Lambda Function
    const emailHandlerLogGroup = new logs.LogGroup(this, 'EmailHandlerLogGroup', {
      logGroupName: '/aws/lambda/MonorepoStack-EmailHandler',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      retention: logs.RetentionDays.ONE_WEEK,
    });

    const emailHandler = new lambda.Function(this, 'EmailHandler', {
      runtime: lambda.Runtime.PYTHON_3_9,
      handler: 'email_handler.lambda_handler',
      code: lambda.Code.fromAsset(path.join(__dirname, 'lambda')),
      environment: {
        QUEUE_URL: emailQueue.queueUrl,
        SNS_TOPIC_ARN: notificationTopic.topicArn,
      },
      logRetention: logs.RetentionDays.ONE_WEEK,  // Ensure the logs are retained for a week
    });
    // Adicionar permissão para acessar Secrets Manager
    emailHandler.addToRolePolicy(new iam.PolicyStatement({
      actions: ['secretsmanager:GetSecretValue'],
      resources: ['*'],
    }));

    // Adiciona a fila SQS como uma fonte de eventos para a função Lambda
    emailHandler.addEventSource(new lambdaEventSources.SqsEventSource(emailQueue));

    // Permissões para a Lambda acessar o SQS e publicar no SNS
    emailQueue.grantConsumeMessages(emailHandler);
    notificationTopic.grantPublish(emailHandler);

    // EC2 Instance
    const instance = new ec2.Instance(this, 'Instance', {
      vpc,
      instanceType: ec2.InstanceType.of(ec2.InstanceClass.T2, ec2.InstanceSize.MICRO),
      machineImage: ec2.MachineImage.latestAmazonLinux(),
      securityGroup,
      vpcSubnets: { subnetType: ec2.SubnetType.PUBLIC },
      role,
      keyName: 'teste' // substitua pelo nome do seu par de chaves
    });

    // Atribuir um IP público
    const eip = new ec2.CfnEIP(this, 'InstanceEIP');
    new ec2.CfnEIPAssociation(this, 'InstanceEIPAssociation', {
      eip: eip.ref,
      instanceId: instance.instanceId
    });

    // User Data Script
    instance.addUserData(
      `#!/bin/bash`,
      `yum update -y`,
      `yum install -y docker`,
      `curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose`,
      `chmod +x /usr/local/bin/docker-compose`,
      `export PATH=$PATH:/usr/local/bin`,
      `service docker start`,
      `usermod -a -G docker ec2-user`,
      `ACCESS_KEY_AWS=$(aws ssm get-parameter --name /workflow_cdk/ACCESS_KEY_AWS --with-decryption --query Parameter.Value --output text --region us-east-1)`,
      `SECRET_KEY_AWS=$(aws ssm get-parameter --name /workflow_cdk/SECRET_KEY_AWS --with-decryption --query Parameter.Value --output text --region us-east-1)`,
      `cat <<EOF > docker-compose.yml`,
      `version: '3.8'`,
      `services:`,
      `  quotation:`,
      `    image: rodrigosntg/quotation:latest`,
      `    container_name: quotation`,
      `    ports:`,
      `      - "8085:8085"`,
      `    networks:`,
      `      - workflow-net`,
      `  workflow:`,
      `    image: rodrigosntg/workflow:latest`,
      `    container_name: workflow`,
      `    ports:`,
      `      - "8081:8081"`,
      `    environment:`,
      `      - SPRING_PROFILES_ACTIVE=development`,
      `      - QUOTATION_SERVICE_URL=http://quotation:8085/api/travel-requests`,
      `      - ACCESS_KEY_AWS=$ACCESS_KEY_AWS`,
      `      - SECRET_KEY_AWS=$SECRET_KEY_AWS`,
      `      - SNS_TOPIC_ARN=${notificationTopic.topicArn}`,
      `    networks:`,
      `      - workflow-net`,
      `    depends_on:`,
      `      - quotation`,
      `networks:`,
      `  workflow-net:`,
      `    driver: bridge`,
      `EOF`,
      `docker-compose -p workflow_project up -d`
    );

    // Dashboard do CloudWatch
    const dashboard = new cloudwatch.Dashboard(this, 'TravelRequestWorkflowDashboard', {
      dashboardName: 'TravelRequestWorkflowDashboard',
    });

    // Widgets do Task Controller
    dashboard.addWidgets(
      new cloudwatch.TextWidget({
        markdown: '## Métricas do Controlador de Tarefas',
        width: 24
      }),
      new cloudwatch.GraphWidget({
        title: 'Solicitações de Tarefas',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'tasks.requests.count',
          dimensionsMap: { "type": "get" },
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Erros de Tarefas',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'tasks.errors.count',
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Latência de Tarefas',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'tasks.latency.avg',
          statistic: 'avg',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Disponibilidade de Tarefas',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'tasks.availability.avg',
          statistic: 'avg',
        })]
      })
    );

    // Widgets do Controlador de Processos
    dashboard.addWidgets(
      new cloudwatch.TextWidget({
        markdown: '## Métricas do Controlador de Processos',
        width: 24
      }),
      new cloudwatch.GraphWidget({
        title: 'Solicitações de Processos',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'processes.requests.count',
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Erros de Processos',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'processes.errors.count',
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Latência de Processos',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'processes.latency.avg',
          statistic: 'avg',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Disponibilidade de Processos',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'processes.availability.avg',
          statistic: 'avg',
        })]
      })
    );

    // Widgets do Travel Request Controller
    dashboard.addWidgets(
      new cloudwatch.TextWidget({
        markdown: '## Métricas do Controlador de Solicitações de Viagem',
        width: 24
      }),
      new cloudwatch.GraphWidget({
        title: 'Criação de Solicitações de Viagem',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'travelRequests.create.count',
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Erros de Solicitações de Viagem',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'travelRequests.errors.count',
          statistic: 'sum',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Latência de Solicitações de Viagem',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'travelRequests.latency.avg',
          statistic: 'avg',
        })]
      }),
      new cloudwatch.GraphWidget({
        title: 'Disponibilidade de Solicitações de Viagem',
        left: [new cloudwatch.Metric({
          namespace: 'TravelRequestWorkflowMetrics',
          metricName: 'travelRequests.availability.avg',
          statistic: 'avg',
        })]
      })
    );

  }
}

const app = new cdk.App();
new MonorepoStack(app, 'MonorepoStack', {
  env: { region: 'us-east-1' },
});
