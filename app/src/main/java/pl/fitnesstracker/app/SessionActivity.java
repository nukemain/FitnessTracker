package pl.fitnesstracker.app;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.Exercise;
import pl.fitnesstracker.model.PlanItem;
import pl.fitnesstracker.model.SessionRecord;

public class SessionActivity extends AppCompatActivity {

    private Chronometer timer;
    private TextView tvPlanName;
    private EditText etSessionNote;
    private LinearLayout exercisesContainer;
    private Button btnFinishSession;

    private final FitnessSystemController controller = FitnessSystemController.getInstance();
    private int planId;
    private int editSessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        timer = findViewById(R.id.timer);
        tvPlanName = findViewById(R.id.tvSessionPlanName);
        etSessionNote = findViewById(R.id.etSessionNote);
        exercisesContainer = findViewById(R.id.exercisesContainer);
        btnFinishSession = findViewById(R.id.btnFinishSession);

        planId = getIntent().getIntExtra("PLAN_ID", -1);
        editSessionId = getIntent().getIntExtra("EDIT_SESSION_ID", -1);

        if (editSessionId != -1) {
            tvPlanName.setText("Edycja Treningu");
            btnFinishSession.setText("ZAPISZ ZMIANY");
            loadExistingSession();
        } else {
            String planName = getIntent().getStringExtra("PLAN_NAME");
            tvPlanName.setText(planName != null ? planName : "Trening");
            startNewSessionAndLoadExercises();
        }

        btnFinishSession.setOnClickListener(v -> executeFinish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new android.app.AlertDialog.Builder(SessionActivity.this)
                        .setTitle("Wyjście")
                        .setMessage("Czy na pewno chcesz wyjść? Niezapisane zmiany zostaną utracone.")
                        .setPositiveButton("Wyjdź", (dialog, which) -> finish())
                        .setNegativeButton("Zostań", null)
                        .show();
            }
        });
    }

    private void startNewSessionAndLoadExercises() {
        Executors.newSingleThreadExecutor().execute(() -> {
            controller.startSession(planId);
            List<PlanItem> planItems = controller.getPlanDetails(planId);
            runOnUiThread(() -> {
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();
                if (planItems.isEmpty()) {
                    Toast.makeText(this, "Plan jest pusty!", Toast.LENGTH_LONG).show();
                } else {
                    for (PlanItem item : planItems) {
                        addExerciseView(item.getExerciseDetails(), null);
                    }
                }
            });
        });
    }

    private void loadExistingSession() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SessionRecord> records = controller.getSessionDetails(editSessionId);
            runOnUiThread(() -> {
                timer.setVisibility(View.GONE); // Ukrywamy stoper w trybie edycji
                for (SessionRecord record : records) {
                    addExerciseView(record.getExerciseDetails(), record);
                }
            });
        });
    }

    private void addExerciseView(Exercise ex, SessionRecord record) {
        if (ex == null) return;
        View view = getLayoutInflater().inflate(R.layout.item_session_exercise, null);
        TextView tvName = view.findViewById(R.id.tvExName);
        EditText etSets = view.findViewById(R.id.etExSets);
        EditText etReps = view.findViewById(R.id.etExReps);
        EditText etWeight = view.findViewById(R.id.etExWeight);
        Button btnSave = view.findViewById(R.id.btnExSave);

        tvName.setText(ex.getName());

        boolean isCardio = "Cardio".equalsIgnoreCase(ex.getCategory()) || "Cardio".equalsIgnoreCase(ex.getType());
        if (isCardio) {
            etReps.setVisibility(View.GONE);
            etSets.setVisibility(View.GONE);
            etWeight.setHint("Czas (min)");
        } else {
            etWeight.setHint("Kg");
            etSets.setHint("Serie");
        }

        if (record != null) {
            etSets.setText(String.valueOf(record.getSets()));
            etReps.setText(String.valueOf(record.getReps()));
            etWeight.setText(String.valueOf(record.getWeight()));
            btnSave.setText("ZAKTUALIZUJ");
            view.setBackgroundResource(R.color.success_highlight);
        }

        btnSave.setOnClickListener(v -> {
            String sSets = etSets.getText().toString();
            String sReps = etReps.getText().toString();
            String sWeight = etWeight.getText().toString();

            try {
                int sets = isCardio ? 1 : Integer.parseInt(sSets);
                int reps = isCardio ? 0 : Integer.parseInt(sReps);
                double weightOrTime = Double.parseDouble(sWeight);

                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean success;
                    if (editSessionId != -1 && record != null) {
                        // Tryb edycji - aktualizujemy istniejący rekord
                        success = controller.updateSet(record.getId(), sets, reps, weightOrTime);
                    } else {
                        // Tryb nowej sesji - dodajemy nowy rekord
                        success = controller.logSet(ex.getId(), sets, reps, weightOrTime);
                    }

                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Zapisano: " + ex.getName(), Toast.LENGTH_SHORT).show();
                            view.setBackgroundResource(R.color.success_highlight);
                            btnSave.setText("ZAKTUALIZUJ");
                        } else {
                            Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Wpisz poprawne liczby", Toast.LENGTH_SHORT).show();
            }
        });

        exercisesContainer.addView(view);
    }

    private void executeFinish() {
        if (editSessionId != -1) {
            // W trybie edycji po prostu zamykamy
            Toast.makeText(this, "Zmiany zapisane!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Logika dla kończenia nowej sesji
        long elapsedMillis = SystemClock.elapsedRealtime() - timer.getBase();
        String duration = String.format("%02d:%02d:%02d", 
                (elapsedMillis / (1000*60*60)) % 24,
                (elapsedMillis / (1000*60)) % 60,
                (elapsedMillis / 1000) % 60);

        String noteContent = etSessionNote.getText().toString();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (!noteContent.isEmpty()) {
                controller.addNoteToSession(noteContent);
            }
            controller.endSession(duration);
            runOnUiThread(this::finish);
        });
    }
}
