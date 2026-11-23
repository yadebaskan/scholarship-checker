package com.example.scholarship;

public class ScholarshipEvaluator {

    public static final double MAX_PER_CAPITA_INCOME = 8000.0;
    public static final double MIN_GPA_FIRST_YEAR = 2.00;
    public static final double MIN_GPA_OTHER_YEARS = 2.50;
    public static final int FULL_SCORE_THRESHOLD = 80;
    public static final int HALF_SCORE_THRESHOLD = 60;
    public static final int ORPHAN_BONUS = 5;
    public static final int DISABLED_BONUS = 5;
    public static final int MAX_SCORE = 100;

    private static final double MAX_GPA = 4.0;
    private static final int MAX_GPA_POINTS = 50;
    private static final int MAX_INCOME_POINTS = 40;
    private static final int POINTS_PER_SIBLING = 3;
    private static final int MAX_SIBLING_POINTS = 10;

    /**
     * Evaluates a scholarship application against the eligibility rules.
     * Rules are checked in order: disciplinary record, other scholarship,
     * minimum GPA, income per person, then the score decides FULL/HALF/REJECTED.
     */
    public EligibilityResult evaluate(Application app) {
        if (app == null) {
            throw new IllegalArgumentException("Application must not be null");
        }
        if (app.disciplinaryRecord()) {
            return reject("Disciplinary record");
        }
        if (app.hasOtherScholarship()) {
            return reject("Already receives another scholarship");
        }
        double minGpa = (app.year() == 1) ? MIN_GPA_FIRST_YEAR : MIN_GPA_OTHER_YEARS;
        if (app.gpa() < minGpa) {
            return reject("GPA below minimum");
        }
        double perCapita = app.monthlyIncome() / app.householdSize();
        if (perCapita > MAX_PER_CAPITA_INCOME) {
            return reject("Income per person too high");
        }

        int bonus = (app.orphan() ? ORPHAN_BONUS : 0) + (app.disabled() ? DISABLED_BONUS : 0);
        int score = Math.min(MAX_SCORE, calculateScore(app.gpa(), perCapita, app.siblings()) + bonus);
        if (score >= FULL_SCORE_THRESHOLD) {
            return new EligibilityResult(ScholarshipDecision.FULL, score, "Eligible");
        } else if (score >= HALF_SCORE_THRESHOLD) {
            return new EligibilityResult(ScholarshipDecision.HALF, score, "Eligible");
        }
        return new EligibilityResult(ScholarshipDecision.REJECTED, score, "Score below 60");
    }

    /** Score = GPA points (0-50) + income points (0-40) + sibling points (0-10). */
    public int calculateScore(double gpa, double perCapitaIncome, int siblings) {
        int gpaPoints = (int) Math.round(gpa / MAX_GPA * MAX_GPA_POINTS);
        int incomePoints = (int) Math.round((1 - perCapitaIncome / MAX_PER_CAPITA_INCOME) * MAX_INCOME_POINTS);
        int siblingPoints = Math.min(siblings * POINTS_PER_SIBLING, MAX_SIBLING_POINTS);
        return gpaPoints + incomePoints + siblingPoints;
    }

    private EligibilityResult reject(String reason) {
        return new EligibilityResult(ScholarshipDecision.REJECTED, 0, reason);
    }
}