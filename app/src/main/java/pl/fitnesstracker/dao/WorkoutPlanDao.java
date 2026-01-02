package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.Exercise;
import pl.fitnesstracker.model.PlanItem;
import pl.fitnesstracker.model.WorkoutPlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDao {

    // Tworzenie planu
    public Integer createPlan(WorkoutPlan plan) {
        String sql = "INSERT INTO PlanTreningowy (id_uzytkownika, nazwa_planu, opis) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, plan.getUserId());
            stmt.setString(2, plan.getPlanName());
            stmt.setString(3, plan.getDescription());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return (Integer) generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Dodawanie pozycji do planu (przyjmuje teraz obiekt PlanItem)
    public boolean addPlanItem(PlanItem item) {
        String sql = "INSERT INTO PozycjaPlanu (id_planu, id_cwiczenia, liczba_serii, liczba_powtorzen, ciezar) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Zakładamy, że item ma ustawiony planId i exerciseId
            stmt.setInt(1, item.getPlanId());
            stmt.setInt(2, item.getExerciseId());
            stmt.setInt(3, item.getSets());
            stmt.setInt(4, item.getReps());
            stmt.setBigDecimal(5, item.getWeight());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Pobieranie planów (nagłówki)
    public List<WorkoutPlan> getPlansByUser(Integer userId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM PlanTreningowy WHERE id_uzytkownika = ? AND aktywny = TRUE ORDER BY data_utworzenia DESC";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WorkoutPlan plan = new WorkoutPlan();
                plan.setId(Integer.valueOf(rs.getInt("id_planu")));
                plan.setUserId(userId);
                plan.setPlanName(rs.getString("nazwa_planu"));
                plan.setDescription(rs.getString("opis"));
                plan.setCreatedDate(rs.getTimestamp("data_utworzenia"));
                plan.setActive(Boolean.valueOf(rs.getBoolean("aktywny")));
                plans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    // Pobieranie pozycji planu wraz ze szczegółami ćwiczeń
    public List<PlanItem> getPlanItems(Integer planId) {
        List<PlanItem> items = new ArrayList<>();
        // JOIN pozwala nam pobrać nazwę i opis ćwiczenia w jednym zapytaniu
        String sql = "SELECT pp.*, c.nazwa_cwiczenia, c.opis, c.kategoria, c.typ " +
                "FROM PozycjaPlanu pp " +
                "JOIN Cwiczenie c ON pp.id_cwiczenia = c.id_cwiczenia " +
                "WHERE pp.id_planu = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, planId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PlanItem item = new PlanItem();
                item.setId(Integer.valueOf(rs.getInt("id_pozycji")));
                item.setPlanId(Integer.valueOf(rs.getInt("id_planu")));
                item.setExerciseId(Integer.valueOf(rs.getInt("id_cwiczenia")));
                item.setSets(Integer.valueOf(rs.getInt("liczba_serii")));
                item.setReps(Integer.valueOf(rs.getInt("liczba_powtorzen")));
                item.setWeight(rs.getBigDecimal("ciezar"));

                // Budowanie zagnieżdżonego obiektu Exercise
                Exercise ex = new Exercise();
                ex.setId(Integer.valueOf(rs.getInt("id_cwiczenia")));
                ex.setName(rs.getString("nazwa_cwiczenia"));
                ex.setDescription(rs.getString("opis"));
                ex.setCategory(rs.getString("kategoria"));
                ex.setType(rs.getString("typ"));

                item.setExerciseDetails(ex); // Ustawiamy szczegóły

                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean deletePlan(int planId) {
        String sql = "DELETE FROM PlanTreningowy WHERE id_planu = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}