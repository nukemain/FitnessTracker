package pl.fitnesstracker.controller;

import pl.fitnesstracker.dao.DatabaseConnector;
import pl.fitnesstracker.model.SessionRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CardioStrategy implements ISessionCalculationStrategy {

    @Override
    public void calculateAndPersist(int userId, SessionRecord record) {
        // Cardio ignoruje pojedyncze serie siłowe
    }

    @Override
    public void finalizeSessionStats(int userId, String duration) {
        // Format duration z Postgre to np. "01:15:00" lub "1 hour".
        int minutes = parseDurationToMinutes(duration);

        // Wzór: ok. 8 kcal na minutę (średnia intensywność)
        int caloriesBurned = minutes * 8;

        updateCalories(userId, caloriesBurned);
        //System.out.println("[STRATEGIA CARDIO] Czas: " + minutes + " min. Spalono ok. " + caloriesBurned + " kcal.");
    }

    private int parseDurationToMinutes(String duration) {
        try {
            // format HH:MM:SS
            String[] parts = duration.split(":");
            if (parts.length == 3) {
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                return (h * 60) + m;
            }
        } catch (Exception e) {
            System.out.println("Błąd parsowania czasu: " + duration);
        }
        return 30; // Wartość domyślna w razie błędu formatu
    }

    private void updateCalories(int userId, int caloriesToAdd) {
        // Dodajemy nowe kalorie do sumy już istniejącej
        String sql = "UPDATE Statystyka SET spalone_kalorie = spalone_kalorie + ? WHERE id_uzytkownika = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, caloriesToAdd);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}