package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.SessionRecord;
import pl.fitnesstracker.model.User;
import java.math.BigDecimal;

public class SessionRecordingProcess {
    private final FitnessModel model;
    private ISessionCalculationStrategy strategy;

    public SessionRecordingProcess(FitnessModel model) {
        this.model = model;
        // Domyślna strategia, zostanie nadpisana przy starcie sesji
        this.strategy = new StrengthStrategy();
    }

    // Tę metodę wywołamy w kontrolerze przy starcie sesji, żeby ustawić dobrą strategię
    public void determineStrategy(User user) {
        String goal = user.getTrainingGoal(); // np. "Masa", "Sila", "Cardio", "Utrata wagi"

        if (goal != null && (goal.equalsIgnoreCase("Cardio") || goal.equalsIgnoreCase("Utrata wagi"))) {
            this.strategy = new CardioStrategy();
            System.out.println("-> Wybrano strategię: CARDIO (Liczenie kalorii)");
        } else {
            this.strategy = new StrengthStrategy();
            System.out.println("-> Wybrano strategię: SIŁA (Liczenie 1RM)");
        }
    }

    public boolean logSet(Integer userId, Integer sessionId, Integer exerciseId, int sets, int reps, double weight) {
        SessionRecord record = new SessionRecord();
        record.setSessionId(sessionId);
        record.setExerciseId(exerciseId);
        record.setSets(Integer.valueOf(sets));
        record.setReps(Integer.valueOf(reps));
        record.setWeight(BigDecimal.valueOf(weight));

        boolean success = model.getTrainingSessionDao().addSessionRecord(record);

        if (success) {
            // Strategia oblicza swoje metryki
            strategy.calculateAndPersist(userId, record);
        }
        return success;
    }

    public void finishSession(int userId, int sessionId, String duration) {
        model.getTrainingSessionDao().finishSession(Integer.valueOf(sessionId), duration);
        // Koniec sesji (ważne dla Cardio)
        strategy.finalizeSessionStats(userId, duration);
    }
}