package backend.controller;

import backend.model.CommentModel;
import backend.model.User;
import backend.repository.UserRepository;
import backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private static final Logger LOGGER = Logger.getLogger(CommentController.class.getName());
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> createComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String username = getCurrentUsername(userId);
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Comment content cannot be empty fill out"));
            }
            LOGGER.info("Received POST /api/comments/post/" + postId + " for userId: " + userId + " with username: " + username);
            CommentModel comment = commentService.createComment(postId, userId, username, content);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            LOGGER.severe("Error creating comment: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error creating comment: " + e.getMessage()));
        }
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable String postId) {
        try {
            LOGGER.info("Received GET /api/comments/post/" + postId);
            List<CommentModel> comments = commentService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            LOGGER.severe("Error fetching comments: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching comments: " + e.getMessage()));
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable String commentId,
            @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Comment content cannot be empty"));
            }
            LOGGER.info("Received PUT /api/comments/" + commentId + " for userId: " + userId);
            CommentModel updatedComment = commentService.updateComment(commentId, userId, content);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalAccessException e) {
            LOGGER.severe("Unauthorized comment update: " + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "You can only edit your own comments"));
        } catch (Exception e) {
            LOGGER.severe("Error updating comment: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error updating comment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable String commentId) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received DELETE /api/comments/" + commentId + " for userId: " + userId);
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (IllegalAccessException e) {
            LOGGER.severe("Unauthorized comment deletion: " + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "You can only delete your own comments"));
        } catch (Exception e) {
            LOGGER.severe("Error deleting comment: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting comment: " + e.getMessage()));
        }
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOGGER.info("Principal type: " + principal.getClass().getName() + ", value: " + principal);
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private String getCurrentUsername(String userId) {
        // Fetch user by ID (MongoDB _id)
        return userRepository.findById(userId)
                .map(user -> user.getUsername()) // Use username field (e.g., "sachintha")
                .orElse("Anonymous");
    }
}