package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "posts")
public class PostModel {
    @Id
    private String id;
    private String userId;
    private String postImg = "default.png";
    private String postName;
    private String postCategory;
    private String postDescription;
    private List<String> likedBy = new ArrayList<>();
    private int shareCount = 0;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();

    // Default constructor
    public PostModel() {
    }

    // Parameterized constructor
    public PostModel(String userId, String postImg, String postName, String postCategory, String postDescription) {
        this.userId = userId;
        this.postImg = postImg != null ? postImg : "default.png";
        this.postName = postName;
        this.postCategory = postCategory;
        this.postDescription = postDescription;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg != null ? postImg : "default.png";
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getPostCategory() {
        return postCategory;
    }

    public void setPostCategory(String postCategory) {
        this.postCategory = postCategory;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    // New getters and setters for likes and shares
    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods for likes
    public boolean isLikedBy(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }

    public void addLike(String userId) {
        if (likedBy == null) {
            likedBy = new ArrayList<>();
        }
        if (!likedBy.contains(userId)) {
            likedBy.add(userId);
        }
    }

    public void removeLike(String userId) {
        if (likedBy != null) {
            likedBy.remove(userId);
        }
    }

    public int getLikeCount() {
        return likedBy == null ? 0 : likedBy.size();
    }

    public void incrementShareCount() {
        shareCount++;
    }
}