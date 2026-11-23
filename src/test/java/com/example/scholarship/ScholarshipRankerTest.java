package com.example.scholarship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Timeout;

class ScholarshipRankerTest {

    private ScholarshipRanker ranker;

    // score 80, income per person 2000
    private final Application a = new Application(4.0, 2000, 1, 0, 2, false, false);
    // score 79
    private final Application b = new Application(4.0, 2200, 1, 0, 2, false, false);
    // score 60
    private final Application c = new Application(2.8, 3000, 1, 0, 2, false, false);
    // rejected (disciplinary record)
    private final Application rejected = new Application(4.0, 2000, 1, 0, 2, true, false);

    @BeforeEach
    void setUp() {
        ranker = new ScholarshipRanker();
    }

    @Test
    @DisplayName("TC27: Null list is rejected")
    void nullListThrows() {
        assertThrows(IllegalArgumentException.class, () -> ranker.selectTop(null, 1));
    }

    @Test
    @DisplayName("TC28: Negative quota is rejected")
    void negativeQuotaThrows() {
        assertThrows(IllegalArgumentException.class, () -> ranker.selectTop(List.of(a), -1));
    }

    @Test
    @DisplayName("TC29: Empty list gives empty result")
    void emptyList() {
        assertTrue(ranker.selectTop(List.of(), 5).isEmpty());
    }

    @Test
    @DisplayName("TC30: Quota 0 gives empty result")
    void quotaZero() {
        assertTrue(ranker.selectTop(List.of(a, b), 0).isEmpty());
    }

    @Test
    @DisplayName("TC31: Rejected applications are never selected")
    void rejectedExcluded() {
        assertEquals(List.of(a), ranker.selectTop(List.of(rejected, a), 5));
    }

    @Test
    @DisplayName("TC32: Result is sorted by score, best first")
    void sortedByScore() {
        assertEquals(List.of(a, b, c), ranker.selectTop(List.of(c, a, b), 3));
    }

    @Test
    @DisplayName("TC33: Quota smaller than eligible count keeps the best")
    void quotaSmallerThanEligible() {
        assertEquals(List.of(a, b), ranker.selectTop(List.of(c, a, b), 2));
    }

    @Test
    @DisplayName("TC34: Quota larger than eligible count returns all eligible")
    void quotaLargerThanEligible() {
        assertEquals(3, ranker.selectTop(List.of(a, b, c, rejected), 10).size());
    }

    @Test
    @DisplayName("TC35: Equal score -> lower income per person first")
    void tieBrokenByIncome() {
        Application poorer = new Application(3.6, 1000, 1, 0, 2, false, false); // score 80, income 1000
        assertEquals(List.of(poorer, a), ranker.selectTop(List.of(a, poorer), 2));
    }

    @Test
    @DisplayName("TC36: Equal score and income -> higher GPA first")
    void tieBrokenByGpa() {
        Application lowerGpa = new Application(3.6, 2000, 1, 0, 2, false, false, true, false); // 45+30+5 = 80
        assertEquals(List.of(a, lowerGpa), ranker.selectTop(List.of(lowerGpa, a), 2));
    }

    @Test
    @DisplayName("TC37: A null element in the list is rejected")
    void nullElementThrows() {
        List<Application> list = Arrays.asList(a, null);
        assertThrows(IllegalArgumentException.class, () -> ranker.selectTop(list, 2));
    }

    @Test
    @DisplayName("TC38: The input list is not modified")
    void inputNotModified() {
        List<Application> input = new ArrayList<>(List.of(c, a, b));
        ranker.selectTop(input, 3);
        assertEquals(List.of(c, a, b), input);
    }
    @Test
    @DisplayName("TC39: Tie-break uses income per person (income / household size)")
    void tieBrokenByIncomePerPerson() {
        // score 80, income per person 4000 / 4 = 1000
        Application largeHousehold = new Application(3.6, 4000, 4, 0, 2, false, false);
        // 'a' has score 80, income per person 2000 / 1 = 2000
        assertEquals(List.of(largeHousehold, a), ranker.selectTop(List.of(a, largeHousehold), 2));
    }
    @Test
    @DisplayName("TC41: Complete tie keeps the input order")
    void completeTieKeepsInputOrder() {
        // both: score 80, same income per person, same GPA
        Application x = new Application(3.6, 2000, 1, 0, 2, false, false, true, false);
        Application y = new Application(3.6, 2000, 1, 0, 2, false, false, false, true);
        assertEquals(List.of(x, y), ranker.selectTop(List.of(x, y), 2));
        assertEquals(List.of(y, x), ranker.selectTop(List.of(y, x), 2));
    }

    @Test
    @Timeout(2)
    @DisplayName("TC42: 10,000 applications are ranked within 2 seconds")
    void largeListPerformance() {
        List<Application> many = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            double gpa = 2.5 + (i % 16) * 0.1;
            many.add(new Application(gpa, 1000 + (i % 50) * 100, 1 + i % 4, i % 5, 2, false, false));
        }
        List<Application> top = ranker.selectTop(many, 100);
        assertEquals(100, top.size());
    }
}
