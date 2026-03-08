package com.bookmap.app.model;

/**
 * Club model representing a reading club.
 */
public class Club {
    private long id;
    private String name;
    private String description;
    private boolean isPublic;
    private long creatorId;
    private String bannerPath;
    private String createdAt;

    // Transient fields
    private int memberCount;
    private String creatorName;

    public Club() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public long getCreatorId() { return creatorId; }
    public void setCreatorId(long creatorId) { this.creatorId = creatorId; }

    public String getBannerPath() { return bannerPath; }
    public void setBannerPath(String bannerPath) { this.bannerPath = bannerPath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
