package pl.fitnesstracker.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.TrainingSession;
import pl.fitnesstracker.model.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserEmail, tvUserWeight, tvUserGoal;
    private Button btnEditGoal, btnDeleteAccount, btnAllStats, btnEditWeight;
    private LinearLayout historyContainer;

    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserWeight = findViewById(R.id.tvUserWeight);
        tvUserGoal = findViewById(R.id.tvUserGoal);
        btnEditGoal = findViewById(R.id.btnEditGoal);
        btnEditWeight = findViewById(R.id.btnEditWeight);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnAllStats = findViewById(R.id.btnAllStats);
        historyContainer = findViewById(R.id.historyContainer);

        if (btnEditWeight != null) {
            btnEditWeight.setOnClickListener(v -> showEditWeightDialog());
        } else {
            tvUserWeight.setOnClickListener(v -> showEditWeightDialog());
            tvUserWeight.setHint("Kliknij, aby zmienić wagę");
        }

        refreshData();

        btnEditGoal.setOnClickListener(v -> showEditGoalDialog());

        btnAllStats.setOnClickListener(v ->
                startActivity(new Intent(this, ExerciseStatsActivity.class))
        );

        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void refreshData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            User u = controller.getCurrentUser();
            if (u == null) return;
            
            List<TrainingSession> history = controller.getCompletedSessions(u.getId());

            runOnUiThread(() -> {
                tvUserEmail.setText("Email: " + u.getEmail());
                tvUserWeight.setText("Waga: " + u.getWeight() + " kg");
                tvUserGoal.setText("Cel: " + u.getTrainingGoal());
                renderHistoryList(history);
            });
        });
    }

    private void showEditWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zaktualizuj Wagę");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        User u = controller.getCurrentUser();
        if (u != null && u.getWeight() != null) {
            input.setText(u.getWeight().toString());
        }

        builder.setView(input);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String weightStr = input.getText().toString();
            if (!weightStr.isEmpty()) {
                try {
                    BigDecimal newWeight = new BigDecimal(weightStr);
                    Executors.newSingleThreadExecutor().execute(() -> {
                        controller.updateUserData(newWeight, controller.getCurrentUser().getTrainingGoal());
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Waga zaktualizowana!", Toast.LENGTH_SHORT).show();
                            refreshData();
                        });
                    });
                } catch (Exception e) {
                    Toast.makeText(this, "Błędny format wagi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }

    private void renderHistoryList(List<TrainingSession> history) {
        historyContainer.removeAllViews();

        if (history.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Brak zakończonych treningów.");
            tv.setPadding(0, 20, 0, 0);
            tv.setGravity(Gravity.CENTER);
            historyContainer.addView(tv);
            return;
        }

        for (TrainingSession s : history) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);

            // Pobranie koloru z motywu
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
            rowLayout.setBackgroundColor(typedValue.data);
            rowLayout.setPadding(20, 20, 20, 20);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, 4);
            rowLayout.setLayoutParams(rowParams);

            TextView infoText = new TextView(this);
            String date = s.getSessionDate() != null ? s.getSessionDate().toString() : "Data nieznana";
            String duration = s.getDuration() != null ? s.getDuration() : "--:--";

            infoText.setText("📅 " + date + "\n⏱ " + duration);
            infoText.setTextSize(16f);
            infoText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            infoText.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SessionDetailsActivity.class);
                intent.putExtra("SESSION_ID", s.getId());
                intent.putExtra("SESSION_DATE", date);
                startActivity(intent);
            });

            Button btnDelete = new Button(this);
            btnDelete.setText("X");
            btnDelete.setTextColor(Color.WHITE);
            btnDelete.setBackgroundTintList(getColorStateList(R.color.error)); 
            
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(100, 100);
            btnParams.setMarginStart(16);
            btnDelete.setLayoutParams(btnParams);

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Usuń trening")
                        .setMessage("Czy na pewno usunąć trening z dnia " + date + "?")
                        .setPositiveButton("Usuń", (d, w) -> deleteSession(s.getId()))
                        .setNegativeButton("Anuluj", null)
                        .show();
            });

            rowLayout.addView(infoText);
            rowLayout.addView(btnDelete);
            historyContainer.addView(rowLayout);
        }
    }

    private void deleteSession(int sessionId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            controller.deleteTrainingSession(sessionId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Trening usunięty", Toast.LENGTH_SHORT).show();
                refreshData();
            });
        });
    }

    private void showEditGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zmień Cel Treningowy");

        final Spinner goalSpinner = new Spinner(this);
        String[] goals = {"Siła", "Cardio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        goalSpinner.setAdapter(adapter);

        User u = controller.getCurrentUser();
        if (u != null && u.getTrainingGoal() != null && u.getTrainingGoal().equalsIgnoreCase("Cardio")) {
            goalSpinner.setSelection(1);
        } else {
            goalSpinner.setSelection(0);
        }

        builder.setView(goalSpinner);

        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String newGoal = goalSpinner.getSelectedItem().toString();
            Executors.newSingleThreadExecutor().execute(() -> {
                controller.updateUserGoal(newGoal);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Cel zaktualizowany!", Toast.LENGTH_SHORT).show();
                    refreshData(); 
                });
            });
        });

        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Usuwanie Konta")
                .setMessage("Czy na pewno chcesz usunąć konto? Wszystkie dane (plany, historia, statystyki) zostaną utracone bezpowrotnie.")
                .setPositiveButton("TAK, USUŃ", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        controller.deleteAccount();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Konto usunięte.", Toast.LENGTH_LONG).show();
                            Intent i = new Intent(this, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        });
                    });
                })
                .setNegativeButton("Anuluj", null)
                .show();
    }
}
