package backend.service;

import backend.model.ReviewModel;
import backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ReviewService {
    private static final Logger LOGGER = Logger.getLogger(ReviewService.class.getName());

    @Autowired
    private ReviewRepository reviewRepository;

    public List<ReviewModel> getAllReviews() {
        LOGGER.info("Getting all reviews");
        return reviewRepository.findAll();
    }

    public List<ReviewModel> getReviewsByRecent() {
        LOGGER.info("Getting reviews sorted by recent");
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ReviewModel> getReviewsByRating() {
        LOGGER.info("Getting reviews sorted by rating");
        return reviewRepository.findAllByOrderByRatingDesc();
    }

    public ReviewModel getReviewById(String id) {
        LOGGER.info("Getting review by ID: " + id);
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
    }

    public ReviewModel createReview(ReviewModel review) {
        LOGGER.info("Creating new review");

        // Validate rating (1-5 stars)
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Set timestamps
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        return reviewRepository.save(review);
    }

    public ReviewModel updateReview(String id, ReviewModel updatedReview) {
        LOGGER.info("Updating review with ID: " + id);

        ReviewModel existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));

        // Validate rating
        if (updatedReview.getRating() < 1 || updatedReview.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Update fields
        existingReview.setName(updatedReview.getName());
        existingReview.setRating(updatedReview.getRating());
        existingReview.setDescription(updatedReview.getDescription());
        existingReview.setUpdatedAt(new Date());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(String id) {
        LOGGER.info("Deleting review with ID: " + id);
        reviewRepository.deleteById(id);
    }
}