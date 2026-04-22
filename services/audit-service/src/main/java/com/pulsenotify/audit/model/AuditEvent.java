package com.pulsenotify.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String notificationId;

    @Getter(onMethod_ = @DynamoDbSortKey)
    private String timestamp;

    @Getter
    private String eventType;

    @Getter
    private String channel;

    @Getter
    private String recipient;

    @Getter
    private Integer attemptNumber;

    @Getter
    private String errorCode;

    @Getter
    private String errorMessage;

    @Getter
    private String providerMessageId;

}