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

public class SessionActivity extends AppCompatActivity {

    private Chronometer timer;
    private TextView tvPlanName;
    private EditText etSessionNote;
    private LinearLayout exercisesContainer;
    private Button btnFinishSession;

    private final FitnessSystemController controller = FitnessSystemController.getInstance();
    private int planId;

    private final java.util.Set<Integer> loggedExerciseIds = new java.util.HashSet<>();
    private int totalExercisesCount = 0;

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
        String planName = getIntent().getStringExtra("PLAN_NAME");
        tvPlanName.setText(planName != null ? planName : "Trening");

        startSessionAndLoadExercises();

        btnFinishSession.setOnClickListener(v -> finishSession());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new android.app.AlertDialog.Builder(SessionActivity.this)
                        .setTitle("Wyjście z treningu")
                        .setMessage("Czy na pewno chcesz przerwać trening? Postępy nie zostaną zapisane.")
                        .setPositiveButton("Wyjdź", (dialog, which) -> finish())
                        .setNegativeButton("Zostań", null)
                        .show();
            }
        });
    }

    private void startSessionAndLoadExercises() {
        Executors.newSingleThreadExecutor().execute(() -> {
            controller.startSession(planId);

            List<PlanItem> planItems = controller.getPlanDetails(planId);
            totalExercisesCount = planItems.size();

            runOnUiThread(() -> {
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();

                if (planItems.isEmpty()) {
                    Toast.makeText(this, "Plan jest pusty! Dodaj ćwiczenia w edytorze.", Toast.LENGTH_LONG).show();
                } else {
                    for (PlanItem item : planItems) {
                        addExerciseView(item.getExerciseDetails());
                    }
                }
            });
        });
    }

    private void addExerciseView(Exercise ex) {
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
            etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            etReps.setVisibility(View.VISIBLE);
            etSets.setVisibility(View.VISIBLE);
            etWeight.setHint("Kg");
            etSets.setHint("Serie");
        }

        btnSave.setOnClickListener(v -> {
            String sSets = etSets.getText().toString();
            String sReps = etReps.getText().toString();
            String sWeight = etWeight.getText().toString();

            if (!isCardio && sSets.isEmpty()) {
                etSets.setError("Wymagane"); return;
            }
            if (!isCardio && sReps.isEmpty()) {
                etReps.setError("Wymagane"); return;
            }
            if (sWeight.isEmpty()) {
                etWeight.setError("Wymagane"); return;
            }

            try {
                int sets = isCardio ? 1 : Integer.parseInt(sSets);
                int reps = isCardio ? 0 : Integer.parseInt(sReps);
                double weightOrTime = Double.parseDouble(sWeight);

                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean success = controller.logSet(ex.getId(), sets, reps, weightOrTime);

                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Zapisano: " + ex.getName(), Toast.LENGTH_SHORT).show();
                            view.setBackgroundResource(R.color.success_highlight);
                            btnSave.setText("ZAKTUALIZUJ");
                            loggedExerciseIds.add(ex.getId());
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

    private void finishSession() {
        if (loggedExerciseIds.size() < totalExercisesCount) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Niedokończony trening")
                    .setMessage("Nie zapisałeś wyników dla wszystkich ćwiczeń z planu ("
                            + loggedExerciseIds.size() + "/" + totalExercisesCount + ").\n\nCzy na pewno chcesz zakończyć?")
                    .setPositiveButton("Tak, zakończ", (dialog, which) -> executeFinish())
                    .setNegativeButton("Wróć", null)
                    .show();
        } else {
            executeFinish();
        }
    }

    private void executeFinish() {
        long elapsedMillis = SystemClock.elapsedRealtime() - timer.getBase();
        int seconds = (int) (elapsedMillis / 1000) % 60;
        int minutes = (int) ((elapsedMillis / (1000 * 60)) % 60);
        int hours   = (int) ((elapsedMillis / (1000 * 60 * 60)) % 24);
        String duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

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
