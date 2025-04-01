package backend.service;


import backend.model.EventModel;
import backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class EventService {
    private static final Logger LOGGER = Logger.getLogger(EventService.class.getName());

    @Autowired
    private EventRepository eventRepository;

    // Create an event (admin only)
    public EventModel createEvent(String adminId, String title, String description, LocalDateTime startTime,
                                  LocalDateTime endTime, String zoomLink, String category) {
        LOGGER.info("Creating event for adminId: " + adminId);
        EventModel event = new EventModel(adminId, title, description, startTime, endTime, zoomLink, category);
        return eventRepository.save(event);
    }

    // Update an event (admin only)
    public EventModel updateEvent(String eventId, String adminId, String title, String description,
                                  LocalDateTime startTime, LocalDateTime endTime, String zoomLink, String category) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (!event.getAdminId().equals(adminId)) {
            throw new RuntimeException("Unauthorized to update this event");
        }
        event.setTitle(title);
        event.setDescription(description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setZoomLink(zoomLink);
        event.setCategory(category);
        return eventRepository.save(event);
    }

    // Delete an event (admin only)
    public void deleteEvent(String eventId, String adminId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (!event.getAdminId().equals(adminId)) {
            throw new RuntimeException("Unauthorized to delete this event");
        }
        eventRepository.delete(event);
    }

    // Get all events (for users' calendar display)
    public List<EventModel> getAllEvents() {
        LOGGER.info("Fetching all events");
        return eventRepository.findAll();
    }

    // Get upcoming events (for users' calendar)
    public List<EventModel> getUpcomingEvents() {
        LOGGER.info("Fetching upcoming events");
        return eventRepository.findByStartTimeAfter(LocalDateTime.now());
    }

    // Get events by adminId (for admin dashboard)
    public List<EventModel> getEventsByAdminId(String adminId) {
        LOGGER.info("Fetching events for adminId: " + adminId);
        return eventRepository.findByAdminId(adminId);
    }
}