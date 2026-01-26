package pl.fitnesstracker.model;

import pl.fitnesstracker.dao.*;

public class FitnessModel {
    private UserDao userDao;
    private ExerciseDao exerciseDao;
    private WorkoutPlanDao workoutPlanDao;
    private TrainingSessionDao trainingSessionDao;
    private StatisticsDao statisticsDao;
    private NotificationDao notificationDao;
    private DayOfWeekDao dayOfWeekDao;

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
    public void setUserDao(UserDao userDao) { this.userDao = userDao; }
    
    public ExerciseDao getExerciseDao() { return exerciseDao; }
    public void setExerciseDao(ExerciseDao exerciseDao) { this.exerciseDao = exerciseDao; }
    
    public WorkoutPlanDao getWorkoutPlanDao() { return workoutPlanDao; }
    public void setWorkoutPlanDao(WorkoutPlanDao workoutPlanDao) { this.workoutPlanDao = workoutPlanDao; }
    
    public TrainingSessionDao getTrainingSessionDao() { return trainingSessionDao; }
    public void setTrainingSessionDao(TrainingSessionDao trainingSessionDao) { this.trainingSessionDao = trainingSessionDao; }
    
    public StatisticsDao getStatisticsDao() { return statisticsDao; }
    public void setStatisticsDao(StatisticsDao statisticsDao) { this.statisticsDao = statisticsDao; }
    
    public NotificationDao getNotificationDao() { return notificationDao; }
    public void setNotificationDao(NotificationDao notificationDao) { this.notificationDao = notificationDao; }
    
    public DayOfWeekDao getDayOfWeekDao() { return dayOfWeekDao; }
    public void setDayOfWeekDao(DayOfWeekDao dayOfWeekDao) { this.dayOfWeekDao = dayOfWeekDao; }
}