package com.pulsenotify.audit.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.pulsenotify.audit.model.AuditEvent;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
@RequiredArgsConstructor
public class AuditEventRepository {

    private final DynamoDbTable<AuditEvent> auditEventTable;

    public void save(AuditEvent event) {
        auditEventTable.putItem(event);
    }

    public List<AuditEvent> findByNotificationId(String notificationId) {
        return auditEventTable
            .query(QueryConditional.keyEqualTo(
                Key.builder()
                   .partitionValue(notificationId)
                   .build()))
            .items()
            .stream()
            .toList();
    }
}