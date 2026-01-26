package pl.fitnesstracker.fitnesse;

import fit.ColumnFixture;
import pl.fitnesstracker.controller.FitnessSystemController;

public class TestCreatePlan extends ColumnFixture {
    public String planName;
    public String description;

    public boolean createPlan() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        
        int countBefore = controller.getWorkoutPlanCount();
        
        controller.createWorkoutPlan(planName, description);
        
        int countAfter = controller.getWorkoutPlanCount();
        
        return countAfter == countBefore + 1;
    }
}
