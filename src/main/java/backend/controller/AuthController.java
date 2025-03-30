package backend.controller;

import backend.model.User;
import backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody User user) {
        User newUser = userService.signup(user.getUsername(), user.getEmail(), user.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", newUser.getId());
        response.put("username", newUser.getUsername());
        response.put("email", newUser.getEmail());
        response.put("isAdmin", newUser.isAdmin());
        return ResponseEntity.ok(response);
    }

    // Updated to use email instead of username
    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        String token = userService.login(email, password);
        User loggedInUser = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("id", loggedInUser.getId());
        response.put("username", loggedInUser.getUsername());
        response.put("email", loggedInUser.getEmail());
        response.put("isAdmin", loggedInUser.isAdmin());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/google-success")
    public ResponseEntity<Map<String, Object>> googleLoginSuccess(
            @RequestParam("token") String token,
            @RequestParam("id") String id,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("isAdmin") boolean isAdmin) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("id", id);
        response.put("username", username);
        response.put("email", email);
        response.put("isAdmin", isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<Map<String, Object>> signout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Signed out successfully");
        return ResponseEntity.ok(response);
    }
}