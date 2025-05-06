package backend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
public class FileController {
    private static final Logger LOGGER = Logger.getLogger(FileController.class.getName());
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/uploads/";

    public FileController() {
        // Ensure upload directory exists when controller starts
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                LOGGER.info("Creating upload directory: " + uploadPath.toAbsolutePath());
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to create upload directory: " + e.getMessage());
        }
    }

    @GetMapping("/api/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            LOGGER.info("Serving file: " + filename);
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            LOGGER.info("Looking for file at: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                LOGGER.info("File found: " + filePath.toAbsolutePath());
                // Determine content type
                String contentType = "application/octet-stream";
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                LOGGER.warning("File not found: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            LOGGER.severe("Error serving file: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/api/debug/uploads-info")
    public ResponseEntity<?> getUploadsInfo() {
        Map<String, Object> info = new HashMap<>();

        Path uploadsDir = Paths.get(UPLOAD_DIR);
        info.put("uploadsPath", uploadsDir.toAbsolutePath().toString());
        info.put("exists", Files.exists(uploadsDir));
        info.put("writable", Files.isWritable(uploadsDir));
        info.put("readable", Files.isReadable(uploadsDir));

        if (Files.exists(uploadsDir)) {
            try {
                List<String> files = Files.list(uploadsDir)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());

                info.put("files", files);
                info.put("fileCount", files.size());
            } catch (IOException e) {
                info.put("error", e.getMessage());
            }
        }

        return ResponseEntity.ok(info);
    }

    @GetMapping("/api/files/list")
    public ResponseEntity<?> listFiles() {
        try {
            LOGGER.info("Request to list uploaded files");
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                LOGGER.warning("Upload directory does not exist: " + uploadPath);
                return ResponseEntity.ok(Map.of(
                        "files", new ArrayList<>(),
                        "error", "Upload directory does not exist"
                ));
            }

            List<String> files = Files.list(uploadPath)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("files", files));
        } catch (Exception e) {
            LOGGER.severe("Error listing files: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error listing files: " + e.getMessage()
            ));
        }
    }
}