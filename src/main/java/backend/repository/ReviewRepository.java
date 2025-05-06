package backend.repository;

import backend.model.ReviewModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<ReviewModel, String> {
    List<ReviewModel> findAllByOrderByCreatedAtDesc();
    List<ReviewModel> findAllByOrderByRatingDesc();
}