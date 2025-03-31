package backend.repository;

import backend.model.PostModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PostRepository extends MongoRepository<PostModel, String> {
    List<PostModel> findByUserId(String userId);
}