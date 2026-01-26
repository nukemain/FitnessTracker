package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.StatisticsDao;
import pl.fitnesstracker.model.ExerciseStatsDTO;
import pl.fitnesstracker.model.Statistics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryStatisticsDao extends StatisticsDao {

    @Override
    public Statistics getUserStatistics(Integer userId) {
        Statistics stats = new Statistics();
        stats.setUserId(userId);
        stats.setTotalWorkouts(5);
        stats.setTotalReps(500);
        stats.setCaloriesBurned(1200);
        stats.setTotalRecords(25);
        stats.setMaxWeight(new BigDecimal("100.0"));
        return stats;
    }

    @Override
    public List<ExerciseStatsDTO> getExerciseStats(int userId) {
        return new ArrayList<>();
    }

    @Override
    public Optional<ExerciseStatsDTO> getStatsForExercise(int userId, int exerciseId) {
        return Optional.empty();
    }
}
