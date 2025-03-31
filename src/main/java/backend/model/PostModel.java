package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "posts")
public class PostModel {
    @Id
    private String id;
    private String userId; // Links to User.id
    private String postImg = "default.png";
    private String postName;
    private String postCategory;
    private String postDescription;

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
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPostImg() { return postImg; }
    public void setPostImg(String postImg) { this.postImg = postImg != null ? postImg : "default.png"; }
    public String getPostName() { return postName; }
    public void setPostName(String postName) { this.postName = postName; }
    public String getPostCategory() { return postCategory; }
    public void setPostCategory(String postCategory) { this.postCategory = postCategory; }
    public String getPostDescription() { return postDescription; }
    public void setPostDescription(String postDescription) { this.postDescription = postDescription; }
}