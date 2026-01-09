package pl.fitnesstracker.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.Notification;
import pl.fitnesstracker.model.User;
import pl.fitnesstracker.model.WorkoutPlan;
import pl.fitnesstracker.app.MainActivity;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

public class NotificationService {
    private final FitnessModel model;
    private Context context; 

    public NotificationService(FitnessModel model) {
        this.model = model;
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    public void createAchievementNotification(int userId, String title, String message) {
        if (context == null) return; 

        Notification note = new Notification();
        note.setUserId(userId);
        note.setType("Osiągnięcie");
        note.setMessage(title + ": " + message);
        model.getNotificationDao().createNotification(note);

        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "achievement_channel";

        NotificationChannel channel = new NotificationChannel(
                channelId,
                "Osiągnięcia",
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(channel);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public void checkAndNotifyScheduledWorkout(User user) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String polishDayName = mapDayToPolish(today);

        List<WorkoutPlan> plans = model.getWorkoutPlanDao().getPlansByUser(user.getId());

        for (WorkoutPlan plan : plans) {
            boolean isScheduledToday = model.getDayOfWeekDao().isPlanScheduledForDay(plan.getId(), polishDayName);

            if (isScheduledToday) {
                boolean alreadyNotified = model.getNotificationDao().hasNotificationForToday(user.getId(), "Przypomnienie");

                if (!alreadyNotified) {
                    Notification note = new Notification();
                    note.setUserId(user.getId());
                    note.setType("Przypomnienie");
                    note.setMessage("Dzisiaj masz zaplanowany trening: " + plan.getPlanName());
                    model.getNotificationDao().createNotification(note);
                }
            }
        }
    }

    private String mapDayToPolish(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Poniedziałek";
            case TUESDAY: return "Wtorek";
            case WEDNESDAY: return "Środa";
            case THURSDAY: return "Czwartek";
            case FRIDAY: return "Piątek";
            case SATURDAY: return "Sobota";
            case SUNDAY: return "Niedziela";
            default: return "";
        }
    }

    public void updateDailyWorkoutNotification(int userId) {
        model.getNotificationDao().deleteNotificationsByType(userId, "Przypomnienie");

        Notification note = new Notification();
        note.setUserId(userId);
        note.setType("Info");
        note.setMessage("Zrobiono dzisiejszy trening! Dobra robota. ✅");
        model.getNotificationDao().createNotification(note);
    }
}
