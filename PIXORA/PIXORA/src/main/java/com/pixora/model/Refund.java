package com.pixora.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {
    private int refundId;
    private String refundRef;
    private int paymentId;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Integer processedByUserId;
    private LocalDateTime createdAt;

    public Refund() {}

    public int getRefundId() { return refundId; }
    public void setRefundId(int refundId) { this.refundId = refundId; }
    public String getRefundRef() { return refundRef; }
    public void setRefundRef(String refundRef) { this.refundRef = refundRef; }
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(Integer processedByUserId) { this.processedByUserId = processedByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}