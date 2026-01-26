package pl.fitnesstracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import pl.fitnesstracker.controller.IAddExerciseStrategy;
import pl.fitnesstracker.controller.WorkoutPlanModificationProcess;
import pl.fitnesstracker.dao.WorkoutPlanDao;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.PlanItem;
import pl.fitnesstracker.model.WorkoutPlan;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UseCaseWorkoutPlan {

    @Mock
    private FitnessModel fitnessModel;

    @Mock
    private WorkoutPlanDao workoutPlanDao;

    @Mock
    private IAddExerciseStrategy addExerciseStrategy;

    private WorkoutPlanModificationProcess process;

    @Before
    public void setUp() {
        // mockowanie getWorkoutPlanDao w FitnessModel
        when(fitnessModel.getWorkoutPlanDao()).thenReturn(workoutPlanDao);

        // tworzenie instancji klasy testowanej
        process = new WorkoutPlanModificationProcess(fitnessModel);

        // wstrzykiwanie mocka strategii
        process.setStrategy(addExerciseStrategy);
    }

    @Test
    public void testCreatePlan_Success() {
        int userId = 1;
        String planName = "Test Plan";
        String description = "Test Description";
        Integer expectedPlanId = 100;

        when(workoutPlanDao.createPlan(any(WorkoutPlan.class))).thenReturn(expectedPlanId);

        // tworzenie planu
        Integer result = process.createPlan(userId, planName, description);

        // asercje
        assertNotNull("Plan ID should not be null", result);
        assertEquals("Plan ID should match returned value", expectedPlanId, result);

        // weryfikacja czy DAO zostało wywołane z poprawnymi danymi
        verify(workoutPlanDao).createPlan(argThat(plan -> plan.getUserId() == userId &&
                plan.getPlanName().equals(planName) &&
                plan.getDescription().equals(description)));
    }

    @Test
    public void testAddExercise_Success() {
        Integer planId = 100;
        Integer exerciseId = 5;
        int sets = 3;
        int reps = 12;
        double weight = 50.0;
        Integer finalExerciseId = 5; // strategia zwraca to samo id

        // mockowanie strategii
        when(addExerciseStrategy.addExerciseToPlan(exerciseId)).thenReturn(finalExerciseId);

        // mockowanie DAO
        when(workoutPlanDao.addPlanItem(any(PlanItem.class))).thenReturn(true);

        // dodanie ćwiczenia
        boolean result = process.addExercise(planId, exerciseId, sets, reps, weight);

        // asercja
        assertTrue("Add exercise should return true", result);

        // weryfikacja
        verify(addExerciseStrategy).addExerciseToPlan(exerciseId);
        verify(workoutPlanDao).addPlanItem(argThat(item -> item.getPlanId().equals(planId) &&
                item.getExerciseId().equals(finalExerciseId) &&
                item.getSets() == sets &&
                item.getReps() == reps &&
                item.getWeight().doubleValue() == weight));
    }

    @Test
    public void testAddExercise_StrategyFails() {
        Integer planId = 100;
        Integer exerciseId = 5;

        // symulacja błędu strategii
        when(addExerciseStrategy.addExerciseToPlan(exerciseId)).thenThrow(new RuntimeException("Strategy failed"));

        // dodanie ćwiczenia
        boolean result = process.addExercise(planId, exerciseId, 3, 12, 50.0);

        // asercja
        assertFalse("Should return false when strategy fails", result);

        // weryfikacja, że DAO nie zostało wywołane
        verify(workoutPlanDao, never()).addPlanItem(any(PlanItem.class));
    }

    @Test
    public void testAddExercise_DaoFails() {
        Integer planId = 100;
        Integer exerciseId = 5;

        // symulacja błędu dao
        when(addExerciseStrategy.addExerciseToPlan(exerciseId)).thenReturn(exerciseId);
        when(workoutPlanDao.addPlanItem(any(PlanItem.class))).thenReturn(false);

        // dodanie ćwiczenia
        boolean result = process.addExercise(planId, exerciseId, 3, 12, 50.0);

        // asercja
        assertFalse("Should return false when DAO returns false", result);
    }
}