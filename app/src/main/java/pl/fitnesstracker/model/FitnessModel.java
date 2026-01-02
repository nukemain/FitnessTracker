package pl.fitnesstracker.model;

import pl.fitnesstracker.dao.*;

public class FitnessModel {
    private final UserDao userDao;
    private final ExerciseDao exerciseDao;
    private final WorkoutPlanDao workoutPlanDao;
    private final TrainingSessionDao trainingSessionDao;
    private final StatisticsDao statisticsDao;
    private final NotificationDao notificationDao;
    private final DayOfWeekDao dayOfWeekDao;

    public FitnessModel() {
        this.userDao = new UserDao();
        this.exerciseDao = new ExerciseDao();
        this.workoutPlanDao = new WorkoutPlanDao();
        this.trainingSessionDao = new TrainingSessionDao();
        this.statisticsDao = new StatisticsDao();
        this.notificationDao = new NotificationDao();
        this.dayOfWeekDao = new DayOfWeekDao();
    }

    public UserDao getUserDao() { return userDao; }
    public ExerciseDao getExerciseDao() { return exerciseDao; }
    public WorkoutPlanDao getWorkoutPlanDao() { return workoutPlanDao; }
    public TrainingSessionDao getTrainingSessionDao() { return trainingSessionDao; }
    public StatisticsDao getStatisticsDao() { return statisticsDao; }
    public NotificationDao getNotificationDao() { return notificationDao; }
    public DayOfWeekDao getDayOfWeekDao() { return dayOfWeekDao; }
}