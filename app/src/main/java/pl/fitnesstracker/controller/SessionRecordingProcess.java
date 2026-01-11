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


    private final List<String> newRecordExerciseNames = new ArrayList<>();

    public SessionRecordingProcess(FitnessModel model, NotificationService notificationService) {
        this.model = model;
        this.notificationService = notificationService;
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
