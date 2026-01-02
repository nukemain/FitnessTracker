package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.FitnessModel;

public class AdminDeletionProcess {
    private final FitnessModel model;
    private final NotificationService notificationService;

    public AdminDeletionProcess(FitnessModel model, NotificationService notificationService) {
        this.model = model;
        this.notificationService = notificationService;
    }

    public void executeUserDeletion(int userId) {
        // Tu mogłaby być logika np. wysłania maila przed usunięciem
        // Na razie usuwamy bezpośrednio
        boolean success = model.getUserDao().deleteUser(userId);
        if (success) {
            System.out.println("[AdminProcess] Użytkownik ID " + userId + " został usunięty (wraz z danymi kaskadowo).");
        } else {
            System.out.println("[AdminProcess] Błąd usuwania użytkownika.");
        }
    }
}