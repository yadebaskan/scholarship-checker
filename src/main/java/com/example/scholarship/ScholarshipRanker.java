package com.example.scholarship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScholarshipRanker {

    private final ScholarshipEvaluator evaluator = new ScholarshipEvaluator();

    private record Ranked(Application app, int score, double perCapita) {
    }

    /**
     * Returns at most {@code quota} eligible applications, best first.
     * Order: higher score, then lower income per person, then higher GPA.
     * Rejected applications are never selected. The input list is not modified.
     */
    public List<Application> selectTop(List<Application> applications, int quota) {
        if (applications == null) {
            throw new IllegalArgumentException("Applications must not be null");
        }
        if (quota < 0) {
            throw new IllegalArgumentException("Quota must not be negative");
        }

        List<Ranked> eligible = new ArrayList<>();
        for (Application app : applications) {
            EligibilityResult result = evaluator.evaluate(app);
            if (result.decision() != ScholarshipDecision.REJECTED) {
                double perCapita = app.monthlyIncome() / app.householdSize();
                eligible.add(new Ranked(app, result.score(), perCapita));
            }
        }

        Comparator<Ranked> order = Comparator
                .comparingInt(Ranked::score).reversed()
                .thenComparingDouble(Ranked::perCapita)
                .thenComparing(Comparator.comparingDouble((Ranked r) -> r.app().gpa()).reversed());
        eligible.sort(order);

        List<Application> selected = new ArrayList<>();
        int limit = Math.min(quota, eligible.size());
        for (int i = 0; i < limit; i++) {
            selected.add(eligible.get(i).app());
        }
        return selected;
    }
}