package backend.service;

import backend.model.CommentModel;
import backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class CommentService {
    private static final Logger LOGGER = Logger.getLogger(CommentService.class.getName());
    @Autowired
    private CommentRepository commentRepository;

    public CommentModel createComment(String postId, String userId, String username, String content) {
        LOGGER.info("Creating comment for postId: " + postId + " by userId: " + userId);
        CommentModel comment = new CommentModel(postId, userId, username, content);
        return commentRepository.save(comment);
    }

    public List<CommentModel> getCommentsByPostId(String postId) {
        LOGGER.info("Fetching comments for postId: " + postId);
        return commentRepository.findByPostId(postId);
    }
}