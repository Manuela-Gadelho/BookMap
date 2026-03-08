package com.bookmap.app.model;

/**
 * Report model for flagging inappropriate users or content.
 * Status: PENDING, REVIEWED, RESOLVED
 */
public class Report {
    private long id;
    private long reporterId;
    private long reportedUserId;
    private long reportedContentId;
    private String contentType;
    private String reason;
    private String status;
    private String createdAt;

    public Report() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getReporterId() { return reporterId; }
    public void setReporterId(long reporterId) { this.reporterId = reporterId; }

    public long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(long reportedUserId) { this.reportedUserId = reportedUserId; }

    public long getReportedContentId() { return reportedContentId; }
    public void setReportedContentId(long reportedContentId) { this.reportedContentId = reportedContentId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
