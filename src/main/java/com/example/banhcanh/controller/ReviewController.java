package com.example.banhcanh.controller;

import com.example.banhcanh.model.Review;
import com.example.banhcanh.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @GetMapping("/approved")
    public List<Review> getApprovedReviews() {
        return reviewRepository.findByIsApprovedTrue();
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewRepository.save(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Review updated) {
        return reviewRepository.findById(id).map(review -> {
            if (updated.getRating() != null) review.setRating(updated.getRating());
            if (updated.getComment() != null) review.setComment(updated.getComment());
            if (updated.getIsApproved() != null) review.setIsApproved(updated.getIsApproved());
            if (updated.getAdminReply() != null) review.setAdminReply(updated.getAdminReply());
            return ResponseEntity.ok(reviewRepository.save(review));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá"));
    }
}
