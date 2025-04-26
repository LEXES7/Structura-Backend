package backend.repository;

import backend.model.LearnModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LearnRepository extends MongoRepository<LearnModel, String> {
    List<LearnModel> findByUserId(String userId);
}