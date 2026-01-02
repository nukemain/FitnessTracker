package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.Exercise;
import pl.fitnesstracker.model.FitnessModel;

public class NewExStrategy implements IAddExerciseStrategy {

    private final FitnessModel model;
    private final Exercise newExerciseData;

    public NewExStrategy(FitnessModel model, Exercise newExerciseData) {
        this.model = model;
        this.newExerciseData = newExerciseData;
    }

    @Override
    public Integer addExerciseToPlan(int ignoredId) {
        System.out.println("[Strategy] Zapisywanie nowego ćwiczenia: '" + newExerciseData.getName() + "'");
        System.out.println("[Strategy] Opis: " + newExerciseData.getDescription());

        Integer newId = model.getExerciseDao().addCustomExercise(newExerciseData);

        if (newId == null) {
            throw new RuntimeException("Błąd bazy danych: Nie udało się utworzyć ćwiczenia.");
        }

        return newId;
    }
}