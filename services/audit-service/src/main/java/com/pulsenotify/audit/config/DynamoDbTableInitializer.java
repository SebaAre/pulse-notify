package com.pulsenotify.audit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.pulsenotify.audit.model.AuditEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.dynamodb.auto-create-table", havingValue = "true")
public class DynamoDbTableInitializer {

    private final DynamoDbTable<AuditEvent> auditEventTable;

    @EventListener(ApplicationReadyEvent.class)
    public void createTableIfMissing() {
        try {
            auditEventTable.createTable();
            log.info("created DynamoDB table {}", auditEventTable.tableName());
        } catch (ResourceInUseException ex) {
            log.debug("DynamoDB table {} already exists", auditEventTable.tableName());
        }
    }
}