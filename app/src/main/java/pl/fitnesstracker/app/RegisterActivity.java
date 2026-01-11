package pl.fitnesstracker.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.dao.DatabaseConnector;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPass, etWeight, etHeight;
    private Spinner spinnerGoal;
    private Button btnRegister;
    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegEmail);
        etPass = findViewById(R.id.etRegPassword);
        etWeight = findViewById(R.id.etRegWeight);
        etHeight = findViewById(R.id.etRegHeight);
        spinnerGoal = findViewById(R.id.spinnerRegGoal);
        btnRegister = findViewById(R.id.btnRegisterConfirm);

        String[] goals = {"Siła", "Cardio"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, goals);
        spinnerGoal.setAdapter(adapter);

        // Sprawdzenie połączenia przy wejściu
        checkDatabaseConnection();

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String goal = spinnerGoal.getSelectedItem().toString();
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            // WALIDACJA
            if (email.isEmpty()) {
                etEmail.setError("Wymagane"); return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Niepoprawny format email"); return;
            }
            if (pass.length() < 4) {
                etPass.setError("Hasło za krótkie (min 4)"); return;
            }
            if (weightStr.isEmpty()) {
                etWeight.setError("Wymagane"); return;
            }
            if (heightStr.isEmpty()) {
                etHeight.setError("Wymagane"); return;
            }

            try {
                BigDecimal weight = new BigDecimal(weightStr);
                Integer height = Integer.parseInt(heightStr);

                Executors.newSingleThreadExecutor().execute(() -> {
                    boolean success = controller.register(email, pass, weight, height, goal);
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Sukces! Zaloguj się.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Błąd rejestracji (email zajęty?)", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Waga/Wzrost muszą być liczbami!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDatabaseConnection() {
        // Blokujemy przycisk na czas sprawdzania
        btnRegister.setEnabled(false);
        
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean connected = DatabaseConnector.isConnected();
            
            runOnUiThread(() -> {
                if (connected) {
                    btnRegister.setEnabled(true);
                } else {
                    Toast.makeText(this, "Brak połączenia z bazą danych! Spróbuj ponownie później.", Toast.LENGTH_LONG).show();
                    btnRegister.setEnabled(false);
                }
            });
        });
    }
}
