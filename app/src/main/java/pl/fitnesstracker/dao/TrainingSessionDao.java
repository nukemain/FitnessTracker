package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.Exercise;
import pl.fitnesstracker.model.Note;
import pl.fitnesstracker.model.SessionRecord;
import pl.fitnesstracker.model.TrainingSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingSessionDao {

    public Integer startSession(Integer userId, Integer planId) {
        String sql = "INSERT INTO SesjaTreningowa (id_uzytkownika, id_planu, status, data_sesji) VALUES (?, ?, 'W toku', CURRENT_DATE)";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            if (planId != null) stmt.setInt(2, planId);
            else stmt.setNull(2, Types.INTEGER);

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return (Integer) generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void finishSession(Integer sessionId, String duration) {
        String sql = "UPDATE SesjaTreningowa SET status = 'Zakonczona', czas_trwania = ?::interval WHERE id_sesji = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, duration);
            stmt.setInt(2, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Zapis rekordu sesji (czyli obiekt SessionRecord)
    public boolean addSessionRecord(SessionRecord record) {
        String sql = "INSERT INTO RekordSesji (id_sesji, id_cwiczenia, liczba_serii, liczba_powtorzen, ciezar) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, record.getSessionId());

            stmt.setInt(2, record.getExerciseId());
            stmt.setInt(3, record.getSets());
            stmt.setInt(4, record.getReps());
            stmt.setBigDecimal(5, record.getWeight());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Pobieranie rekordów dla sesji (z JOINem do ćwiczeń)
    public List<SessionRecord> getSessionRecords(int sessionId) {
        List<SessionRecord> records = new ArrayList<>();

        // ZMIANA W SQL: Dodaliśmy c.typ i c.kategoria
        String sql = "SELECT r.id_rekordu, r.id_sesji, r.id_cwiczenia, r.liczba_serii, r.liczba_powtorzen, r.ciezar, " +
                "c.nazwa_cwiczenia, c.typ, c.kategoria " +
                "FROM RekordSesji r " +
                "JOIN Cwiczenie c ON r.id_cwiczenia = c.id_cwiczenia " +
                "WHERE r.id_sesji = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                SessionRecord r = new SessionRecord();
                r.setId(rs.getInt("id_rekordu"));
                r.setSessionId(rs.getInt("id_sesji"));
                r.setExerciseId(rs.getInt("id_cwiczenia"));
                r.setSets(rs.getInt("liczba_serii"));
                r.setReps(rs.getInt("liczba_powtorzen"));
                r.setWeight(rs.getBigDecimal("ciezar"));

                Exercise exInfo = new Exercise();
                exInfo.setName(rs.getString("nazwa_cwiczenia"));

                exInfo.setType(rs.getString("typ"));
                exInfo.setCategory(rs.getString("kategoria"));

                r.setExerciseDetails(exInfo);
                records.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // Dodanie notatki
    public void addNote(Note note) {
        String sql = "INSERT INTO Notatka (id_sesji, tresc_notatki) VALUES (?, ?)";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, note.getSessionId());
            stmt.setString(2, note.getContent());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Note> getSessionNotes(int sessionId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM Notatka WHERE id_sesji = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                notes.add(new Note(sessionId, rs.getString("tresc_notatki")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return notes;
    }

    // Metoda do pobierania historii zakończonych treningów
    public List<TrainingSession> getCompletedSessions(int userId) {
        List<TrainingSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM SesjaTreningowa WHERE id_uzytkownika = ? AND status LIKE 'Zako%' ORDER BY data_sesji DESC";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TrainingSession s = new TrainingSession();
                s.setId(rs.getInt("id_sesji"));
                s.setPlanId(rs.getInt("id_planu"));
                s.setSessionDate(rs.getDate("data_sesji"));
                s.setDuration(rs.getString("czas_trwania"));
                sessions.add(s);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    public void deleteSession(int sessionId) {
        String sql = "DELETE FROM SesjaTreningowa WHERE id_sesji = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}