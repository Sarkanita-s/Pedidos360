package com.pedidos360.backend.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderKafkaEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private Long orderId;
    private String eventType;
    private String status;
    private String customerName;
    private Double total;
    private String ownerOid;
    private LocalDateTime timestamp;
    private String details;

    public OrderKafkaEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public OrderKafkaEvent(Long orderId, String eventType, String status, String customerName, Double total, String ownerOid, String details) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.eventType = eventType;
        this.status = status;
        this.customerName = customerName;
        this.total = total;
        this.ownerOid = ownerOid;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
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
        return "OrderKafkaEvent{" +
                "eventId='" + eventId + '\'' +
                ", orderId=" + orderId +
                ", eventType='" + eventType + '\'' +
                ", status='" + status + '\'' +
                ", customerName='" + customerName + '\'' +
                ", total=" + total +
                ", ownerOid='" + ownerOid + '\'' +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }
}
