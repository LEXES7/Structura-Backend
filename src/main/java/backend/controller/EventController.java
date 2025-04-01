package backend.controller;

import backend.model.EventModel;
import backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private static final Logger LOGGER = Logger.getLogger(EventController.class.getName());

    @Autowired
    private EventService eventService;

    // Create event (admin only)
    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startTime") String startTime, // Expect ISO format, e.g., "2025-04-10T10:00:00"
            @RequestParam(value = "endTime", required = false) String endTime, // Optional
            @RequestParam(value = "zoomLink", required = false) String zoomLink,
            @RequestParam("category") String category) {
        try {
            String adminId = getCurrentUserId();
            LOGGER.info("Received POST /api/events for adminId: " + adminId);
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = (endTime != null) ? LocalDateTime.parse(endTime) : null;
            EventModel event = eventService.createEvent(adminId, title, description, start, end, zoomLink, category);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            LOGGER.severe("Error creating event: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error creating event: " + e.getMessage()));
        }
    }

    // Update event (admin only)
    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String eventId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("startTime") String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "zoomLink", required = false) String zoomLink,
            @RequestParam("category") String category) {
        try {
            String adminId = getCurrentUserId();
            LOGGER.info("Received PUT /api/events/" + eventId + " for adminId: " + adminId);
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = (endTime != null) ? LocalDateTime.parse(endTime) : null;
            EventModel updatedEvent = eventService.updateEvent(eventId, adminId, title, description, start, end, zoomLink, category);
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            LOGGER.severe("Error updating event: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error updating event: " + e.getMessage()));
        }
    }

    // Delete event (admin only)
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable String eventId) {
        try {
            String adminId = getCurrentUserId();
            LOGGER.info("Received DELETE /api/events/" + eventId + " for adminId: " + adminId);
            eventService.deleteEvent(eventId, adminId);
            return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
        } catch (Exception e) {
            LOGGER.severe("Error deleting event: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting event: " + e.getMessage()));
        }
    }

    // Get all events (for user dashboard calendar)
    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            LOGGER.info("Received GET /api/events");
            List<EventModel> events = eventService.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            LOGGER.severe("Error fetching all events: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching events: " + e.getMessage()));
        }
    }

    // Get upcoming events (for user dashboard calendar)
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents() {
        try {
            LOGGER.info("Received GET /api/events/upcoming");
            List<EventModel> upcomingEvents = eventService.getUpcomingEvents();
            return ResponseEntity.ok(upcomingEvents);
        } catch (Exception e) {
            LOGGER.severe("Error fetching upcoming events: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching upcoming events: " + e.getMessage()));
        }
    }

    // Get events by admin (for admin dashboard)
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminEvents() {
        try {
            String adminId = getCurrentUserId();
            LOGGER.info("Received GET /api/events/admin for adminId: " + adminId);
            List<EventModel> events = eventService.getEventsByAdminId(adminId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            LOGGER.severe("Error fetching admin events: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching admin events: " + e.getMessage()));
        }
    }

    // Helper method to get the current authenticated user's ID
    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // Assuming username is the userId
        } else {
            return principal.toString();
        }
    }
}