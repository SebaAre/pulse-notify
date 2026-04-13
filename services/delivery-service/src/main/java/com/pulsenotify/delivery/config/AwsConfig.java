package com.pulsenotify.delivery.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.SesClientBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

@Configuration
public class AwsConfig {
    
    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint-override:}")
    private String endpointOverride;

    @Bean
    public SnsClient snsClient() {
        SnsClientBuilder builder = SnsClient.builder()
            .region(Region.of(region));
        
        if (!endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
            .region(Region.of(region));

        if (!endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride)); 
        }
    
        return builder.build();
    }

    @Bean
    public SesClient sesClient() {
        SesClientBuilder builder = SesClient.builder()
            .region(Region.of(region));

        if (!endpointOverride.isEmpty()) {
            builder.endpointOverride(URI.create(endpointOverride)); 
        }
    
        return builder.build();
    }
    

}
