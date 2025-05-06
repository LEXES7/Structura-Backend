package backend.controller;

import backend.model.CommentModel;
import backend.repository.UserRepository;
import backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CommentController {
    private static final Logger LOGGER = Logger.getLogger(CommentController.class.getName());
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> createComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getCurrentUserId();
            String username = getCurrentUsername(userId);
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                LOGGER.warning("Comment content is empty for postId: " + postId);
                return ResponseEntity.badRequest().body(Map.of("message", "Comment content cannot be empty"));
            }
            LOGGER.info("POST /api/comments/post/" + postId + " for userId: " + userId + ", username: " + username + ", token: " + (authHeader != null ? authHeader.substring(0, 20) + "..." : "none"));
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
            LOGGER.info("GET /api/comments/post/" + postId);
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
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getCurrentUserId();
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                LOGGER.warning("Comment content is empty for commentId: " + commentId);
                return ResponseEntity.badRequest().body(Map.of("message", "Comment content cannot be empty"));
            }
            LOGGER.info("PUT /api/comments/" + commentId + " for userId: " + userId + ", token: " + (authHeader != null ? authHeader.substring(0, 20) + "..." : "none"));
            CommentModel updatedComment = commentService.updateComment(commentId, userId, content);
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalAccessException e) {
            LOGGER.severe("Unauthorized comment update: " + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.severe("Comment not found: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOGGER.severe("Error updating comment: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error updating comment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String commentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("DELETE /api/comments/" + commentId + " for userId: " + userId + ", token: " + (authHeader != null ? authHeader.substring(0, 20) + "..." : "none"));
            commentService.deleteComment(commentId, userId);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (IllegalAccessException e) {
            LOGGER.severe("Unauthorized comment deletion: " + e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.severe("Comment not found: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            LOGGER.severe("Error deleting comment: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting comment: " + e.getMessage()));
        }
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOGGER.info("Principal type: " + principal.getClass().getName() + ", value: " + principal);
        if (principal instanceof String) {
            return (String) principal;
        }
        throw new IllegalStateException("User not authenticated");
    }

    private String getCurrentUsername(String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse("Anonymous");
    }
}