package pl.fitnesstracker.fitnesse;

import fit.ColumnFixture;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.Exercise;

import java.util.List;

public class TestExerciseManagement extends ColumnFixture {
    public String name;
    public String description;
    public String category;
    public String type;

    public Integer addExercise() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        return controller.addCustomExerciseToLibrary(name, description, category, type);
    }

    public int getExerciseCount() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        List<Exercise> list = controller.getAvailableExercises();
        return list.size();
    }
}
