package backend.service;

import backend.model.User;
import backend.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SECRET_KEY = "58rJZYctShDfvcPWO6ACjw8DexOpYoiYp2h1ZO9BqJ4";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public User signup(String username, String email, String password) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public User signupGoogle(User user) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public String login(String email, String password) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public User findByUsername(String username) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public Optional<User> findByEmail(String email) {
        // Your existing code here
        return Optional.empty(); // Replace with actual implementation
    }

    public User updateUser(String userId, User updatedUser, String authUserId) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public void deleteUser(String userId, String authUserId) {
        // Your existing code here
    }

    public String generateJwt(String userId) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    public String validateJwt(String token) {
        // Your existing code here
        return null; // Replace with actual implementation
    }

    private void validateUsername(String username) {
        // Your existing code here
    }

    // Add these methods to your UserService class - keep all your existing methods above
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}