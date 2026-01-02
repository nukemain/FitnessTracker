package pl.fitnesstracker.controller;

import pl.fitnesstracker.dao.DatabaseConnector;
import pl.fitnesstracker.model.SessionRecord;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StrengthStrategy implements ISessionCalculationStrategy {

    @Override
    public void calculateAndPersist(int userId, SessionRecord record) {
        // Obliczamy 1RM z bieżącej serii wg wzoru Epleya: W * (1 + R/30) (uu fancy wzorek)
        double weight = record.getWeight().doubleValue();
        int reps = record.getReps();

        if (weight <= 0 || reps <= 0) return;

        double currentOneRepMax = weight * (1 + (double)reps / 30.0);

        // Sprawdzamy w bazie obecny rekord 1RM
        double dbOneRepMax = getCurrentDb1RM(userId);

        // Jeśli nowy wynik jest lepszy, aktualizujemy bazę
        if (currentOneRepMax > dbOneRepMax) {
            updateDb1RM(userId, currentOneRepMax);
            System.out.println("[STRATEGIA SIŁOWA] Nowy rekord 1RM: " + String.format("%.2f", Double.valueOf(currentOneRepMax)) + " kg!");
        }
    }

    @Override
    public void finalizeSessionStats(int userId, String duration) {
        // W strategii siłowej czas ma mniejsze znaczenie dla statystyk "siły", więc możemy to pominąć lub zalogować.
        System.out.println("[STRATEGIA SIŁOWA] Zakończono sesję. Statystyki siłowe zaktualizowane na bieżąco.");
    }

    // Metody pomocnicze JDBC (Private)

    private double getCurrentDb1RM(int userId) {
        String sql = "SELECT przewidywane_1rm FROM Statystyka WHERE id_uzytkownika = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("przewidywane_1rm");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private void updateDb1RM(int userId, double newMax) {
        String sql = "UPDATE Statystyka SET przewidywane_1rm = ? WHERE id_uzytkownika = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, BigDecimal.valueOf(newMax));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}