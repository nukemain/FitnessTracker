package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.ExerciseDao;
import pl.fitnesstracker.model.Exercise;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryExerciseDao extends ExerciseDao {
    private final List<Exercise> exercises = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(500);

    @Override
    public boolean exerciseExists(int exerciseId) {
        return exercises.stream().anyMatch(e -> e.getId().equals(exerciseId));
    }

    @Override
    public Integer addCustomExercise(Exercise exercise) {
        exercise.setId(idGenerator.incrementAndGet());
        exercises.add(exercise);
        return exercise.getId();
    }

    @Override
    public List<Exercise> getAllExercises(Integer userId) {
        return exercises.stream()
                .filter(e -> e.getUserId() == null || e.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
