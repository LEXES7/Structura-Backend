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
import java.util.List;
import java.util.logging.Logger;

@Service
public class PostService {
    private static final Logger LOGGER = Logger.getLogger(PostService.class.getName());
    @Autowired
    private PostRepository postRepository;

    // Updated to save files in src/main/resources/uploads/
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/uploads/";

    public PostModel createPost(String userId, String postName, String postCategory, String postDescription, MultipartFile file) throws IOException {
        LOGGER.info("Creating post for userId: " + userId);
        PostModel post = new PostModel();
        post.setUserId(userId);
        post.setPostName(postName);
        post.setPostCategory(postCategory);
        post.setPostDescription(postDescription);

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info("Saving file to: " + path.toString());
            Files.createDirectories(path.getParent()); // Ensure directory exists
            Files.write(path, file.getBytes());
            post.setPostImg("/uploads/" + fileName); // Save relative path for frontend usage
            LOGGER.info("File saved successfully: " + path.toString());
        } else {
            post.setPostImg("default.png"); // Default value as per your model
        }

        return postRepository.save(post);
    }

    public PostModel getPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public PostModel updatePost(String postId, String userId, String postName, String postCategory, String postDescription, MultipartFile file) throws IOException {
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this post");
        }
        post.setPostName(postName);
        post.setPostCategory(postCategory);
        post.setPostDescription(postDescription);

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            LOGGER.info("Saving file to: " + path.toString());
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            post.setPostImg("/uploads/" + fileName); // Save relative path for frontend usage
            LOGGER.info("File updated successfully: " + path.toString());
        }

        return postRepository.save(post);
    }

    public void deletePost(String postId, String userId) {
        PostModel post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this post");
        }
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