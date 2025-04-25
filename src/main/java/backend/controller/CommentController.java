package backend.controller;

import backend.model.CommentModel;
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

    @PostMapping("/post/{postId}")
    public ResponseEntity<?> createComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> request) {
        try {
            String userId = getCurrentUserId();
            String username = getCurrentUsername();
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Comment content cannot be empty"));
            }
            LOGGER.info("Received POST /api/comments/post/" + postId + " for userId: " + userId);
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

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return "Anonymous";
        }
    }
}