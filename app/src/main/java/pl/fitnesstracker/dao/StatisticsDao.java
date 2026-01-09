package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.ExerciseStatsDTO;
import pl.fitnesstracker.model.Statistics;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatisticsDao {

    public Statistics getUserStatistics(Integer userId) {
        String sql = "SELECT * FROM Statystyka WHERE id_uzytkownika = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Statistics stats = new Statistics();
                stats.setId(Integer.valueOf(rs.getInt("id_statystyki")));
                stats.setUserId(userId);
                stats.setLastUpdate(rs.getTimestamp("data_aktualizacji"));
                stats.setMaxWeight(rs.getBigDecimal("przewidywane_1rm"));
                stats.setTotalReps(Integer.valueOf(rs.getInt("liczba_powtorzen")));
                stats.setTotalWorkouts(Integer.valueOf(rs.getInt("liczba_treningow")));
                stats.setTotalRecords(Integer.valueOf(rs.getInt("liczba_rekordow")));
                stats.setCaloriesBurned(rs.getInt("spalone_kalorie"));
                return stats;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ExerciseStatsDTO> getExerciseStats(int userId) {
        List<ExerciseStatsDTO> list = new ArrayList<>();
        String sql = "SELECT c.nazwa_cwiczenia, c.typ, " +
                "MAX(r.ciezar) as max_w, " +
                "MAX(r.ciezar * r.liczba_powtorzen * r.liczba_serii) as max_vol " +
                "FROM RekordSesji r " +
                "JOIN SesjaTreningowa s ON r.id_sesji = s.id_sesji " +
                "JOIN Cwiczenie c ON r.id_cwiczenia = c.id_cwiczenia " +
                "WHERE s.id_uzytkownika = ? " +
                "GROUP BY c.nazwa_cwiczenia, c.typ";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new ExerciseStatsDTO(
                        rs.getString("nazwa_cwiczenia"),
                        rs.getString("typ"),
                        rs.getBigDecimal("max_w"),
                        rs.getBigDecimal("max_vol")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<ExerciseStatsDTO> getStatsForExercise(int userId, int exerciseId) {
        String sql = "SELECT c.nazwa_cwiczenia, c.typ, MAX(r.ciezar) as max_w " +
                "FROM RekordSesji r " +
                "JOIN SesjaTreningowa s ON r.id_sesji = s.id_sesji " +
                "JOIN Cwiczenie c ON r.id_cwiczenia = c.id_cwiczenia " +
                "WHERE s.id_uzytkownika = ? AND r.id_cwiczenia = ? " +
                "GROUP BY c.nazwa_cwiczenia, c.typ";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, exerciseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new ExerciseStatsDTO(
                        rs.getString("nazwa_cwiczenia"),
                        rs.getString("typ"),
                        rs.getBigDecimal("max_w"),
                        null // Max Volume not needed for this check
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
