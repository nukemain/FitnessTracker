package pl.fitnesstracker.dao;

import pl.fitnesstracker.model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    // Pobierz powiadomienia użytkownika
    public List<Notification> getUserNotifications(Integer userId) {
        List<Notification> notifications = new ArrayList<>();
        // Pobieramy najnowsze na górze
        String sql = "SELECT * FROM Powiadomienie WHERE id_uzytkownika = ? ORDER BY data_powiadomienia DESC";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notif = new Notification();
                notif.setId(Integer.valueOf(rs.getInt("id_powiadomienia")));
                notif.setUserId(userId);
                notif.setType(rs.getString("typ_powiadomienia"));
                notif.setMessage(rs.getString("tresc"));
                notif.setDate(rs.getTimestamp("data_powiadomienia"));
                notif.setStatus(rs.getString("status"));
                notifications.add(notif);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    // Oznacz jako przeczytane
    public void markAsRead(Integer notificationId) {
        String sql = "UPDATE Powiadomienie SET status = 'Przeczytane' WHERE id_powiadomienia = ?";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tworzenie powiadomienia
    public void createNotification(Notification notification) {
        String sql = "INSERT INTO Powiadomienie (id_uzytkownika, typ_powiadomienia, tresc, status) VALUES (?, ?, ?, 'Nowe')";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getType()); // Np. "Przypomnienie"
            stmt.setString(3, notification.getMessage());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Sprawdza czy użytkownik dostał już dziś powiadomienie tego typu
    public boolean hasNotificationForToday(int userId, String type) {
        String sql = "SELECT 1 FROM Powiadomienie WHERE id_uzytkownika = ? AND typ_powiadomienia = ? AND DATE(data_powiadomienia) = CURRENT_DATE";
        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void deleteNotificationsByType(int userId, String type) {
        String sql = "DELETE FROM Powiadomienie WHERE id_uzytkownika = ? AND typ_powiadomienia = ?";

        try (Connection conn = DatabaseConnector.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}