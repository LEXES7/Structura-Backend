package backend.repository;

import backend.model.CourseModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<CourseModel, String> {
    List<CourseModel> findByUserId(String userId);
}