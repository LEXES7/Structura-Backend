package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "comments")
public class CommentModel {
    @Id
    private String id;
    private String postId;
    private String userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public CommentModel() {
        this.createdAt = LocalDateTime.now();
    }

    public CommentModel(String postId, String userId, String username, String content) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}