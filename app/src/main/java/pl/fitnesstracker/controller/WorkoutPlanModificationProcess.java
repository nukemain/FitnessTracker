package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.PlanItem;
import java.math.BigDecimal;

public class WorkoutPlanModificationProcess {
    private final FitnessModel model;
    private IAddExerciseStrategy strategy;

    public WorkoutPlanModificationProcess(FitnessModel model) {
        this.model = model;
        this.strategy = new ExistingExStrategy(model);
    }

    public Integer createPlan(int userId, String name, String description) {
        pl.fitnesstracker.model.WorkoutPlan plan = new pl.fitnesstracker.model.WorkoutPlan();
        plan.setUserId(userId);
        plan.setPlanName(name);
        plan.setDescription(description);
        return model.getWorkoutPlanDao().createPlan(plan);
    }

    public boolean addExercise(Integer planId, Integer exerciseId, int sets, int reps, double weight) {
        try {
            Integer finalExerciseId = strategy.addExerciseToPlan(exerciseId);

            PlanItem item = new PlanItem(planId, finalExerciseId, sets, reps, BigDecimal.valueOf(weight));
            return model.getWorkoutPlanDao().addPlanItem(item);

        } catch (Exception e) {
            System.out.println("Błąd procesu dodawania ćwiczenia: " + e.getMessage());
            return false;
        }
    }

    public void setStrategy(IAddExerciseStrategy strategy) {
        this.strategy = strategy;
    }
}