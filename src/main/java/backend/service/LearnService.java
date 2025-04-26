package backend.service;

import backend.model.LearnModel;
import backend.repository.LearnRepository;
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
public class LearnService {
    private static final Logger LOGGER = Logger.getLogger(LearnService.class.getName());
    @Autowired
    private LearnRepository learnRepository;

    private static final String UPLOAD_DIR = "uploads/"; // Directory for uploaded images

    public LearnModel createLearn(String userId, String learnName, String learnCategory, String learnDescription, MultipartFile file) throws IOException {
        LOGGER.info("Creating learn for userId: " + userId);
        LearnModel learn = new LearnModel();
        learn.setUserId(userId);
        learn.setLearnName(learnName);
        learn.setLearnCategory(learnCategory);
        learn.setLearnDescription(learnDescription);

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);
                Files.createDirectories(path.getParent()); // Ensure directory exists
                Files.write(path, file.getBytes());
                learn.setLearnImg("/" + UPLOAD_DIR + fileName);
                LOGGER.info("Uploaded file to: " + path);
            } catch (IOException e) {
                LOGGER.severe("Failed to upload file: " + e.getMessage());
                throw new IOException("Failed to upload file", e);
            }
        } else {
            learn.setLearnImg("default.png"); // Default value as per your model
        }

        return learnRepository.save(learn);
    }

    public LearnModel updateLearn(String learnId, String userId, String learnName, String learnCategory, String learnDescription, MultipartFile file) throws IOException {
        LearnModel learn = learnRepository.findById(learnId)
                .orElseThrow(() -> new RuntimeException("Learn not found"));
        if (!learn.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this learn");
        }
        learn.setLearnName(learnName);
        learn.setLearnCategory(learnCategory);
        learn.setLearnDescription(learnDescription);

        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            learn.setLearnImg("/uploads/" + fileName);
        }

        return learnRepository.save(learn);
    }


    public void deleteLearn(String learnId, String userId) {
        LearnModel learn = learnRepository.findById(learnId)
                .orElseThrow(() -> new RuntimeException("Learn not found"));
        if (!learn.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this learn");
        }
        learnRepository.delete(learn);
    }

    public List<LearnModel> getLearnsByUserId(String userId) {
        LOGGER.info("Fetching learns for userId: " + userId);
        return learnRepository.findByUserId(userId);
    }

    public List<LearnModel> getAllLearns() {
        LOGGER.info("Fetching all learns");
        return learnRepository.findAll();
    }
}