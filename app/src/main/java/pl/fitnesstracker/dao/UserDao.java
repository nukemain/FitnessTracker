package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.User;
import pl.fitnesstracker.utils.SecurityUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public boolean registerUser(User user) {
        // Dodano obsługę kolumny 'rola'
        String insertUserSql = "INSERT INTO Uzytkownik (email, haslo, waga, wzrost, cel_treningowy, rola) VALUES (?, ?, ?, ?, ?, ?)";
        String insertStatsSql = "INSERT INTO Statystyka (id_uzytkownika, data_aktualizacji) VALUES (?, CURRENT_TIMESTAMP)";

        Connection conn = null;
        PreparedStatement stmtUser = null;
        PreparedStatement stmtStats = null;

        try {
            conn = DatabaseConnector.getInstance().getConnection();
            conn.setAutoCommit(false);

            stmtUser = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getEmail());
            stmtUser.setString(2, SecurityUtils.hashPassword(user.getPassword()));
            stmtUser.setBigDecimal(3, user.getWeight());
            stmtUser.setInt(4, user.getHeight());
            stmtUser.setString(5, user.getTrainingGoal());
            stmtUser.setString(6, user.getRole()); // Zapis roli do bazy

            int affectedRows = stmtUser.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmtUser.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newUserId = generatedKeys.getInt(1);
                        user.setId(Integer.valueOf(newUserId));

                        stmtStats = conn.prepareStatement(insertStatsSql);
                        stmtStats.setInt(1, newUserId);
                        stmtStats.executeUpdate();

                        conn.commit();
                        return true;
                    }
                }
            }
            conn.rollback();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                if (stmtUser != null) stmtUser.close();
                if (stmtStats != null) stmtStats.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Optional<User> login(String email, String rawPassword) {
        String sql = "SELECT * FROM Uzytkownik WHERE email = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("haslo");
                String inputHash = SecurityUtils.hashPassword(rawPassword);

                if (storedHash.equals(inputHash)) {
                    User user = new User();
                    user.setId(Integer.valueOf(rs.getInt("id_uzytkownika")));
                    user.setEmail(rs.getString("email"));
                    user.setWeight(rs.getBigDecimal("waga"));
                    user.setHeight(Integer.valueOf(rs.getInt("wzrost")));
                    user.setTrainingGoal(rs.getString("cel_treningowy"));
                    // Pobranie roli z bazy
                    user.setRole(rs.getString("rola")); 
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM Uzytkownik WHERE id_uzytkownika = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE Uzytkownik SET waga = ?, wzrost = ?, cel_treningowy = ?, rola = ? WHERE id_uzytkownika = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, user.getWeight());
            stmt.setInt(2, user.getHeight());
            stmt.setString(3, user.getTrainingGoal());
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Uzytkownik";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id_uzytkownika"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("rola"));
                users.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return users;
    }
}
