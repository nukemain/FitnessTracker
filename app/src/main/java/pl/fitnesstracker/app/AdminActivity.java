package pl.fitnesstracker.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.FitnessModel;
import pl.fitnesstracker.controller.AdminDeletionProcess;
import pl.fitnesstracker.model.User;

public class AdminActivity extends AppCompatActivity {

    private LinearLayout container;
    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        container = findViewById(R.id.usersListContainer);
        loadUsers();
    }

    private void loadUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<User> users = controller.getAllUsers();

            runOnUiThread(() -> {
                container.removeAllViews();
                for (User u : users) {
                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setPadding(0, 10, 0, 10);

                    TextView info = new TextView(this);
                    info.setText(u.getEmail() + " (ID:" + u.getId() + ")");
                    info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                    Button btnDel = new Button(this);
                    btnDel.setText("X");
                    btnDel.setBackgroundColor(Color.RED);
                    btnDel.setTextColor(Color.WHITE);

                    // Blokada usunięcia samego siebie
                    if(u.getId().equals(controller.getCurrentUser().getId())) {
                        btnDel.setEnabled(false);
                        btnDel.setBackgroundColor(Color.GRAY);
                    }

                    btnDel.setOnClickListener(v -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Usunąć użytkownika?")
                                .setPositiveButton("Tak", (d, w) -> deleteUser(u.getId()))
                                .setNegativeButton("Nie", null)
                                .show();
                    });

                    row.addView(info);
                    row.addView(btnDel);
                    container.addView(row);
                }
            });
        });
    }

    private void deleteUser(int userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            new AdminDeletionProcess(new FitnessModel(), null).executeUserDeletion(userId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Usunięto.", Toast.LENGTH_SHORT).show();
                loadUsers();
            });
        });
    }
}