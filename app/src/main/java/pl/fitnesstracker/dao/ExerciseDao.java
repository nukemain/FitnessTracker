package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.Exercise;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDao {

    // Sprawdzenie czy ćwiczenie istnieje (dla ExistingExStrategy)
    public boolean exerciseExists(int exerciseId) {
        String sql = "SELECT 1 FROM Cwiczenie WHERE id_cwiczenia = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, exerciseId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Dodanie nowego ćwiczenia (dla NewExStrategy)
    public Integer addCustomExercise(Exercise exercise) {
        String sql = "INSERT INTO Cwiczenie (id_uzytkownika, nazwa_cwiczenia, opis, kategoria, typ) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, exercise.getUserId());
            stmt.setString(2, exercise.getName());

            stmt.setString(3, exercise.getDescription());

            stmt.setString(4, exercise.getCategory());
            stmt.setString(5, exercise.getType());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        exercise.setId(newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Pobranie wszystkich ćwiczeń (dla widoku listy)
    public List<Exercise> getAllExercises(Integer userId) {
        List<Exercise> exercises = new ArrayList<>();
        String sql = "SELECT * FROM Cwiczenie WHERE id_uzytkownika IS NULL OR id_uzytkownika = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Exercise ex = new Exercise();
                ex.setId(rs.getInt("id_cwiczenia"));
                ex.setName(rs.getString("nazwa_cwiczenia"));
                ex.setDescription(rs.getString("opis"));
                ex.setCategory(rs.getString("kategoria"));
                ex.setType(rs.getString("typ"));
                exercises.add(ex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exercises;
    }
}