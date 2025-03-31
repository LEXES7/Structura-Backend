package backend.service;

import backend.model.User;
import backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SECRET_KEY = "58rJZYctShDfvcPWO6ACjw8DexOpYoiYp2h1ZO9BqJ4"; // Matches JwtFilter
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public User signup(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(new java.util.Date());
        user.setUpdatedAt(new java.util.Date());
        return userRepository.save(user);
    }

    public User signupGoogle(User user) {
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return generateJwt(user.getId());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(String userId, User updatedUser, String authUserId) {
        if (!userId.equals(authUserId)) {
            throw new RuntimeException("You are not allowed to update this user");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getUsername() != null) {
            validateUsername(updatedUser.getUsername());
            user.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null) {
            if (updatedUser.getPassword().length() < 6) throw new RuntimeException("Password must be at least 6 characters");
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.setUpdatedAt(new java.util.Date());
        return userRepository.save(user);
    }

    public void deleteUser(String userId, String authUserId) {
        if (!userId.equals(authUserId)) {
            throw new RuntimeException("You are not allowed to delete this user");
        }
        userRepository.deleteById(userId);
    }

    public String generateJwt(String userId) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY); // Consistent with JwtFilter
        Key key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private void validateUsername(String username) {
        if (username.length() < 7 || username.length() > 20) throw new RuntimeException("Username must be between 7 and 20 characters");
        if (username.contains(" ")) throw new RuntimeException("Username cannot contain spaces");
        if (!username.equals(username.toLowerCase())) throw new RuntimeException("Username must be lowercase");
        if (!username.matches("^[a-zA-Z0-9]+$")) throw new RuntimeException("Username must contain only letters and numbers");
    }
}