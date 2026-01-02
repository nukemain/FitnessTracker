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
    private LinearLayout exercisesContainer; // Kontener na listę ćwiczeń
    private Button btnFinishSession;

    private final FitnessSystemController controller = FitnessSystemController.getInstance();
    private int planId;

    private final java.util.Set<Integer> loggedExerciseIds = new java.util.HashSet<>();
    private int totalExercisesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // Powiązanie widoków
        timer = findViewById(R.id.timer);
        tvPlanName = findViewById(R.id.tvSessionPlanName);
        etSessionNote = findViewById(R.id.etSessionNote);
        exercisesContainer = findViewById(R.id.exercisesContainer);
        btnFinishSession = findViewById(R.id.btnFinishSession);

        // Pobranie danych z Intentu
        planId = getIntent().getIntExtra("PLAN_ID", -1);
        String planName = getIntent().getStringExtra("PLAN_NAME");
        tvPlanName.setText(planName != null ? planName : "Trening");

        // Start sesji i ładowanie ćwiczeń
        startSessionAndLoadExercises();

        // Obsługa zakończenia
        btnFinishSession.setOnClickListener(v -> finishSession());
    }

    private void startSessionAndLoadExercises() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Rozpocznij sesję w bazie
            controller.startSession(planId);

            // 2. Pobierz ćwiczenia przypisane do tego planu
            List<PlanItem> planItems = controller.getPlanDetails(planId);
            totalExercisesCount = planItems.size();

            runOnUiThread(() -> {
                // Start stopera
                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();

                // 3. Wygeneruj widoki dla każdego ćwiczenia
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

        // wiersz z ćwiczeniem
        View view = getLayoutInflater().inflate(R.layout.item_session_exercise, null);

        // Znajdujemy elementy wewnątrz tego wiersza
        TextView tvName = view.findViewById(R.id.tvExName);
        EditText etSets = view.findViewById(R.id.etExSets);
        EditText etReps = view.findViewById(R.id.etExReps);
        EditText etWeight = view.findViewById(R.id.etExWeight);
        Button btnSave = view.findViewById(R.id.btnExSave);

        // Ustawiamy nazwę
        tvName.setText(ex.getName());

        // --- LOGIKA SIŁA vs CARDIO ---
        // Sprawdzamy typ ćwiczenia
        boolean isCardio = "Cardio".equalsIgnoreCase(ex.getCategory()) || "Cardio".equalsIgnoreCase(ex.getType());

        if (isCardio) {
            // Dostosowanie pod Cardio
            etReps.setVisibility(View.GONE);       // Ukrywamy powtórzenia
            etSets.setVisibility(View.GONE);       // Ukrywamy serie
            etWeight.setHint("Czas (min)");        // Zmieniamy hint Kg -> Czas
            etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            // Standard dla Siły
            etReps.setVisibility(View.VISIBLE);
            etSets.setVisibility(View.VISIBLE);
            etWeight.setHint("Kg");
            etSets.setHint("Serie");
        }

        // Obsługa przycisku "Zapisz Serię"
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

            // Parsowanie danych
            try {
                int sets = isCardio ? 1 : Integer.parseInt(sSets);
                int reps = isCardio ? 0 : Integer.parseInt(sReps);
                double weightOrTime = Double.parseDouble(sWeight);

                // Zapis w tle
                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean success = controller.logSet(ex.getId(), sets, reps, weightOrTime);

                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Zapisano: " + ex.getName(), Toast.LENGTH_SHORT).show();
                            // Wizualne potwierdzenie (zmienia tło na jasnozielone)
                            view.setBackgroundColor(0xFFE8F5E9);
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

        // Dodajemy gotowy wiersz do kontenera na ekranie
        exercisesContainer.addView(view);
    }

    private void finishSession() {
        // Sprawdź czy użytkownik zapisał wyniki dla wszystkich ćwiczeń
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