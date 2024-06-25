package com.ntconsult.workflow.configs;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

@Configuration
public class MetricsConfig {

    @Value("${cloudwatch.accessKey}")
    private String awsAccessKey;

    @Value("${cloudwatch.secretKey}")
    private String awsSecretKey;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer() {
        return registry -> registry.config().commonTags("application", "workflow-service");
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey))
                )
                .build();
    }

    @Bean
    public MeterRegistry getMeterRegistry(CloudWatchAsyncClient cloudWatchAsyncClient) {
        CloudWatchConfig cloudWatchConfig = setupCloudWatchConfig();

        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, cloudWatchAsyncClient);
    }

    private CloudWatchConfig setupCloudWatchConfig() {
        return new CloudWatchConfig() {
            private final Map<String, String> configuration = Map.of(
                    "cloudwatch.namespace", "TravelRequestWorkflowMetrics",
                    "cloudwatch.step", Duration.ofMinutes(1).toString()
            );

            @Override
            public String get(String key) {
                return configuration.get(key);
            }
        };
    }
}
