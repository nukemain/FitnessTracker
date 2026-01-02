package pl.fitnesstracker.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.Notification;
import pl.fitnesstracker.model.PlanItem;
import pl.fitnesstracker.model.Statistics;
import pl.fitnesstracker.model.User;
import pl.fitnesstracker.model.WorkoutPlan;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvStatsWorkoutCount, tvStatsMax, tvNotification;
    private LinearLayout plansContainer;
    private Button btnAdminPanel;
    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(); // Odśwież dane po powrocie z tworzenia planu lub sesji
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Inicjalizacja widoków
        tvStatsWorkoutCount = findViewById(R.id.tvStatsWorkoutCount);
        tvStatsMax = findViewById(R.id.tvStatsMax);
        tvNotification = findViewById(R.id.tvNotification);
        plansContainer = findViewById(R.id.plansContainer);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);

        // 1. Konfiguracja Powiadomień
        setupNotifications();

        // 2. Obsługa przycisków nawigacyjnych
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        findViewById(R.id.btnCreatePlan).setOnClickListener(v ->
                startActivity(new Intent(this, CreatePlanActivity.class))
        );

        // 3. Widoczność panelu Admina
        checkAdminAccess();

        // 4. Pobranie danych
        refreshData();
    }

    private void setupNotifications() {
        // Uruchom harmonogram (ustawia AlarmManager na 6:00)
        NotificationScheduler.scheduleDailyNotification(this);

        // Poproś o uprawnienia w Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void checkAdminAccess() {
        User currentUser = controller.getCurrentUser();
        // Prosta weryfikacja - w produkcji lepiej użyć pola 'role' w bazie
        if (currentUser != null && currentUser.getEmail().startsWith("admin")) {
            btnAdminPanel.setVisibility(View.VISIBLE);
            btnAdminPanel.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }
    }

    private void refreshData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Pobieranie danych z bazy
            Statistics stats = controller.getUserStatistics();
            List<WorkoutPlan> plans = controller.getUserWorkoutPlans();
            List<Notification> notifs = controller.getUserNotifications();

            runOnUiThread(() -> {
                // Aktualizacja UI
                tvStatsWorkoutCount.setText("Treningi: " + stats.getTotalWorkouts());
                // Wyświetlamy datę aktualizacji lub ogólne info, bo 1RM jest teraz w osobnym ekranie
                tvStatsMax.setText("Ostatnia aktywność: " + (stats.getLastUpdate() != null ? stats.getLastUpdate().toString().substring(0, 10) : "-"));

                if (!notifs.isEmpty()) {
                    tvNotification.setText("🔔 " + notifs.get(0).getMessage());
                } else {
                    tvNotification.setText("Brak nowych powiadomień");
                }

                renderPlans(plans);
            });
        });
    }

    private void renderPlans(List<WorkoutPlan> plans) {
        plansContainer.removeAllViews();

        if (plans.isEmpty()) {
            TextView info = new TextView(this);
            info.setText("Nie masz jeszcze planów. Utwórz pierwszy!");
            info.setPadding(0, 20, 0, 20);
            info.setGravity(Gravity.CENTER);
            plansContainer.addView(info);
            return;
        }

        for (WorkoutPlan plan : plans) {
            // Główna karta planu
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 32);
            card.setLayoutParams(params);
            card.setRadius(16f);
            card.setCardElevation(8f);
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(32, 32, 32, 32);

            // Tytuł planu
            TextView title = new TextView(this);
            title.setText(plan.getPlanName());
            title.setTextSize(20f);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(getColor(R.color.primary));

            // Podgląd ćwiczeń (tekst)
            TextView preview = new TextView(this);
            preview.setTextSize(14f);
            preview.setTextColor(getColor(R.color.text_secondary));
            preview.setPadding(0, 8, 0, 24);
            preview.setText("Ładowanie podglądu...");

            // --- Przyciski Akcji ---
            LinearLayout btnLayout = new LinearLayout(this);
            btnLayout.setOrientation(LinearLayout.HORIZONTAL);
            btnLayout.setWeightSum(3); // 3 przyciski

            // 1. START
            Button btnStart = new Button(this);
            btnStart.setText("START");
            btnStart.setBackgroundTintList(getColorStateList(R.color.accent));
            btnStart.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lpStart = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lpStart.setMarginEnd(8);
            btnStart.setLayoutParams(lpStart);

            // 2. DNI (Harmonogram)
            Button btnSchedule = new Button(this);
            btnSchedule.setText("DNI");
            btnSchedule.setBackgroundTintList(getColorStateList(R.color.primary));
            btnSchedule.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lpSch = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lpSch.setMarginEnd(8);
            btnSchedule.setLayoutParams(lpSch);

            // 3. USUŃ
            Button btnDelete = new Button(this);
            btnDelete.setText("USUŃ");
            btnDelete.setBackgroundTintList(getColorStateList(R.color.background)); // Szary
            btnDelete.setTextColor(getColor(R.color.text_secondary));
            btnDelete.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            // --- Logika Przycisków ---

            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, SessionActivity.class);
                intent.putExtra("PLAN_ID", plan.getId());
                intent.putExtra("PLAN_NAME", plan.getPlanName());
                startActivity(intent);
            });

            btnSchedule.setOnClickListener(v -> showDaySelectionDialog(plan));

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Usunąć plan?")
                        .setMessage("Czy na pewno chcesz usunąć plan '" + plan.getPlanName() + "'?")
                        .setPositiveButton("Usuń", (d, w) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                controller.deleteWorkoutPlan(plan.getId());
                                runOnUiThread(this::refreshData);
                            });
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
            });

            // Ładowanie podglądu ćwiczeń w tle
            loadPlanPreview(plan.getId(), preview);

            // Składanie widoku
            btnLayout.addView(btnStart);
            btnLayout.addView(btnSchedule);
            btnLayout.addView(btnDelete);

            content.addView(title);
            content.addView(preview);
            content.addView(btnLayout);
            card.addView(content);
            plansContainer.addView(card);
        }
    }

    private void loadPlanPreview(int planId, TextView targetView) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<PlanItem> items = controller.getPlanDetails(planId);
            StringBuilder sb = new StringBuilder();

            if (items.isEmpty()) {
                sb.append("Brak ćwiczeń w planie.");
            } else {
                for (int i = 0; i < Math.min(items.size(), 3); i++) {
                    String name = items.get(i).getExerciseDetails() != null ?
                            items.get(i).getExerciseDetails().getName() : "Ćwiczenie";
                    sb.append("• ").append(name).append("\n");
                }
                if (items.size() > 3) sb.append("...i ").append(items.size() - 3).append(" więcej");
            }

            runOnUiThread(() -> targetView.setText(sb.toString().trim()));
        });
    }

    private void showDaySelectionDialog(WorkoutPlan plan) {
        String[] days = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela"};
        boolean[] checkedItems = new boolean[days.length];
        List<String> selectedDays = new ArrayList<>();

        new AlertDialog.Builder(this)
                .setTitle("Kiedy ćwiczysz: " + plan.getPlanName() + "?")
                .setMultiChoiceItems(days, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) selectedDays.add(days[which]);
                    else selectedDays.remove(days[which]);
                })
                .setPositiveButton("Zapisz Harmonogram", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        // Wymaga dodania metody assignPlanToDays w Controllerze (pętla po dniach)
                        for(String day : selectedDays) {
                            controller.assignPlanToDay(plan.getId(), day);
                        }
                        runOnUiThread(() -> Toast.makeText(this, "Zapisano dni treningowe!", Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }
}