package pl.fitnesstracker.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvStatus, btnGoToRegister;
    private Button btnLogin;
    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvStatus = findViewById(R.id.tvStatus);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Walidacja pól
            if (email.isEmpty()) {
                etEmail.setError("Podaj email");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Podaj hasło");
                return;
            }

            // Blokada przycisku na czas logowania
            btnLogin.setEnabled(false);
            tvStatus.setText("Logowanie...");

            // --- POCZĄTEK WĄTKU W TLE ---
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // 1. Logowanie (Baza Danych)
                    boolean success = controller.login(email, password);

                    // 2. Sprawdzenie powiadomień
                    boolean hasWorkout = false;
                    if (success) {
                        try {
                            hasWorkout = controller.checkDailyNotification();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    // Przekazujemy wyniki do wątku UI
                    boolean finalSuccess = success;
                    boolean finalHasWorkout = hasWorkout;

                    // --- POWRÓT DO UI ---
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);

                        if (finalSuccess) {
                            // Jeśli był trening zaplanowany, pokaż powiadomienie
                            if (finalHasWorkout) {
                                triggerAndroidNotification();
                            }

                            Toast.makeText(this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show();

                            // Przejście do Dashboardu
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            tvStatus.setText("Błędny email lub hasło");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        tvStatus.setText("Błąd połączenia: " + e.getMessage());
                    });
                }
            });
            // --- KONIEC WĄTKU W TLE ---
        });

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }

    private void triggerAndroidNotification() {
        String channelId = "fitness_channel";
        android.app.NotificationChannel channel = new android.app.NotificationChannel(
                channelId, "Treningi", android.app.NotificationManager.IMPORTANCE_HIGH);

        android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);

            android.app.Notification.Builder builder = new android.app.Notification.Builder(this, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Czas na trening!")
                    .setContentText("Masz zaplanowany trening na dzisiaj.")
                    .setAutoCancel(true);

            manager.notify(1, builder.build());
        }
    }
}