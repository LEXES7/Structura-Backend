package backend.controller;

import backend.model.ReviewModel;
import backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReviewController {
    private static final Logger LOGGER = Logger.getLogger(ReviewController.class.getName());

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewModel>> getAllReviews() {
        LOGGER.info("GET /api/reviews");
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ReviewModel>> getRecentReviews() {
        LOGGER.info("GET /api/reviews/recent");
        return ResponseEntity.ok(reviewService.getReviewsByRecent());
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<ReviewModel>> getTopRatedReviews() {
        LOGGER.info("GET /api/reviews/top-rated");
        return ResponseEntity.ok(reviewService.getReviewsByRating());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewModel> getReviewById(@PathVariable String id) {
        LOGGER.info("GET /api/reviews/" + id);
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PostMapping
    public ResponseEntity<ReviewModel> createReview(@RequestBody ReviewModel review) {
        LOGGER.info("POST /api/reviews with data: " + review);
        return ResponseEntity.ok(reviewService.createReview(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewModel> updateReview(
            @PathVariable String id,
            @RequestBody ReviewModel review
    ) {
        LOGGER.info("PUT /api/reviews/" + id);
        return ResponseEntity.ok(reviewService.updateReview(id, review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        LOGGER.info("DELETE /api/reviews/" + id);
        reviewService.deleteReview(id);
        return ResponseEntity.ok().build();
    }
}