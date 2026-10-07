package com.pixora.model;

import java.time.LocalDateTime;

public class ComplaintAction {
    private int actionId;
    private int complaintId;
    private int actorUserId;
    private String actorName;
    private String actionText;
    private LocalDateTime createdAt;

    public ComplaintAction() {}

    public int getActionId() { return actionId; }
    public void setActionId(int actionId) { this.actionId = actionId; }
    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }
    public int getActorUserId() { return actorUserId; }
    public void setActorUserId(int actorUserId) { this.actorUserId = actorUserId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActionText() { return actionText; }
    public void setActionText(String actionText) { this.actionText = actionText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}