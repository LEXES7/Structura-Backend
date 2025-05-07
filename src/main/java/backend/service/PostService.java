package backend.service;

import backend.model.PostModel;
import backend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PostService {
    private static final Logger LOGGER = Logger.getLogger(PostService.class.getName());

    @Autowired
    private PostRepository postRepository;

    // Updated to use resources/uploads
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/uploads/";

    public PostService() {
        // Ensure upload directory exists when service starts
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                LOGGER.info("Creating upload directory: " + uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            } else {
                LOGGER.info("Upload directory exists at: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to create upload directory: " + e.getMessage());
        }
    }

    public PostModel createPost(String userId, String postName, String postCategory, String postDescription, MultipartFile file) throws IOException {
        LOGGER.info("Creating post for userId: " + userId);
        PostModel post = new PostModel();
        post.setUserId(userId);
        post.setPostName(postName);
        post.setPostCategory(postCategory);
        post.setPostDescription(postDescription);
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());

        // Initialize collections
        post.setLikedBy(new ArrayList<>());
        post.setShareCount(0);

        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" +
                    (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "unnamed");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info(String.format("Saving file to: %s", filePath.toAbsolutePath()));

            // Ensure directory exists
            Files.createDirectories(filePath.getParent());

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Store the path for frontend access
            post.setPostImg("/uploads/" + fileName);
            LOGGER.info(String.format("File saved with path: %s", post.getPostImg()));
        } else {
            post.setPostImg("default.png");
            LOGGER.info("No file uploaded, using default image");
        }

        return postRepository.save(post);
    }

    public PostModel getPostById(String postId) {
        LOGGER.info(String.format("Fetching post by ID: %s", postId));
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        // Ensure collections are initialized
        if (post.getLikedBy() == null) {
            post.setLikedBy(new ArrayList<>());
        }

        return post;
    }

    public PostModel toggleLike(String postId, String userId) {
        if (postId == null || postId.trim().isEmpty()) {
            LOGGER.severe("Post ID cannot be null or empty");
            throw new IllegalArgumentException("Post ID cannot be null or empty");
        }

        if (userId == null || userId.trim().isEmpty()) {
            LOGGER.severe("User ID cannot be null or empty");
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        LOGGER.info(String.format("Toggling like for post %s by user %s", postId, userId));

        try {
            // Check if post exists
            PostModel post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

            // Initialize likedBy if null
            if (post.getLikedBy() == null) {
                post.setLikedBy(new ArrayList<>());
            }

            // Toggle like
            if (post.getLikedBy().contains(userId)) {
                LOGGER.info(String.format("Removing like from user: %s", userId));
                post.getLikedBy().remove(userId);
            } else {
                LOGGER.info(String.format("Adding like from user: %s", userId));
                post.getLikedBy().add(userId);
            }

            post.setUpdatedAt(new Date());

            LOGGER.info(String.format("Saving post with updated likes. Like count: %d", post.getLikedBy().size()));
            return postRepository.save(post);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in toggleLike: " + e.getMessage(), e);
            throw e;  // Re-throw to propagate to the controller
        }
    }

    public PostModel incrementShareCount(String postId) {
        LOGGER.info(String.format("Incrementing share count for post %s", postId));
        try {
            PostModel post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

            // Fixed null check for Integer
            Integer shareCount = post.getShareCount();
            if (shareCount == null) {
                post.setShareCount(1);
            } else {
                post.setShareCount(shareCount + 1);
            }
            post.setUpdatedAt(new Date());

            LOGGER.info(String.format("New share count: %d", post.getShareCount()));
            return postRepository.save(post);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error incrementing share count: " + e.getMessage(), e);
            throw e;
        }
    }

    public PostModel updatePost(String postId, String userId, String postName, String postCategory, String postDescription, MultipartFile file) throws IOException {
        LOGGER.info(String.format("Updating post %s for user %s", postId, userId));
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            LOGGER.warning(String.format("User %s attempted to update post %s without permission", userId, postId));
            throw new RuntimeException("Unauthorized to update this post");
        }

        post.setPostName(postName);
        post.setPostCategory(postCategory);
        post.setPostDescription(postDescription);
        post.setUpdatedAt(new Date());

        // Ensure collections are initialized
        if (post.getLikedBy() == null) {
            post.setLikedBy(new ArrayList<>());
        }

        Integer shareCount = post.getShareCount();
        if (shareCount == null) {
            post.setShareCount(0);
        }

        if (file != null && !file.isEmpty()) {
            // Delete old image file if it exists and isn't the default
            if (post.getPostImg() != null && !post.getPostImg().equals("default.png") && post.getPostImg().startsWith("/uploads/")) {
                try {
                    String oldFilename = post.getPostImg().substring("/uploads/".length());
                    Path oldFilePath = Paths.get(UPLOAD_DIR, oldFilename);
                    if (Files.exists(oldFilePath)) {
                        LOGGER.info(String.format("Deleting old image file: %s", oldFilePath));
                        Files.delete(oldFilePath);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Failed to delete old image: " + e.getMessage());
                    // Continue even if deletion fails
                }
            }

            // Save new image
            String originalFilename = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" +
                    (originalFilename != null ? originalFilename.replaceAll("\\s+", "_") : "unnamed");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info(String.format("Saving new file to: %s", filePath.toAbsolutePath()));

            // Ensure directory exists
            Files.createDirectories(filePath.getParent());

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            post.setPostImg("/uploads/" + fileName);
            LOGGER.info(String.format("File updated successfully with path: %s", post.getPostImg()));
        }

        LOGGER.info("Saving updated post to database");
        return postRepository.save(post);
    }

    public void deletePost(String postId, String userId) {
        LOGGER.info(String.format("Deleting post %s for user %s", postId, userId));
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            LOGGER.warning(String.format("User %s attempted to delete post %s without permission", userId, postId));
            throw new RuntimeException("Unauthorized to delete this post");
        }

        // Delete image file if it exists and isn't the default
        if (post.getPostImg() != null && !post.getPostImg().equals("default.png") && post.getPostImg().startsWith("/uploads/")) {
            try {
                String filename = post.getPostImg().substring("/uploads/".length());
                Path filePath = Paths.get(UPLOAD_DIR, filename);
                if (Files.exists(filePath)) {
                    LOGGER.info(String.format("Deleting image file: %s", filePath));
                    Files.delete(filePath);
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to delete image file: " + e.getMessage());
                // Continue even if file deletion fails
            }
        }

        LOGGER.info("Removing post from database");
        postRepository.delete(post);
    }

    public List<PostModel> getPostsByUserId(String userId) {
        LOGGER.info(String.format("Fetching posts for userId: %s", userId));
        List<PostModel> posts = postRepository.findByUserId(userId);

        // Ensure collections are initialized for all posts
        for (PostModel post : posts) {
            if (post.getLikedBy() == null) {
                post.setLikedBy(new ArrayList<>());
            }
            Integer shareCount = post.getShareCount();
            if (shareCount == null) {
                post.setShareCount(0);
            }
        }

        return posts;
    }

    public List<PostModel> getAllPosts() {
        LOGGER.info("Fetching all posts");
        List<PostModel> posts = postRepository.findAll();

        // Ensure collections are initialized for all posts
        for (PostModel post : posts) {
            if (post.getLikedBy() == null) {
                post.setLikedBy(new ArrayList<>());
            }
            Integer shareCount = post.getShareCount();
            if (shareCount == null) {
                post.setShareCount(0);
            }
        }

        return posts;
    }
}