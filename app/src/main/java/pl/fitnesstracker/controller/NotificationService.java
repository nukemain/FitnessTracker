package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.model.Notification;
import pl.fitnesstracker.model.User;
import pl.fitnesstracker.model.WorkoutPlan;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class NotificationService {
    private final FitnessModel model;

    public NotificationService(FitnessModel model) {
        this.model = model;
    }

    // 1. Powiadomienie o osiągnięciach (wywoływane po treningu/rekordzie)
    public void createAchievementNotification(int userId, String achievementName) {
        Notification note = new Notification();
        note.setUserId(Integer.valueOf(userId));
        note.setType("Osiągnięcie");
        note.setMessage("Gratulacje! Osiągnięto cel: " + achievementName);
        model.getNotificationDao().createNotification(note);
        System.out.println("[NotificationService] Wysłano powiadomienie o osiągnięciu do User ID: " + userId);
    }

    // 2. Powiadomienie o zaplanowanym treningu (sprawdzane np. przy logowaniu)
    public void checkAndNotifyScheduledWorkout(User user) {
        // Pobieramy dzisiejszy dzień tygodnia
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String polishDayName = mapDayToPolish(today);

        // Sprawdzamy plany użytkownika
        List<WorkoutPlan> plans = model.getWorkoutPlanDao().getPlansByUser(user.getId());

        for (WorkoutPlan plan : plans) {
            // Sprawdzamy, czy plan jest przypisany do dzisiejszego dnia
            boolean isScheduledToday = model.getDayOfWeekDao().isPlanScheduledForDay(plan.getId(), polishDayName);

            if (isScheduledToday) {
                // Sprawdzamy czy nie ma już powiadomienia na dziś, żeby nie spamować
                boolean alreadyNotified = model.getNotificationDao().hasNotificationForToday(user.getId(), "Przypomnienie");

                if (!alreadyNotified) {
                    Notification note = new Notification();
                    note.setUserId(user.getId());
                    note.setType("Przypomnienie");
                    note.setMessage("Dzisiaj masz zaplanowany trening: " + plan.getPlanName());
                    model.getNotificationDao().createNotification(note);
                    System.out.println("[NotificationService] Przypomniano o treningu: " + plan.getPlanName());
                }
            }
        }
    }

    private String mapDayToPolish(DayOfWeek day) {
        // Proste mapowanie na nazwy w bazie
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
        // 1. Sprawdź czy jest powiadomienie o typie "Przypomnienie" z dzisiaj
        // To wymaga metody w DAO, np. findNotificationByTypeAndDate
        // Dla uproszczenia w tym projekcie: po prostu usuwamy stare przypomnienia i dodajemy nowe "Sukces"

        // Usuń stare przypomnienia
        model.getNotificationDao().deleteNotificationsByType(userId, "Przypomnienie");

        // Dodaj nowe powiadomienie
        Notification note = new Notification();
        note.setUserId(userId);
        note.setType("Info");
        note.setMessage("Zrobiono dzisiejszy trening! Dobra robota. ✅");
        model.getNotificationDao().createNotification(note);
    }
}