package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.ExerciseStatsDTO;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.SessionRecord;
import pl.fitnesstracker.model.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionRecordingProcess {
    private final FitnessModel model;
    private final NotificationService notificationService;
    private ISessionCalculationStrategy strategy;

    private final List<String> newRecordExerciseNames = new ArrayList<>();

    public SessionRecordingProcess(FitnessModel model, NotificationService notificationService) {
        this.model = model;
        this.notificationService = notificationService;
        this.strategy = new StrengthStrategy();
    }

    public void determineStrategy(User user) {
        this.newRecordExerciseNames.clear();

        String goal = user.getTrainingGoal();
        if (goal != null && (goal.equalsIgnoreCase("Cardio") || goal.equalsIgnoreCase("Utrata wagi"))) {
            this.strategy = new CardioStrategy();
            System.out.println("-> Wybrano strategię: CARDIO (Liczenie kalorii)");
        } else {
            this.strategy = new StrengthStrategy();
            System.out.println("-> Wybrano strategię: SIŁA (Liczenie 1RM)");
        }
    }

    public boolean logSet(Integer userId, Integer sessionId, Integer exerciseId, int sets, int reps, double weight) {
        BigDecimal newWeight = BigDecimal.valueOf(weight);

        Optional<ExerciseStatsDTO> oldRecordOpt = model.getStatisticsDao().getStatsForExercise(userId, exerciseId);
        if (oldRecordOpt.isPresent()) {
            BigDecimal oldMax = oldRecordOpt.get().getMaxWeight();
            if (oldMax != null && newWeight.compareTo(oldMax) > 0) {
                String exerciseName = oldRecordOpt.get().getExerciseName();
                if (!newRecordExerciseNames.contains(exerciseName)) {
                    newRecordExerciseNames.add(exerciseName);
                }
            }
        } else {
            // Jeśli nie ma starego rekordu, to ten jest pierwszy, więc też jest rekordem.
            // W idealnym świecie pobralibyśmy tu nazwę ćwiczenia po ID.
            // Na razie dodajemy ogólną informację, jeśli to pierwszy rekord.
            if (newRecordExerciseNames.isEmpty()) { 
                newRecordExerciseNames.add("nowym ćwiczeniu");
            }
        }

        SessionRecord record = new SessionRecord();
        record.setSessionId(sessionId);
        record.setExerciseId(exerciseId);
        record.setSets(Integer.valueOf(sets));
        record.setReps(Integer.valueOf(reps));
        record.setWeight(newWeight);

        return model.getTrainingSessionDao().addSessionRecord(record);
    }

    public void finishSession(int userId, int sessionId, String duration) {
        model.getTrainingSessionDao().finishSession(Integer.valueOf(sessionId), duration);

        if (!newRecordExerciseNames.isEmpty()) {
            String exercises = String.join(", ", newRecordExerciseNames);
            notificationService.createAchievementNotification(
                userId,
                "Nowy rekord!",
                "Gratulacje! Pobiłeś rekord w: " + exercises
            );
        }
    }
}
