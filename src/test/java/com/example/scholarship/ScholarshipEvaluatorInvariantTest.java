package com.example.scholarship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScholarshipEvaluatorInvariantTest {

    private final ScholarshipEvaluator evaluator = new ScholarshipEvaluator();

    @Test
    @DisplayName("TC40: Invariants hold for ~32,000 generated inputs")
    void invariantsHoldOverInputGrid() {
        double[] gpas = {0.0, 1.99, 2.0, 2.49, 2.5, 3.0, 3.5, 4.0};
        double[] incomes = {0, 1000, 4000, 8000, 8001, 16000, 100000};
        int[] households = {1, 2, 5};
        int[] siblings = {0, 1, 4, 10};
        int[] years = {1, 2, 4};
        boolean[] flags = {false, true};

        for (double gpa : gpas)
            for (double income : incomes)
                for (int h : households)
                    for (int s : siblings)
                        for (int y : years)
                            for (boolean disc : flags)
                                for (boolean other : flags)
                                    for (boolean orphan : flags)
                                        for (boolean disabled : flags) {
                                            Application app = new Application(gpa, income, h, s, y, disc, other, orphan, disabled);
                                            EligibilityResult r = evaluator.evaluate(app);
                                            String ctx = app.toString();

                                            assertTrue(r.score() >= 0 && r.score() <= 100, ctx);
                                            switch (r.decision()) {
                                                case FULL -> assertTrue(r.score() >= 80, ctx);
                                                case HALF -> assertTrue(r.score() >= 60 && r.score() < 80, ctx);
                                                case REJECTED -> {
                                                    if (r.reason().equals("Score below 60")) {
                                                        assertTrue(r.score() < 60, ctx);
                                                    } else {
                                                        assertEquals(0, r.score(), ctx);
                                                    }
                                                }
                                            }
                                            if (disc || other) {
                                                assertEquals(ScholarshipDecision.REJECTED, r.decision(), ctx);
                                            }
                                            if (!orphan) {
                                                Application withBonus = new Application(gpa, income, h, s, y, disc, other, true, disabled);
                                                assertTrue(evaluator.evaluate(withBonus).score() >= r.score(), ctx);
                                            }
                                        }
    }
}