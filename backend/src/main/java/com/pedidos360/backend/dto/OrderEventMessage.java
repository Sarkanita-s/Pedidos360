package com.pedidos360.backend.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OrderEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String eventType;
    private String status;
    private String ownerOid;
    private LocalDateTime timestamp;
    private String details;

    public OrderEventMessage() {
    }

    public OrderEventMessage(Long orderId, String eventType, String status, String ownerOid, String details) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.status = status;
        this.ownerOid = ownerOid;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerOid() {
        return ownerOid;
    }

    public void setOwnerOid(String ownerOid) {
        this.ownerOid = ownerOid;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "OrderEventMessage{" +
                "orderId=" + orderId +
                ", eventType='" + eventType + '\'' +
                ", status='" + status + '\'' +
                ", ownerOid='" + ownerOid + '\'' +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }
}
