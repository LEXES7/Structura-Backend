package backend.controller;

import backend.model.PostModel;
import backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private static final Logger LOGGER = Logger.getLogger(PostController.class.getName());
    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestParam("postName") String postName,
            @RequestParam("postCategory") String postCategory,
            @RequestParam("postDescription") String postDescription,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received POST /api/posts for userId: " + userId);
            PostModel post = postService.createPost(userId, postName, postCategory, postDescription, file);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            LOGGER.severe("Error creating post: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error creating post: " + e.getMessage()));
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable String postId,
            @RequestParam("postName") String postName,
            @RequestParam("postCategory") String postCategory,
            @RequestParam("postDescription") String postDescription,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received PUT /api/posts/" + postId + " for userId: " + userId);
            PostModel updatedPost = postService.updatePost(postId, userId, postName, postCategory, postDescription, file);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            LOGGER.severe("Error updating post: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error updating post: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received DELETE /api/posts/" + postId + " for userId: " + userId);
            postService.deletePost(postId, userId);
            return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
        } catch (Exception e) {
            LOGGER.severe("Error deleting post: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error deleting post: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            LOGGER.info("Received GET /api/posts");
            List<PostModel> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            LOGGER.severe("Error fetching all posts: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching posts: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserPosts() {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received GET /api/posts/user for userId: " + userId);
            List<PostModel> posts = postService.getPostsByUserId(userId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            LOGGER.severe("Error fetching user posts: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching user posts: " + e.getMessage()));
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
}