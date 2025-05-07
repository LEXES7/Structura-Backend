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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SECRET_KEY = "58rJZYctShDfvcPWO6ACjw8DexOpYoiYp2h1ZO9BqJ4";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public User signup(String username, String email, String password) {
        validateUsername(username);

        // Check if email is already registered
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        return userRepository.save(user);
    }

    public User signupGoogle(User user) {
        try {
            LOGGER.info("Starting Google signup for user: " + user.getEmail());

            // Check if email already exists
            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                LOGGER.info("User with email " + user.getEmail() + " already exists, returning existing user");
                return existingUser.get();
            }

            // Validate username before saving
            String username = user.getUsername();
            if (username == null || username.trim().isEmpty()) {
                username = user.getEmail().split("@")[0] + UUID.randomUUID().toString().substring(0, 4);
                user.setUsername(username);
                LOGGER.info("Generated username: " + username);
            } else {
                try {
                    validateUsername(username);
                } catch (RuntimeException e) {
                    // If username validation fails, append random string
                    username = username + UUID.randomUUID().toString().substring(0, 4);
                    user.setUsername(username);
                    LOGGER.info("Modified username to ensure uniqueness: " + username);
                }
            }

            // Generate a random password for OAuth users if not already set
            if (user.getPassword() == null) {
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            }

            // Ensure timestamps are set
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(new Date());
            }
            if (user.getUpdatedAt() == null) {
                user.setUpdatedAt(new Date());
            }

            // Save and return the user
            User savedUser = userRepository.save(user);
            LOGGER.info("Successfully created new user with ID: " + savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in signupGoogle", e);
            throw new RuntimeException("Failed to create user via Google login: " + e.getMessage());
        }
    }

    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return generateJwt(user.getId());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(String userId, User updatedUser, String authUserId) {
        User existingUser = findById(userId);

        // Only admin or the user themselves can update their profile
        if (!userId.equals(authUserId)) {
            User requester = findById(authUserId);
            if (!requester.isAdmin()) {
                throw new RuntimeException("You don't have permission to update this user");
            }
        }

        // Update fields
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(existingUser.getUsername())) {
            validateUsername(updatedUser.getUsername());
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (updatedUser.getProfilePicture() != null) {
            existingUser.setProfilePicture(updatedUser.getProfilePicture());
        }

        // Only admin can change admin status
        if (updatedUser.isAdmin() != existingUser.isAdmin()) {
            User requester = findById(authUserId);
            if (requester.isAdmin()) {
                existingUser.setAdmin(updatedUser.isAdmin());
            }
        }

        existingUser.setUpdatedAt(new Date());
        return userRepository.save(existingUser);
    }

    public void deleteUser(String userId, String authUserId) {
        User existingUser = findById(userId);

        // Only admin or the user themselves can delete their profile
        if (!userId.equals(authUserId)) {
            User requester = findById(authUserId);
            if (!requester.isAdmin()) {
                throw new RuntimeException("You don't have permission to delete this user");
            }
        }

        userRepository.delete(existingUser);
    }

    public String generateJwt(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // Use the older JWT API format that works with your version
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateJwt(String token) {
        try {
            // Use the older JWT API format that works with your version
            return Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException | MalformedJwtException e) {
            return null;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void validateUsername(String username) {
        if (username == null || username.length() < 3) {
            throw new RuntimeException("Username must be at least 3 characters");
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
    }

    public User findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}