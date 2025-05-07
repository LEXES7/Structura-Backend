package backend.controller;

import backend.model.User;
import backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal String authUserId) {
        // Verify the requesting user is an admin
        User requester = userService.findById(authUserId);
        if (!requester.isAdmin()) {
            return ResponseEntity.status(403).body(null);
        }
        List<User> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId, @RequestBody User user,
                                                          @AuthenticationPrincipal String authUserId) {
        User updatedUser = userService.updateUser(userId, user, authUserId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", updatedUser.getId());
        response.put("username", updatedUser.getUsername());
        response.put("email", updatedUser.getEmail());
        response.put("isAdmin", updatedUser.isAdmin());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId, @AuthenticationPrincipal String authUserId) {
        userService.deleteUser(userId, authUserId);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
    }

    @PostMapping("/signout")
    public ResponseEntity<Map<String, Object>> signout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Signout successful"));
    }
}