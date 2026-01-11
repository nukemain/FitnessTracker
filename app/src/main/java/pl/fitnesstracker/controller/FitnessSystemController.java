package pl.fitnesstracker.controller;

import android.content.Context;
import pl.fitnesstracker.model.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FitnessSystemController implements IFitnessSystemController {

    private final FitnessModel model;
    private final SessionRecordingProcess sessionProcess;
    private final WorkoutPlanModificationProcess planProcess;
    private final NotificationService notificationService;
    private final AdminDeletionProcess adminDeletionProcess;

    private User currentUser;
    private Integer currentSessionId;

    private static FitnessSystemController instance;

    private FitnessSystemController() {
        this.model = new FitnessModel();
        this.notificationService = new NotificationService(model);
        this.sessionProcess = new SessionRecordingProcess(model, notificationService);
        this.planProcess = new WorkoutPlanModificationProcess(model);
        this.adminDeletionProcess = new AdminDeletionProcess(model, notificationService);
    }

    public static synchronized FitnessSystemController getInstance() {
        if (instance == null) {
            instance = new FitnessSystemController();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.notificationService.setContext(context);
    }

    @Override
    public boolean login(String email, String password) {
        Optional<User> userOpt = model.getUserDao().login(email, password);
        if (userOpt.isPresent()) {
            this.currentUser = userOpt.get();
            notificationService.checkAndNotifyScheduledWorkout(currentUser);
            return true;
        }
        return false;
    }

    @Override
    public boolean register(String email, String password, BigDecimal weight, Integer height, String goal) {
        User newUser = new User(email, password, weight, height, goal);
        return model.getUserDao().registerUser(newUser);
    }

    @Override
    public void logout() {
        this.currentUser = null;
        this.currentSessionId = null;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    public void updateUserData(BigDecimal newWeight, String newGoal) {
        if (currentUser != null) {
            currentUser.setWeight(newWeight);
            currentUser.setTrainingGoal(newGoal);
            model.getUserDao().updateUser(currentUser);
        }
    }

    @Override
    public Integer createWorkoutPlan(String name, String description) {
        if (currentUser == null) return null;
        return planProcess.createPlan(currentUser.getId(), name, description);
    }

    @Override
    public List<WorkoutPlan> getUserWorkoutPlans() {
        if (currentUser == null) return Collections.emptyList();
        return model.getWorkoutPlanDao().getPlansByUser(currentUser.getId());
    }

    @Override
    public boolean addExerciseToPlan(Integer planId, Integer exerciseId, int sets, int reps, double weight) {
        return planProcess.addExercise(planId, exerciseId, sets, reps, weight);
    }

    public Integer addCustomExerciseToLibrary(String name, String description, String category, String type) {
        if (currentUser == null) return null;

        Exercise ex = new Exercise();
        ex.setUserId(currentUser.getId());
        ex.setName(name);
        ex.setDescription(description);
        ex.setCategory(category);
        ex.setType(type);

        return model.getExerciseDao().addCustomExercise(ex);
    }

    @Override
    public List<Exercise> getAvailableExercises() {
        if (currentUser == null) return Collections.emptyList();
        return model.getExerciseDao().getAllExercises(currentUser.getId());
    }

    public List<PlanItem> getPlanDetails(Integer planId) {
        return model.getWorkoutPlanDao().getPlanItems(planId);
    }

    public void assignPlanToDay(int planId, String dayName) {
        model.getDayOfWeekDao().assignPlanToDay(planId, dayName);
    }

    public void deleteWorkoutPlan(int planId) {
        model.getWorkoutPlanDao().deletePlan(planId);
    }

    @Override
    public Integer startSession(Integer planId) {
        if (currentUser == null || currentSessionId != null) return null;

        //sessionProcess.determineStrategy(currentUser);
        Integer sessionId = model.getTrainingSessionDao().startSession(currentUser.getId(), planId);

        if (sessionId != null) {
            this.currentSessionId = sessionId;
        }
        return sessionId;
    }

    @Override
    public void endSession(String duration) {
        if (currentSessionId == null) return;
        sessionProcess.finishSession(currentUser.getId(), currentSessionId, duration);
        notificationService.updateDailyWorkoutNotification(currentUser.getId());
        this.currentSessionId = null;
    }

    @Override
    public boolean logSet(Integer exerciseId, int sets, int reps, double weight) {
        if (currentSessionId == null) return false;
        return sessionProcess.logSet(currentUser.getId(), currentSessionId, exerciseId, sets, reps, weight);
    }

    public boolean updateSet(int recordId, int sets, int reps, double weight) {
        return model.getTrainingSessionDao().updateSessionRecord(recordId, sets, reps, weight);
    }

    @Override
    public void addNoteToSession(String content) {
        if (currentSessionId == null) return;
        model.getTrainingSessionDao().addNote(new Note(currentSessionId, content));
    }

    public void deleteTrainingSession(int sessionId) {
        model.getTrainingSessionDao().deleteSession(sessionId);
    }

    @Override
    public Statistics getUserStatistics() {
        if (currentUser == null) return new Statistics();
        return model.getStatisticsDao().getUserStatistics(currentUser.getId());
    }

    @Override
    public List<Notification> getUserNotifications() {
        if (currentUser == null) return Collections.emptyList();
        return model.getNotificationDao().getUserNotifications(currentUser.getId());
    }

    public List<TrainingSession> getCompletedSessions(int userId) {
        return model.getTrainingSessionDao().getCompletedSessions(userId);
    }

    public List<SessionRecord> getSessionDetails(int sessionId) {
        return model.getTrainingSessionDao().getSessionRecords(sessionId);
    }
    public List<Note> getSessionNotes(int sessionId) {
        return model.getTrainingSessionDao().getSessionNotes(sessionId);
    }

    public List<ExerciseStatsDTO> getAllExerciseStats() {
        if (currentUser == null) return Collections.emptyList();
        return model.getStatisticsDao().getExerciseStats(currentUser.getId());
    }

    public boolean checkDailyNotification() {
        if(currentUser == null) return false;
        notificationService.checkAndNotifyScheduledWorkout(currentUser);
        List<Notification> notifs = getUserNotifications();
        return notifs.stream().anyMatch(n -> n.getMessage().contains("Dzisiaj masz"));
    }

    public void updateUserGoal(String newGoal) {
        if (currentUser != null) {
            currentUser.setTrainingGoal(newGoal);
            model.getUserDao().updateUser(currentUser);
        }
    }

    public List<User> getAllUsers() {
        if (currentUser == null) return Collections.emptyList();
        return model.getUserDao().getAllUsers();
    }

    public void deleteAccount() {
        if (currentUser != null) {
            adminDeletionProcess.executeUserDeletion(currentUser.getId());
            logout();
        }
    }
}
