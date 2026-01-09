package pl.fitnesstracker.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.Note;
import pl.fitnesstracker.model.SessionRecord;

public class SessionDetailsActivity extends AppCompatActivity {

    private LinearLayout recordsContainer;
    private TextView tvNotes, tvDateTitle;
    private final FitnessSystemController controller = FitnessSystemController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        recordsContainer = findViewById(R.id.recordsContainer);
        tvNotes = findViewById(R.id.tvNotes);
        tvDateTitle = findViewById(R.id.tvDateTitle);
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        int sessionId = getIntent().getIntExtra("SESSION_ID", -1);
        String date = getIntent().getStringExtra("SESSION_DATE");
        tvDateTitle.setText("Trening: " + date);

        // Obsługa przycisku EDYTUJ
        findViewById(R.id.btnEditSession).setOnClickListener(v -> {
            Intent intent = new Intent(SessionDetailsActivity.this, SessionActivity.class);
            intent.putExtra("EDIT_SESSION_ID", sessionId); // Przekazujemy ID sesji w trybie edycji
            startActivity(intent);
            finish(); // Zamykamy szczegóły, bo wrócimy tu po edycji
        });

        loadDetails(sessionId);
    }

    private void loadDetails(int sessionId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SessionRecord> records = controller.getSessionDetails(sessionId);
            List<Note> notes = controller.getSessionNotes(sessionId);

            runOnUiThread(() -> {
                for(SessionRecord r : records) {
                    TextView tv = new TextView(this);
                    String exerciseName = (r.getExerciseDetails() != null) ? r.getExerciseDetails().getName() : "Ćwiczenie";
                    String type = (r.getExerciseDetails() != null) ? r.getExerciseDetails().getType() : "";
                    String category = (r.getExerciseDetails() != null) ? r.getExerciseDetails().getCategory() : "";
                    boolean isCardio = "Cardio".equalsIgnoreCase(type) || "Cardio".equalsIgnoreCase(category);

                    if (isCardio) {
                        tv.setText("• " + exerciseName + ": " + r.getWeight() + " min");
                    } else {
                        tv.setText("• " + exerciseName + ": "
                                + r.getWeight() + "kg x " + r.getReps() + " (" + r.getSets() + " serie)");
                    }

                    tv.setPadding(0, 10, 0, 10);
                    recordsContainer.addView(tv);
                }

                if(!notes.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for(Note n : notes) sb.append("- ").append(n.getContent()).append("\n");
                    tvNotes.setText(sb.toString());
                }
            });
        });
    }
}