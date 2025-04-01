package backend.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "events")
public class EventModel {
    @Id
    private String id;
    private String adminId; // Links to the admin user who created the event
    private String title; // Event title (e.g., "Architectural Design Webinar")
    private String description; // Event description
    private LocalDateTime startTime; // Start date and time
    private LocalDateTime endTime; // End date and time (optional)
    private String zoomLink; // Zoom meeting link (optional)
    private String category; // e.g., "Workshop", "Meeting", "Seminar"

    // Default constructor
    public EventModel() {
    }

    // Parameterized constructor
    public EventModel(String adminId, String title, String description, LocalDateTime startTime,
                      LocalDateTime endTime, String zoomLink, String category) {
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.zoomLink = zoomLink;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getZoomLink() { return zoomLink; }
    public void setZoomLink(String zoomLink) { this.zoomLink = zoomLink; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}