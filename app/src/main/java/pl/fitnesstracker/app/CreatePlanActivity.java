package pl.fitnesstracker.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.Exercise;

public class CreatePlanActivity extends AppCompatActivity {

    private EditText etPlanName;
    private Spinner spinnerExercises;
    private Button btnAddToList, btnSaveAll, btnNewExercise;
    private LinearLayout addedExercisesContainer;

    private final FitnessSystemController controller = FitnessSystemController.getInstance();
    private List<Exercise> availableExercises = new ArrayList<>();
    private final List<Exercise> tempPlanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        etPlanName = findViewById(R.id.etPlanName);
        spinnerExercises = findViewById(R.id.spinnerExercises);
        btnAddToList = findViewById(R.id.btnAddToList);
        btnSaveAll = findViewById(R.id.btnSaveAll);
        btnNewExercise = findViewById(R.id.btnNewExercise);
        addedExercisesContainer = findViewById(R.id.addedExercisesContainer);

        loadExercises();

        btnAddToList.setOnClickListener(v -> {
            int idx = spinnerExercises.getSelectedItemPosition();
            if (idx >= 0 && !availableExercises.isEmpty()) {
                Exercise selected = availableExercises.get(idx);
                tempPlanList.add(selected);
                refreshPreview();
            }
        });

        btnSaveAll.setOnClickListener(v -> {
            btnSaveAll.setEnabled(false);

            String name = etPlanName.getText().toString().trim();

            if (name.isEmpty()) {
                etPlanName.setError("Podaj nazwę!");
                btnSaveAll.setEnabled(true);
                return;
            }
            if (tempPlanList.isEmpty()) {
                Toast.makeText(this, "Dodaj min. 1 ćwiczenie!", Toast.LENGTH_SHORT).show();
                btnSaveAll.setEnabled(true);
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                Integer planId = controller.createWorkoutPlan(name, "Utworzony w aplikacji");

                for (Exercise ex : tempPlanList) {
                    controller.addExerciseToPlan(planId, ex.getId(), 3, 10, 0);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Plan zapisany!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        btnNewExercise.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void loadExercises() {
        Executors.newSingleThreadExecutor().execute(() -> {
            availableExercises = controller.getAvailableExercises();
            List<String> names = availableExercises.stream().map(Exercise::getName).collect(Collectors.toList());
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
                spinnerExercises.setAdapter(adapter);
            });
        });
    }

    private void refreshPreview() {
        addedExercisesContainer.removeAllViews();
        for (int i = 0; i < tempPlanList.size(); i++) {
            Exercise ex = tempPlanList.get(i);
            TextView tv = new TextView(this);
            tv.setText((i + 1) + ". " + ex.getName());
            tv.setTextSize(16f);
            tv.setPadding(0, 8, 0, 8);
            addedExercisesContainer.addView(tv);
        }
    }

    private void showAddExerciseDialog() {
        // To od customowych ćwiczeń
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Stwórz Własne Ćwiczenie");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputName = new EditText(this);
        inputName.setHint("Nazwa ćwiczenia");
        layout.addView(inputName);

        final Spinner typeSpinner = new Spinner(this);
        String[] types = {"Siła", "Cardio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        typeSpinner.setAdapter(adapter);
        layout.addView(typeSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();

            if (name.isEmpty()) return;

            Executors.newSingleThreadExecutor().execute(() -> {
                Integer success = controller.addCustomExerciseToLibrary(name, "Własne", type, type);

                runOnUiThread(() -> {
                    if (success == null) {
                        Toast.makeText(this, "Błąd dodawania", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Dodano!", Toast.LENGTH_SHORT).show();
                        loadExercises();
                    }
                });
            });
        });
        builder.setNegativeButton("Anuluj", null);
        builder.show();
    }
}
