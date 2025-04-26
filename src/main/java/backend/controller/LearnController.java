package backend.controller;

import backend.model.LearnModel;
import backend.service.LearnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/learns")
public class LearnController {
    private static final Logger LOGGER = Logger.getLogger(LearnController.class.getName());
    @Autowired
    private LearnService learnService;

    @PostMapping
    public ResponseEntity<?> createLearn(
            @RequestParam("learnName") String learnName,
            @RequestParam("learnCategory") String learnCategory,
            @RequestParam("learnDescription") String learnDescription,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received POST /api/learns for userId: " + userId);
            LearnModel learn = learnService.createLearn(userId, learnName, learnCategory, learnDescription, file);
            return ResponseEntity.ok(learn);
        } catch (IOException e) {
            LOGGER.severe("Error creating learn: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error creating learn: " + e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.severe("Error creating learn: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("message", "Error creating learn: " + e.getMessage()));
        }
    }

//    @PutMapping("/{learnId}")
//    public ResponseEntity<?> updateLearn(
//            @PathVariable String learnId,
//            @RequestParam("learnName") String learnName,
//            @RequestParam("learnCategory") String learnCategory,
//            @RequestParam("learnDescription") String learnDescription,
//            @RequestParam(value = "file", required = false) MultipartFile file) {
//        try {
//            String userId = getCurrentUserId();
//            LOGGER.info("Received PUT /api/learns/" + learnId + " for userId: " + userId);
//            LearnModel updatedLearn = learnService.updateLearn(learnId, userId, learnName, learnCategory, learnDescription, file);
//            return ResponseEntity.ok(updatedLearn);
//        } catch (IOException e) {
//            LOGGER.severe("Error updating learn: " + e.getMessage());
//            return ResponseEntity.status(500).body(Map.of("message", "Error updating learn: " + e.getMessage()));
//        } catch (RuntimeException e) {
//            LOGGER.severe("Error updating learn: " + e.getMessage());
//            return ResponseEntity.status(400).body(Map.of("message", "Error updating learn: " + e.getMessage()));
//        }
//    }
@PutMapping("/{learnId}")
public ResponseEntity<?> updateLearn(
        @PathVariable String learnId,
        @RequestParam("learnName") String learnName,
        @RequestParam("learnCategory") String learnCategory,
        @RequestParam("learnDescription") String learnDescription,
        @RequestParam(value = "file", required = false) MultipartFile file) {
    try {
        String userId = getCurrentUserId();
        LOGGER.info("Received PUT /api/learns/" + learnId + " for userId: " + userId);
        LearnModel updatedLearn = learnService.updateLearn(learnId, userId, learnName, learnCategory, learnDescription, file);
        return ResponseEntity.ok(updatedLearn);
    } catch (IOException e) {
        LOGGER.severe("Error updating learn: " + e.getMessage());
        return ResponseEntity.status(500).body(Map.of("error", "Error updating learn: " + e.getMessage()));
    } catch (RuntimeException e) {
        LOGGER.severe("Error updating learn: " + e.getMessage());
        return ResponseEntity.status(400).body(Map.of("error", "Error updating learn: " + e.getMessage()));
    }
}




    @DeleteMapping("/{learnId}")
    public ResponseEntity<?> deleteLearn(@PathVariable String learnId) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received DELETE /api/learns/" + learnId + " for userId: " + userId);
            learnService.deleteLearn(learnId, userId);
            return ResponseEntity.ok(Map.of("message", "Learn deleted successfully"));
        } catch (RuntimeException e) {
            LOGGER.severe("Error deleting learn: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("message", "Error deleting learn: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLearns() {
        try {
            LOGGER.info("Received GET /api/learns");
            List<LearnModel> learns = learnService.getAllLearns();
            return ResponseEntity.ok(learns);
        } catch (Exception e) {
            LOGGER.severe("Error fetching all learns: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching learns: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserLearns() {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received GET /api/learns/user for userId: " + userId);
            List<LearnModel> learns = learnService.getLearnsByUserId(userId);
            return ResponseEntity.ok(learns);
        } catch (Exception e) {
            LOGGER.severe("Error fetching user learns: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Error fetching user learns: " + e.getMessage()));
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