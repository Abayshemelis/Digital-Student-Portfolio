package com.amazi.service;

import com.amazi.model.Submission;

/**
 * Service to handle the transition of Portfolios from Draft to Submitted.
 */
public class SubmissionService {

    /**
     * Finalizes the portfolio submission.
     * Logic: Changes status to SUBMITTED and prevents further edits.
     */
    public boolean submitPortfolio(Submission submission) {
        if (submission == null) return false;

        // 1. Logic Check: Only "DRAFT" can be submitted
        if (!"DRAFT".equalsIgnoreCase(submission.getStatus())) {
            System.err.println("Error: Portfolio is already " + submission.getStatus());
            return false;
        }

        // 2. State Change: Lock the status
        submission.setStatus("SUBMITTED");

        // TEMPORARY: Console Logging for verification
        System.out.println("--- PORTFOLIO SUBMITTED ---");
        System.out.println("Title:      " + submission.getTitle());
        System.out.println("Course:     " + submission.getCourse());
        System.out.println("Assignment: " + submission.getAssignment());
        System.out.println("Status:     " + submission.getStatus());
        System.out.println("---------------------------");

        return true; // simulate success
    }

    /**
     * Checks if a portfolio is currently in a state that allows editing.
     */
    public boolean canEdit(Submission submission) {
        return submission != null && "DRAFT".equalsIgnoreCase(submission.getStatus());
    }
}