package backend.repository;

import backend.model.EventModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends MongoRepository<EventModel, String> {
    List<EventModel> findByAdminId(String adminId);
    List<EventModel> findByStartTimeAfter(LocalDateTime dateTime); // For upcoming events
}
