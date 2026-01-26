package pl.fitnesstracker.fitnesse;

import fit.ColumnFixture;
import pl.fitnesstracker.controller.FitnessSystemController;

public class TestSessionManagement extends ColumnFixture {
    public Integer planId;
    public String duration;
    public Integer exerciseId;
    public int sets;
    public int reps;
    public double weight;

    public Integer startSession() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        return controller.startSession(planId);
    }

    public boolean logSet() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        return controller.logSet(exerciseId, sets, reps, weight);
    }

    public boolean endSession() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        
        try {
            controller.endSession(duration);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
