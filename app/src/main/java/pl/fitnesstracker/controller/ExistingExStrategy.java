package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.FitnessModel;

public class ExistingExStrategy implements IAddExerciseStrategy {

    private final FitnessModel model;

    public ExistingExStrategy(FitnessModel model) {
        this.model = model;
    }

    @Override
    public Integer addExerciseToPlan(int exerciseId) {
        System.out.println("[Strategy] Weryfikacja istniejącego ćwiczenia ID: " + exerciseId);

        boolean exists = model.getExerciseDao().exerciseExists(exerciseId);

        if (exists) {
            return exerciseId;
        } else {
            throw new IllegalArgumentException("BŁĄD: Ćwiczenie o ID " + exerciseId + " nie istnieje w bazie!");
        }
    }
}