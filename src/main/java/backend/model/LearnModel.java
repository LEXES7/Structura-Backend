package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "learns")
public class LearnModel {
    @Id
    private String id;
    private String userId; // Links to User.id
    private String learnImg = "default.png";
    private String learnName;
    private String learnCategory;
    private String learnDescription;

    // Default constructor
    public LearnModel() {
    }

    // Parameterized constructor
    public LearnModel(String userId, String learnImg, String learnName, String learnCategory, String learnDescription) {
        this.userId = userId;
        this.learnImg = learnImg != null ? learnImg : "default.png";
        this.learnName =learnName;
        this.learnCategory = learnCategory;
        this.learnDescription = learnDescription;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLearnImg() { return learnImg; }
    public void setLearnImg(String learnImg) { this.learnImg = learnImg != null ? learnImg : "default.png"; }
    public String getLearnName() { return learnName; }
    public void setLearnName(String learnName) { this.learnName = learnName; }
    public String getLearnCategory() { return learnCategory; }
    public void setLearnCategory(String learnCategory) { this.learnCategory = learnCategory; }
    public String getLearnDescription() { return learnDescription; }
    public void setLearnDescription(String learnDescription) { this.learnDescription = learnDescription; }
}