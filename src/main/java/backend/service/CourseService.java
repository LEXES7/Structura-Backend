package backend.service;

import backend.model.CourseModel;
import backend.repository.CourseRepository;
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
public class CourseService {
    private static final Logger LOGGER = Logger.getLogger(CourseService.class.getName());
    @Autowired
    private CourseRepository courseRepository;

    private static final String UPLOAD_DIR = "uploads/"; // Directory for uploaded files

    public CourseModel createCourse(String userId, String courseName, MultipartFile pdfFile, MultipartFile videoFile) throws IOException {
        LOGGER.info("Creating course for userId: " + userId);
        CourseModel course = new CourseModel();
        course.setUserId(userId);
        course.setCourseName(courseName);

        if (pdfFile != null && !pdfFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + pdfFile.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.createDirectories(path.getParent()); // Ensure directory exists
                Files.write(path, pdfFile.getBytes());
                course.setCoursePdf("/" + UPLOAD_DIR + fileName);
                LOGGER.info("Uploaded PDF file to: " + path);
            } catch (IOException e) {
                LOGGER.severe("Failed to upload PDF file: " + e.getMessage());
                throw new IOException("Failed to upload PDF file", e);
            }
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, videoFile.getBytes());
                course.setCourseVideo("/" + UPLOAD_DIR + fileName);
                LOGGER.info("Uploaded video file to: " + path);
            } catch (IOException e) {
                LOGGER.severe("Failed to upload video file: " + e.getMessage());
                throw new IOException("Failed to upload video file", e);
            }
        }

        return courseRepository.save(course);
    }

    public CourseModel updateCourse(String courseId, String userId, String courseName, MultipartFile pdfFile, MultipartFile videoFile) throws IOException {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this course");
        }
        course.setCourseName(courseName);

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + pdfFile.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, pdfFile.getBytes());
            course.setCoursePdf("/" + UPLOAD_DIR + fileName);
            LOGGER.info("Updated PDF file to: " + path);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + videoFile.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, videoFile.getBytes());
            course.setCourseVideo("/" + UPLOAD_DIR + fileName);
            LOGGER.info("Updated video file to: " + path);
        }

        return courseRepository.save(course);
    }

    public void deleteCourse(String courseId, String userId) {
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this course");
        }
        courseRepository.delete(course);
    }

    public List<CourseModel> getCoursesByUserId(String userId) {
        LOGGER.info("Fetching courses for userId: " + userId);
        return courseRepository.findByUserId(userId);
    }

    public List<CourseModel> getAllCourses() {
        LOGGER.info("Fetching all courses");
        return courseRepository.findAll();
    }
}