package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.*;
import java.math.BigDecimal;
import java.util.List;

public interface IFitnessSystemController {
    // Logowanie i Użytkownik
    boolean login(String email, String password);
    boolean register(String email, String password, BigDecimal weight, Integer height, String goal);
    void logout();
    User getCurrentUser();

    // Plany Treningowe
    Integer createWorkoutPlan(String name, String description);
    List<WorkoutPlan> getUserWorkoutPlans();
    boolean addExerciseToPlan(Integer planId, Integer exerciseId, int sets, int reps, double weight);
    List<Exercise> getAvailableExercises();

    // Sesja Treningowa
    Integer startSession(Integer planId);
    void endSession(String duration); // duration np. "01:30:00"
    boolean logSet(Integer exerciseId, int sets, int reps, double weight);
    void addNoteToSession(String content);

    // Statystyki i Inne
    Statistics getUserStatistics();
    List<Notification> getUserNotifications();
}