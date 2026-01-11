package pl.fitnesstracker.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DayOfWeekDao {

    // Przypisz plan do dnia
    public void assignPlanToDay(int planId, String dayName) {
        // Najpierw usuwamy ewentualne stare przypisanie tego planu do tego dnia
        String cleanSql = "DELETE FROM DzienTygodnia WHERE id_planu = ? AND nazwa_dnia = ?";
        String insertSql = "INSERT INTO DzienTygodnia (id_planu, nazwa_dnia) VALUES (?, ?)";

        try (Connection conn = DatabaseConnector.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(cleanSql)) {
                stmt.setInt(1, planId);
                stmt.setString(2, dayName);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, planId);
                stmt.setString(2, dayName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Sprawdź czy plan jest na dziś
    public boolean isPlanScheduledForDay(int planId, String dayName) {
        String sql = "SELECT 1 FROM DzienTygodnia WHERE id_planu = ? AND nazwa_dnia = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            stmt.setString(2, dayName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}