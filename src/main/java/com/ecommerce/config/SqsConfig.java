package com.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SqsConfig {

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient, ObjectMapper objectMapper) {
        return SqsTemplate.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configureDefaultConverter(converter -> {
                    converter.setObjectMapper(objectMapper);
                })
                .build();
    }

    /**
     * LocalStack SQS client — used only when 'local' profile is active.
     * Overrides the auto-configured SqsAsyncClient with a LocalStack endpoint.
     */
    @Bean
    @Profile("local")
    public SqsAsyncClient localSqsAsyncClient(
            @Value("${spring.cloud.aws.sqs.endpoint}") String sqsEndpoint,
            @Value("${spring.cloud.aws.region.static}") String region) {
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(sqsEndpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }
}
