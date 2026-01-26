package pl.fitnesstracker.fitnesse;

import fit.Fixture;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.*;
import pl.fitnesstracker.model.User;

import java.math.BigDecimal;

public class SetUp extends Fixture {
    public static FitnessSystemController controller;
    public static InMemoryWorkoutPlanDao memoryPlanDao;
    public static InMemoryUserDao memoryUserDao;
    public static InMemoryExerciseDao memoryExerciseDao;
    public static InMemoryTrainingSessionDao memorySessionDao;
    public static InMemoryStatisticsDao memoryStatsDao;

    public SetUp() {
        controller = FitnessSystemController.getInstance();
        
        memoryPlanDao = new InMemoryWorkoutPlanDao();
        memoryUserDao = new InMemoryUserDao();
        memoryExerciseDao = new InMemoryExerciseDao();
        memorySessionDao = new InMemoryTrainingSessionDao();
        memoryStatsDao = new InMemoryStatisticsDao();
        
        controller.getModel().setWorkoutPlanDao(memoryPlanDao);
        controller.getModel().setUserDao(memoryUserDao);
        controller.getModel().setExerciseDao(memoryExerciseDao);
        controller.getModel().setTrainingSessionDao(memorySessionDao);
        controller.getModel().setStatisticsDao(memoryStatsDao);
        controller.getModel().setNotificationDao(new InMemoryNotificationDao());
        controller.getModel().setDayOfWeekDao(new InMemoryDayOfWeekDao());
        
        User testUser = new User("test@example.com", "pass", new BigDecimal("75.0"), 180, "Strength");
        testUser.setId(1);
        testUser.setRole("user");
        memoryUserDao.registerUser(testUser);
        controller.setCurrentUser(testUser);
    }
}
