import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import json
import boto3

def get_mailtrap_credentials():
    secret_name = "mailtrap/credentials"
    region_name = "us-east-1"

    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        region_name=region_name
    )

    try:
        get_secret_value_response = client.get_secret_value(SecretId=secret_name)
    except Exception as e:
        print(f"Error retrieving secret {secret_name}: {str(e)}")
        raise e

    secret = get_secret_value_response['SecretString']
    return json.loads(secret)

def lambda_handler(event, context):
    # Recuperando as credenciais do Mailtrap do Secrets Manager
    credentials = get_mailtrap_credentials()
    mailtrap_host = 'sandbox.smtp.mailtrap.io'
    mailtrap_port = 465
    mailtrap_username = credentials['username']
    mailtrap_password = credentials['password']

    for record in event['Records']:
        # Deserializa a mensagem JSON da fila SQS
        sns_message = json.loads(record['body'])
        notificationContext = json.loads(sns_message['Message'])
        print("Processando registro:", notificationContext)

        # Criando o objeto de mensagem
        message = MIMEMultipart()
        message['From'] = 'sistema@sistema.com'
        message['To'] = notificationContext['recipient']
        message['Subject'] = notificationContext['subject']
        body = notificationContext['body']
        message.attach(MIMEText(body, 'plain'))

        # Iniciar a conexão SMTP em modo seguro
        try:
            with smtplib.SMTP(mailtrap_host, mailtrap_port) as server:
                server.ehlo()
                server.starttls()
                server.login(mailtrap_username, mailtrap_password)
                text = message.as_string()
                server.sendmail(message['From'], message['To'], text)
                print("Email enviado para", notificationContext['recipient'])
        except Exception as e:
            print("Falha ao enviar email:", e)
            return {'statusCode': 500, 'body': json.dumps('Falha ao enviar email')}

    return {'statusCode': 200, 'body': json.dumps('Email enviado com sucesso')}