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

        // 2. NEW: Validation for the new fields before finalizing
        if (submission.getEmail() == null || !submission.getEmail().contains("@")) {
            System.err.println("Error: Invalid organization email.");
            return false;
        }

        // 3. State Change: Lock the status
        submission.setStatus("SUBMITTED");

        // UPDATED: Console Logging including new fields
        System.out.println("--- PORTFOLIO SUBMITTED ---");
        System.out.println("Title:        " + submission.getTitle());
        System.out.println("Organization: " + submission.getOrganizationName()); // New
        System.out.println("Org Email:    " + submission.getEmail());           // New
        System.out.println("Course:       " + submission.getCourse());
        System.out.println("Status:       " + submission.getStatus());
        System.out.println("---------------------------");

        return true;
    }

    /**
     * Checks if a portfolio is currently in a state that allows editing.
     */
    public boolean canEdit(Submission submission) {
        return submission != null && "DRAFT".equalsIgnoreCase(submission.getStatus());
    }
}