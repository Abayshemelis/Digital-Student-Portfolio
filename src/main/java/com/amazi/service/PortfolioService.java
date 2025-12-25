package com.amazi.service;

import com.amazi.model.Portfolio;

public class PortfolioService {

    public void saveDraft(Portfolio portfolio) {
        // Later: save to database
        System.out.println("Saved portfolio: " + portfolio.getTitle());
    }
}
