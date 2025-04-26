package backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class CourseModel {
    @Id
    private String id;
    private String userId; // Links to User.id
    private String courseName;
    private String coursePdf;
    private String courseVideo;

    // Default constructor
    public CourseModel() {
    }

    // Parameterized constructor
    public CourseModel(String userId, String courseName, String coursePdf, String courseVideo) {
        this.userId = userId;
        this.courseName = courseName;
        this.coursePdf = coursePdf;
        this.courseVideo = courseVideo;
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCoursePdf() {
        return coursePdf;
    }

    public void setCoursePdf(String coursePdf) {
        this.coursePdf = coursePdf;
    }

    public String getCourseVideo() {
        return courseVideo;
    }

    public void setCourseVideo(String courseVideo) {
        this.courseVideo = courseVideo;
    }
}