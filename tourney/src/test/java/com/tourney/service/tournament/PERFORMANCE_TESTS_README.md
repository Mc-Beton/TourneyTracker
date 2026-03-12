# Pairing Algorithm Performance Tests

## Overview

Comprehensive performance test suite comparing **CP-SAT** and **Backtracking** pairing algorithms for tournament management.

## Test Matrix

### Algorithms

- **CP-SAT**: Google OR-Tools constraint solver (optimal solutions)
- **Backtracking**: Custom recursive algorithm (first valid solution)

### Tournament Sizes

- 16 players
- 32 players
- 64 players

### Tournament Rounds

- Round 2 (after 1 completed round)
- Round 3 (after 2 completed rounds)

### Total Scenarios: **12 tests** (2 algorithms × 3 sizes × 2 rounds)

## Test Data Setup

### Random Distribution

- **Teams**: Players randomly assigned to Team A or Team B
- **Cities**: Players randomly assigned to City A, B, C, or D
- **Previous Rounds**: Random pairings and match results generated
- **Fixed Seed**: Uses `Random(42)` for reproducible results

### Constraints Tested

- ✅ No rematches (hard constraint)
- ✅ Avoid same team pairings (soft constraint)
- ✅ Avoid same city pairings (soft constraint)
- ✅ Score-based ranking maintained

## Performance Metrics

### Measured

1. **Execution Time** (milliseconds) - Wall-clock time
2. **CPU Time** (milliseconds) - Thread CPU time used
3. **Min/Max/Avg** - Statistical distribution
4. **Standard Deviation** - Time consistency

### Methodology

- **Warmup Runs**: 3 iterations (results discarded)
- **Measurement Runs**: 5 iterations (results averaged)
- **GC Control**: Forced garbage collection between measurements
- **100ms delay** between measurements for JVM stabilization

## Running the Tests

### Single Test Class

```bash
cd TourneyTracker
mvn test -Dtest=PairingAlgorithmPerformanceTest
```

### Specific Test (e.g., 32 players, Round 2, Backtracking)

```bash
mvn test -Dtest=PairingAlgorithmPerformanceTest#testBacktracking_32Players_Round2
```

### All Tests in Order

```bash
mvn test -Dtest=PairingAlgorithmPerformanceTest
```

## Output

### Console Output

```
>>> Testing BACKTRACKING with 16 players, Round 2
Warmup: ...
Measurement: ✓✓✓✓✓
Result: Avg=45.20ms, Min=42.00ms, Max=51.00ms, CPU=44.50ms
```

### CSV Report

After all tests complete, a CSV file is generated:

```
pairing_performance_results.csv
```

#### CSV Format

```csv
Algorithm,Players,Round,AvgTime_ms,MinTime_ms,MaxTime_ms,StdDev_ms,AvgCPU_ms,Success,Runs
BACKTRACKING,16,2,45.20,42.00,51.00,3.12,44.50,TRUE,5
CP_SAT,16,2,125.80,118.00,142.00,8.45,124.20,TRUE,5
...
```

### Import to Excel

1. Open Excel
2. **Data** → **From Text/CSV**
3. Select `pairing_performance_results.csv`
4. Create pivot tables and charts

## Expected Results

### Performance Characteristics

#### Backtracking Algorithm

- **Fast** for small tournaments (16-32 players)
- **Exponential complexity** - May struggle with 64+ players
- **Consistent times** - Low standard deviation
- **First valid solution** - Not necessarily optimal

#### CP-SAT Algorithm

- **Slower startup** - OR-Tools initialization overhead
- **Scales better** - Handles 64+ players efficiently
- **Variable times** - Depends on constraint complexity
- **Optimal solutions** - Minimizes soft constraint violations
- **10-second timeout** - May fail on very complex scenarios

### Round Complexity

- **Round 2**: More pairing options (fewer rematches to avoid)
- **Round 3**: Fewer options (more rematch constraints)

## Validation

Each test validates:

1. ✅ All players paired (accounting for BYE)
2. ✅ No duplicate pairings in current round
3. ✅ No rematches from previous rounds
4. ✅ Valid match structure

## Troubleshooting

### CP-SAT Fails

- **Cause**: OR-Tools not installed or library load failure
- **Solution**: Install OR-Tools native libraries

```bash
pip install ortools
```

### Timeout Errors

- **Cause**: Algorithm taking too long (especially CP-SAT on complex scenarios)
- **Solution**: Increase timeout in `CPSATPairingService.java` (line ~218)

### Out of Memory

- **Cause**: Too many test runs or large tournament sizes
- **Solution**: Increase JVM heap size

```bash
export MAVEN_OPTS="-Xmx2g"
mvn test
```

## Extending Tests

### Add More Tournament Sizes

Add new test methods in `PairingAlgorithmPerformanceTest.java`:

```java
@Test
@Order(13)
void testBacktracking_128Players_Round2() {
    runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 2);
}
```

### Test Different Constraints

Modify `createRoundDefinitions()` method:

```java
def.setAvoidSameTeamPairing(false);  // Disable team constraint
def.setAvoidSameCityPairing(false);  // Disable city constraint
```

### Adjust Measurement Runs

Modify constants at top of test class:

```java
private static final int WARMUP_RUNS = 5;
private static final int MEASUREMENT_RUNS = 10;
```

## Performance Optimization Tips

### For Backtracking

- Ensure proper candidate sorting (non-conflicting pairs first)
- Minimize list copying in recursion
- Consider memoization for repeated states

### For CP-SAT

- Adjust penalty weights (`SAME_TEAM_PENALTY`, `SAME_CITY_PENALTY`)
- Reduce timeout for faster failures
- Profile constraint complexity

## Notes

- **Fixed Random Seed**: Tests use `Random(42)` for reproducibility
- **Not Database Tests**: All data mocked in-memory
- **JIT Impact**: First few runs may be slower (warmup mitigates this)
- **CPU Time**: May not be supported on all JVMs (warning displayed)

---

**Generated by**: TourneyTracker Performance Testing Suite  
**Last Updated**: March 10, 2026
