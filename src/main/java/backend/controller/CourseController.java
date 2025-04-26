package backend.controller;

import backend.model.CourseModel;
import backend.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {
    private static final Logger LOGGER = Logger.getLogger(CourseController.class.getName());
    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<?> createCourse(
            @RequestParam("courseName") String courseName,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received POST /api/courses for userId: " + userId);
            CourseModel course = courseService.createCourse(userId, courseName, pdfFile, videoFile);
            return ResponseEntity.ok(course);
        } catch (IOException e) {
            LOGGER.severe("Error creating course: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error creating course: " + e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.severe("Error creating course: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "Error creating course: " + e.getMessage()));
        }
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<?> updateCourse(
            @PathVariable String courseId,
            @RequestParam("courseName") String courseName,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received PUT /api/courses/" + courseId + " for userId: " + userId);
            CourseModel updatedCourse = courseService.updateCourse(courseId, userId, courseName, pdfFile, videoFile);
            return ResponseEntity.ok(updatedCourse);
        } catch (IOException e) {
            LOGGER.severe("Error updating course: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error updating course: " + e.getMessage()));
        } catch (RuntimeException e) {
            LOGGER.severe("Error updating course: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "Error updating course: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable String courseId) {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received DELETE /api/courses/" + courseId + " for userId: " + userId);
            courseService.deleteCourse(courseId, userId);
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
        } catch (RuntimeException e) {
            LOGGER.severe("Error deleting course: " + e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "Error deleting course: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        try {
            LOGGER.info("Received GET /api/courses");
            List<CourseModel> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            LOGGER.severe("Error fetching all courses: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching courses: " + e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserCourses() {
        try {
            String userId = getCurrentUserId();
            LOGGER.info("Received GET /api/courses/user for userId: " + userId);
            List<CourseModel> courses = courseService.getCoursesByUserId(userId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            LOGGER.severe("Error fetching user courses: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error fetching user courses: " + e.getMessage()));
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