package pl.fitnesstracker.fitnesse;

import pl.fitnesstracker.dao.NotificationDao;
import pl.fitnesstracker.model.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryNotificationDao extends NotificationDao {
    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public List<Notification> getUserNotifications(Integer userId) {
        return notifications.stream()
                .filter(n -> n.getUserId() == userId)
                .collect(Collectors.toList());
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }
    
    @Override
    public void createNotification(Notification notification) {
        notification.setId(notifications.size() + 1);
        notifications.add(notification);
    }
    
    @Override
    public boolean hasNotificationForToday(int userId, String type) {
        return notifications.stream()
                .anyMatch(n -> n.getUserId() == userId && n.getType().equals(type));
    }
    
    @Override
    public void deleteNotificationsByType(int userId, String type) {
        notifications.removeIf(n -> n.getUserId() == userId && n.getType().equals(type));
    }

    @Override
    public void markAsRead(Integer notificationId) {
        notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(n -> n.setStatus("Przeczytane"));
    }
    
}
