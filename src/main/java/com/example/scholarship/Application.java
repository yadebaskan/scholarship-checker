package com.example.scholarship;

public record Application(
        double gpa,
        double monthlyIncome,
        int householdSize,
        int siblings,
        int year,
        boolean disciplinaryRecord,
        boolean hasOtherScholarship,
        boolean orphan,
        boolean disabled) {

    public Application(double gpa, double monthlyIncome, int householdSize, int siblings,
                       int year, boolean disciplinaryRecord, boolean hasOtherScholarship) {
        this(gpa, monthlyIncome, householdSize, siblings, year,
                disciplinaryRecord, hasOtherScholarship, false, false);
    }

    public Application {
        if (Double.isNaN(gpa) || gpa < 0.0 || gpa > 4.0) {
            throw new IllegalArgumentException("GPA must be between 0.00 and 4.00");
        }
        if (Double.isNaN(monthlyIncome) || Double.isInfinite(monthlyIncome) || monthlyIncome < 0) {
            throw new IllegalArgumentException("Income must be a finite, non-negative number");
        }
        if (householdSize < 1) {
            throw new IllegalArgumentException("Household size must be at least 1");
        }
        if (siblings < 0) {
            throw new IllegalArgumentException("Siblings must not be negative");
        }
        if (year < 1 || year > 4) {
            throw new IllegalArgumentException("Year must be between 1 and 4");
        }
    }
}