package pl.fitnesstracker.fitnesse;

import fit.ColumnFixture;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.User;

import java.math.BigDecimal;

public class TestUserManagement extends ColumnFixture {
    public String email;
    public String password;
    public String weight;
    public int height;
    public String goal;

    public boolean register() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        return controller.register(email, password, new BigDecimal(weight), height, goal);
    }
    
    public boolean login() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        return controller.login(email, password);
    }

    public boolean isLoggedIn() {
        FitnessSystemController controller = FitnessSystemController.getInstance();
        User u = controller.getCurrentUser();
        return u != null && u.getEmail().equals(email);
    }
}
