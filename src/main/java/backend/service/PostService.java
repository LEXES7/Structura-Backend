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
import java.util.List;
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

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info("Saving file to: " + filePath.toAbsolutePath());

            // Ensure directory exists
            Files.createDirectories(filePath.getParent());

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Store the path for frontend access
            post.setPostImg("/uploads/" + fileName);
            LOGGER.info("File saved with path: " + post.getPostImg());
        } else {
            post.setPostImg("default.png");
            LOGGER.info("No file uploaded, using default image");
        }

        return postRepository.save(post);
    }

    public PostModel getPostById(String postId) {
        LOGGER.info("Fetching post by ID: " + postId);
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
    }

    public PostModel updatePost(String postId, String userId, String postName, String postCategory, String postDescription, MultipartFile file) throws IOException {
        LOGGER.info("Updating post " + postId + " for user " + userId);
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            LOGGER.warning("User " + userId + " attempted to update post " + postId + " without permission");
            throw new RuntimeException("Unauthorized to update this post");
        }

        post.setPostName(postName);
        post.setPostCategory(postCategory);
        post.setPostDescription(postDescription);

        if (file != null && !file.isEmpty()) {
            // Delete old image file if it exists and isn't the default
            if (post.getPostImg() != null && !post.getPostImg().equals("default.png") && post.getPostImg().startsWith("/uploads/")) {
                try {
                    String oldFilename = post.getPostImg().substring("/uploads/".length());
                    Path oldFilePath = Paths.get(UPLOAD_DIR, oldFilename);
                    if (Files.exists(oldFilePath)) {
                        LOGGER.info("Deleting old image file: " + oldFilePath);
                        Files.delete(oldFilePath);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Failed to delete old image: " + e.getMessage());
                    // Continue even if deletion fails
                }
            }

            // Save new image
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("\\s+", "_");
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info("Saving new file to: " + filePath.toAbsolutePath());

            // Ensure directory exists
            Files.createDirectories(filePath.getParent());

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            post.setPostImg("/uploads/" + fileName);
            LOGGER.info("File updated successfully with path: " + post.getPostImg());
        }

        LOGGER.info("Saving updated post to database");
        return postRepository.save(post);
    }

    public void deletePost(String postId, String userId) {
        LOGGER.info("Deleting post " + postId + " for user " + userId);
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));

        if (!post.getUserId().equals(userId)) {
            LOGGER.warning("User " + userId + " attempted to delete post " + postId + " without permission");
            throw new RuntimeException("Unauthorized to delete this post");
        }

        // Delete image file if it exists and isn't the default
        if (post.getPostImg() != null && !post.getPostImg().equals("default.png") && post.getPostImg().startsWith("/uploads/")) {
            try {
                String filename = post.getPostImg().substring("/uploads/".length());
                Path filePath = Paths.get(UPLOAD_DIR, filename);
                if (Files.exists(filePath)) {
                    LOGGER.info("Deleting image file: " + filePath);
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
        LOGGER.info("Fetching posts for userId: " + userId);
        return postRepository.findByUserId(userId);
    }

    public List<PostModel> getAllPosts() {
        LOGGER.info("Fetching all posts");
        return postRepository.findAll();
    }
}