# Scholarship Eligibility Checker

Small Java application (no GUI, no database) that decides whether a student
scholarship application is eligible (FULL / HALF / REJECTED), and ranks
eligible applications by a quota.

## Requirements
- JDK 25 (Temurin), Maven (bundled with IntelliJ IDEA)

## Build and test
- Run unit tests:                 mvn test
- Tests + JaCoCo coverage report: mvn clean verify   (target/site/jacoco/index.html)
- Mutation testing (PIT):         mvn clean test-compile org.pitest:pitest-maven:mutationCoverage
  (target/pit-reports/.../index.html)

## Structure
- src/main/java/com/example/scholarship: Application, ScholarshipEvaluator,
  ScholarshipRanker, EligibilityResult, ScholarshipDecision
- src/test/java/com/example/scholarship: JUnit 5 tests (53 test cases)