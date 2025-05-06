 package backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
public class PublicImageController {
    private static final Logger LOGGER = Logger.getLogger(PublicImageController.class.getName());
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/uploads/";

    public PublicImageController() {
        // Log the upload directory at startup
        LOGGER.info("PublicImageController initialized. Upload directory: " + UPLOAD_DIR);

        // Check if directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            LOGGER.warning("Upload directory does not exist: " + UPLOAD_DIR);
            uploadDir.mkdirs();
            LOGGER.info("Created upload directory: " + UPLOAD_DIR);
        } else {
            LOGGER.info("Upload directory exists: " + UPLOAD_DIR);

            // Count files in directory
            File[] files = uploadDir.listFiles();
            if (files != null) {
                LOGGER.info("Number of files in upload directory: " + files.length);
            } else {
                LOGGER.warning("Failed to list files in upload directory");
            }
        }
    }

    @GetMapping("/api/public/image/{filename:.+}")
    public ResponseEntity<byte[]> getPublicImage(@PathVariable String filename) {
        try {
            LOGGER.info("PUBLIC controller serving image: " + filename);
            File file = new File(UPLOAD_DIR + filename);
            LOGGER.info("Looking for file at: " + file.getAbsolutePath());

            if (!file.exists()) {
                LOGGER.warning("File not found: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            LOGGER.info("File found, size: " + file.length() + " bytes");
            byte[] fileContent = Files.readAllBytes(file.toPath());

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
                    .header("Access-Control-Allow-Origin", "*")  // Ensure CORS is enabled
                    .header("Cache-Control", "public, max-age=31536000") // Cache for a year
                    .body(fileContent);
        } catch (IOException e) {
            LOGGER.severe("Error serving image: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/public/debug/file-check/{filename:.+}")
    public ResponseEntity<?> checkFileExistence(@PathVariable String filename) {
        File file = new File(UPLOAD_DIR + filename);

        Map<String, Object> result = new HashMap<>();
        result.put("filename", filename);
        result.put("fullPath", file.getAbsolutePath());
        result.put("exists", file.exists());
        result.put("canRead", file.canRead());
        result.put("size", file.exists() ? file.length() : -1);
        result.put("isDirectory", file.isDirectory());
        result.put("lastModified", file.exists() ? new java.util.Date(file.lastModified()) : null);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/public/debug/list-files")
    public ResponseEntity<?> listAllFiles() {
        try {
            Path uploadsDir = Paths.get(UPLOAD_DIR);

            Map<String, Object> result = new HashMap<>();
            result.put("uploadDir", UPLOAD_DIR);
            result.put("exists", Files.exists(uploadsDir));
            result.put("isDirectory", Files.isDirectory(uploadsDir));
            result.put("isReadable", Files.isReadable(uploadsDir));

            if (Files.exists(uploadsDir) && Files.isDirectory(uploadsDir)) {
                result.put("files", Files.list(uploadsDir)
                        .map(p -> {
                            Map<String, Object> fileInfo = new HashMap<>();
                            fileInfo.put("name", p.getFileName().toString());
                            try {
                                fileInfo.put("size", Files.size(p));
                                fileInfo.put("lastModified", Files.getLastModifiedTime(p).toMillis());
                                fileInfo.put("isReadable", Files.isReadable(p));
                            } catch (IOException e) {
                                fileInfo.put("error", e.getMessage());
                            }
                            return fileInfo;
                        })
                        .collect(Collectors.toList()));
            }

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/public/base64/{filename:.+}")
    public ResponseEntity<?> getBase64Image(@PathVariable String filename) {
        try {
            File file = new File(UPLOAD_DIR + filename);
            if (!file.exists()) {
                LOGGER.warning("File not found for base64 encoding: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(file.toPath());
            String base64 = java.util.Base64.getEncoder().encodeToString(fileContent);

            String contentType = "image/jpeg";
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("base64", base64);
            result.put("contentType", contentType);
            result.put("dataUrl", "data:" + contentType + ";base64," + base64);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            LOGGER.severe("Error generating base64 image: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}