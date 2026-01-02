package com.opencode.alumxbackend.jobposts.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.opencode.alumxbackend.common.exception.Errors.UnauthorizedAccessException;
import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.dto.JobPostResponse;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.service.JobPostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobPostController {
    private final JobPostService jobPostService;
    private static final String DUMMY_TOKEN = "alumx-dev-token";
    // private static final Logger logger = Logger.getLogger(JobPostController.class.getName());

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<JobPostResponse>> getPostsByUser(@PathVariable Long userId) {
        List<JobPostResponse> posts = jobPostService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/job-posts")
    public ResponseEntity<?> createJobPost(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @Valid @RequestBody JobPostRequest request
    ) {
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

        JobPost savedPost = jobPostService.createJobPost(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Job post created successfully",
                "postId", savedPost.getPostId(),
                "username", savedPost.getUsername(),
                "createdAt", savedPost.getCreatedAt()
        ));
    }

    @PostMapping("/jobs/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        jobPostService.likePost(postId, userId);
        return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<?> deleteJobPost(
            @RequestHeader(value = "X-DUMMY-TOKEN", required = false) String token,
            @PathVariable Long jobId,
            @RequestParam Long userId
    ) {
        // Currently using request-based userId due to incomplete auth setup.
        // In production, userId should be extracted from SecurityContext/JWT token.
        if (token == null || !token.equals(DUMMY_TOKEN)) {
            throw new UnauthorizedAccessException("Unauthorized: Invalid or missing X-DUMMY-TOKEN header");
        }

        jobPostService.deletePostByUser(userId, jobId);
        return ResponseEntity.ok(Map.of("message", "Job post deleted successfully"));
    }
}
