package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "posts")
public class PostModel {
    @Id
    private String id;
    private String postImg = "default.png"; // Default value
    private String postName;
    private String postCategory;
    private String postDescription;

    // Default constructor
    public PostModel() {
    }

    // Parameterized constructor
    public PostModel(String postImg, String postName, String postCategory, String postDescription) {
        this.postImg = postImg != null ? postImg : "default.png"; // Ensure default if null
        this.postName = postName;
        this.postCategory = postCategory;
        this.postDescription = postDescription;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
