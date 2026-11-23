package com.example.scholarship;

public record EligibilityResult(ScholarshipDecision decision, int score, String reason) {
}