package com.example.scholarship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ScholarshipEvaluatorTest {

    private ScholarshipEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new ScholarshipEvaluator();
    }

    @Nested
    @DisplayName("Equivalence partitioning")
    class EquivalencePartitioning {

        @Test
        @DisplayName("TC01: High GPA and low income -> FULL, score 80")
        void fullScholarship() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2000, 1, 0, 2, false, false));
            assertEquals(ScholarshipDecision.FULL, r.decision());
            assertEquals(80, r.score());
        }

        @Test
        @DisplayName("TC03: Disciplinary record -> REJECTED")
        void disciplinaryRecordRejected() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2000, 1, 0, 2, true, false));
            assertEquals(ScholarshipDecision.REJECTED, r.decision());
        }

        @Test
        @DisplayName("TC15: Income is divided by household size")
        void incomeIsPerPerson() {
            EligibilityResult low = evaluator.evaluate(new Application(4.0, 6000, 3, 0, 2, false, false));
            EligibilityResult high = evaluator.evaluate(new Application(4.0, 16000, 2, 0, 2, false, false));
            assertEquals(ScholarshipDecision.FULL, low.decision());
            assertEquals(80, low.score());
            assertEquals(50, high.score());
            assertEquals("Score below 60", high.reason());
        }

        @Test
        @DisplayName("TC16: Same GPA 2.2 passes in year 1 but fails in year 3")
        void minimumGpaDependsOnYear() {
            EligibilityResult first = evaluator.evaluate(new Application(2.2, 0, 1, 0, 1, false, false));
            EligibilityResult third = evaluator.evaluate(new Application(2.2, 0, 1, 0, 3, false, false));
            assertEquals(ScholarshipDecision.HALF, first.decision());
            assertEquals("GPA below minimum", third.reason());
        }

        @Test
        @DisplayName("TC20: Rejected by rule gives score 0")
        void ruleRejectionHasZeroScore() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2000, 1, 0, 2, false, true));
            assertEquals(0, r.score());
            assertEquals(ScholarshipDecision.REJECTED, r.decision());
        }

        @Test
        @DisplayName("TC26: Short constructor defaults orphan and disabled to false")
        void shortConstructorDefaults() {
            Application a = new Application(3.0, 1000, 1, 0, 2, false, false);
            assertFalse(a.orphan());
            assertFalse(a.disabled());
        }
    }

    @Nested
    @DisplayName("Boundary value analysis")
    class BoundaryValueAnalysis {

        @Test
        @DisplayName("TC02: Score 79 (just below FULL) -> HALF")
        void scoreJustBelowFull() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2200, 1, 0, 2, false, false));
            assertEquals(79, r.score());
            assertEquals(ScholarshipDecision.HALF, r.decision());
        }

        @Test
        @DisplayName("TC04: 1st year GPA 1.99 -> REJECTED, 2.00 -> not rejected for GPA")
        void firstYearGpaBoundary() {
            EligibilityResult below = evaluator.evaluate(new Application(1.99, 0, 1, 0, 1, false, false));
            EligibilityResult at = evaluator.evaluate(new Application(2.00, 0, 1, 0, 1, false, false));
            assertEquals("GPA below minimum", below.reason());
            assertEquals(ScholarshipDecision.HALF, at.decision());
        }

        @ParameterizedTest(name = "TC06: year 2, GPA {0}")
        @CsvSource({"2.49, true", "2.50, false", "2.51, false"})
        @DisplayName("TC06: GPA boundary for years 2-4 (min 2.50)")
        void gpaBoundaryUpperYears(double gpa, boolean rejectedForGpa) {
            EligibilityResult r = evaluator.evaluate(new Application(gpa, 0, 1, 0, 2, false, false));
            assertEquals(rejectedForGpa, "GPA below minimum".equals(r.reason()));
        }

        @ParameterizedTest(name = "TC07: income per person {0}")
        @CsvSource({
                "7999.99, 'Score below 60'",
                "8000.00, 'Score below 60'",
                "8000.01, 'Income per person too high'"})
        @DisplayName("TC07: Income-per-person boundary (max 8000)")
        void incomeBoundary(double income, String expectedReason) {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, income, 1, 0, 2, false, false));
            assertEquals(expectedReason, r.reason());
        }

        @ParameterizedTest(name = "TC08: GPA {0}, income {1} -> score {2}")
        @CsvSource({"2.8, 3000, 60, HALF", "2.8, 3200, 59, REJECTED"})
        @DisplayName("TC08: Score boundary 59/60")
        void scoreBoundaryHalf(double gpa, double income, int score, ScholarshipDecision decision) {
            EligibilityResult r = evaluator.evaluate(new Application(gpa, income, 1, 0, 2, false, false));
            assertEquals(score, r.score());
            assertEquals(decision, r.decision());
        }

        @ParameterizedTest(name = "TC09: {0} siblings -> {1} points")
        @CsvSource({"0, 0", "1, 3", "3, 9", "4, 10", "5, 10"})
        @DisplayName("TC09: Sibling points are capped at 10")
        void siblingPointsCap(int siblings, int expectedPoints) {
            assertEquals(expectedPoints, evaluator.calculateScore(0.0, 8000.0, siblings));
        }

        @Test
        @DisplayName("TC18: Maximum possible score is 100")
        void maximumScore() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 0, 1, 4, 2, false, false));
            assertEquals(100, r.score());
            assertEquals(ScholarshipDecision.FULL, r.decision());
        }

        @Test
        @DisplayName("TC19: GPA points are rounded half up (3.0 -> 37.5 -> 38)")
        void gpaPointsRounding() {
            assertEquals(38, evaluator.calculateScore(3.0, 8000.0, 0));
        }
    }

    @Nested
    @DisplayName("Decision table")
    class DecisionTable {

        @ParameterizedTest(name = "TC10: disc={0}, other={1}, gpa={2}, income={3} -> {4}")
        @CsvSource({
                "true,  false, 4.0, 2000, 'Disciplinary record'",
                "false, true,  4.0, 2000, 'Already receives another scholarship'",
                "false, false, 1.0, 2000, 'GPA below minimum'",
                "false, false, 4.0, 9000, 'Income per person too high'",
                "false, false, 4.0, 2000, 'Eligible'",
                "true,  true,  4.0, 2000, 'Disciplinary record'"})
        @DisplayName("TC10: Decision table of rejection rules")
        void decisionTable(boolean disc, boolean other, double gpa, double income, String reason) {
            EligibilityResult r = evaluator.evaluate(new Application(gpa, income, 1, 0, 2, disc, other));
            assertEquals(reason, r.reason());
        }
    }

    @Nested
    @DisplayName("Scoring and bonus rules")
    class ScoringAndBonus {

        @Test
        @DisplayName("TC17: One sibling lifts score 79 to 82 -> FULL")
        void siblingChangesDecision() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2200, 1, 1, 2, false, false));
            assertEquals(82, r.score());
            assertEquals(ScholarshipDecision.FULL, r.decision());
        }

        @Test
        @DisplayName("TC21: Orphan bonus lifts 79 to 84 -> FULL")
        void orphanBonus() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2200, 1, 0, 2, false, false, true, false));
            assertEquals(84, r.score());
            assertEquals(ScholarshipDecision.FULL, r.decision());
        }

        @Test
        @DisplayName("TC22: Disability bonus lifts 59 to 64 -> HALF")
        void disabledBonus() {
            EligibilityResult r = evaluator.evaluate(new Application(2.8, 3200, 1, 0, 2, false, false, false, true));
            assertEquals(64, r.score());
            assertEquals(ScholarshipDecision.HALF, r.decision());
        }

        @Test
        @DisplayName("TC23: Both bonuses reach 80 (FULL), one bonus gives 75 (HALF)")
        void bonusesCombine() {
            EligibilityResult both = evaluator.evaluate(new Application(2.8, 1000, 1, 0, 2, false, false, true, true));
            EligibilityResult one = evaluator.evaluate(new Application(2.8, 1000, 1, 0, 2, false, false, true, false));
            assertEquals(80, both.score());
            assertEquals(ScholarshipDecision.FULL, both.decision());
            assertEquals(75, one.score());
            assertEquals(ScholarshipDecision.HALF, one.decision());
        }

        @Test
        @DisplayName("TC24: Score is capped at 100 even with bonuses")
        void scoreCappedAtMax() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 0, 1, 4, 2, false, false, true, true));
            assertEquals(100, r.score());
        }

        @Test
        @DisplayName("TC25: Bonus does not override a rejection rule")
        void bonusDoesNotOverrideRules() {
            EligibilityResult r = evaluator.evaluate(new Application(4.0, 2000, 1, 0, 2, true, false, true, true));
            assertEquals(ScholarshipDecision.REJECTED, r.decision());
            assertEquals(0, r.score());
        }
    }

    @Nested
    @DisplayName("Negative testing")
    class NegativeTesting {

        @Test
        @DisplayName("TC05: GPA 4.5 is invalid -> exception")
        void invalidGpaThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Application(4.5, 2000, 1, 0, 2, false, false));
        }

        @Test
        @DisplayName("TC11: Invalid application fields are rejected")
        void invalidFieldsThrow() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(-0.01, 1000, 1, 0, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(4.01, 1000, 1, 0, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, -1, 1, 0, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, 1000, 0, 0, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, 1000, 1, -1, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, 1000, 1, 0, 0, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, 1000, 1, 0, 5, false, false))
            );
        }

        @Test
        @DisplayName("TC12: Null application is rejected")
        void nullApplicationThrows() {
            assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(null));
        }

        @Test
        @DisplayName("TC13: GPA NaN must be rejected")
        void nanGpaThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Application(Double.NaN, 1000, 1, 0, 2, false, false));
        }

        @Test
        @DisplayName("TC14: Income NaN or infinite must be rejected")
        void invalidIncomeThrows() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, Double.NaN, 1, 0, 2, false, false)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new Application(3.0, Double.POSITIVE_INFINITY, 1, 0, 2, false, false))
            );
        }
    }
}