package pl.fitnesstracker.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import pl.fitnesstracker.controller.FitnessSystemController;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "fitness_daily_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Uruchamiamy wątek w tle bo dostęp do bazy może potrwać
        new Thread(() -> {
            FitnessSystemController controller = FitnessSystemController.getInstance();

            boolean hasWorkout = controller.checkDailyNotification();

            if (hasWorkout) {
                showNotification(context);
            }
        }).start();
    }

    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Przypomnienia o treningu",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Powiadomienia o zaplanowanych treningach");
        notificationManager.createNotificationChannel(channel);

        // Co się stanie jak klikniesz w powiadomienie
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Czas na trening!")
                .setContentText("Masz zaplanowany trening na dzisiaj. Powodzenia!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}