package backend.controller;

import backend.model.PostModel;
import backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping
    public ResponseEntity<List<PostModel>> getAllPosts() {
        LOGGER.info("GET request to fetch all posts");
        List<PostModel> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostModel> getPostById(@PathVariable String postId) {
        LOGGER.info("GET request to fetch post with ID: " + postId);
        try {
            PostModel post = postService.getPostById(postId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            LOGGER.warning("Error fetching post: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<PostModel>> getUserPosts(@AuthenticationPrincipal String userId) {
        LOGGER.info("GET request to fetch posts for user: " + userId);
        List<PostModel> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<PostModel> createPost(
            @RequestParam("postName") String postName,
            @RequestParam("postCategory") String postCategory,
            @RequestParam(value = "postDescription", required = false) String postDescription,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal String userId) throws IOException {

        LOGGER.info("POST request to create new post for user: " + userId);
        PostModel newPost = postService.createPost(userId, postName, postCategory, postDescription, file);
        return ResponseEntity.ok(newPost);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostModel> updatePost(
            @PathVariable String postId,
            @RequestParam("postName") String postName,
            @RequestParam("postCategory") String postCategory,
            @RequestParam(value = "postDescription", required = false) String postDescription,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal String userId) throws IOException {

        LOGGER.info("PUT request to update post " + postId + " by user " + userId);
        try {
            PostModel updatedPost = postService.updatePost(postId, userId, postName, postCategory, postDescription, file);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            LOGGER.warning("Error updating post: " + e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(
            @PathVariable String postId,
            @AuthenticationPrincipal String userId) {

        LOGGER.info("DELETE request for post " + postId + " by user " + userId);
        try {
            postService.deletePost(postId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LOGGER.warning("Error deleting post: " + e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }

    // New endpoints for likes and shares
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable String postId,
            @AuthenticationPrincipal String userId) {
        LOGGER.info("Received like toggle request for post: " + postId + " from user: " + userId);

        // Add validation
        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.warning("User ID is null or empty in like request");
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            PostModel updatedPost = postService.toggleLike(postId, userId);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            LOGGER.warning("Error toggling like: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<PostModel> incrementShare(
            @PathVariable String postId) {
        LOGGER.info("Received share increment request for post: " + postId);
        try {
            PostModel updatedPost = postService.incrementShareCount(postId);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            LOGGER.warning("Error incrementing share count: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}