package pl.fitnesstracker.app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;
import java.util.concurrent.Executors;

import pl.fitnesstracker.R;
import pl.fitnesstracker.controller.FitnessSystemController;
import pl.fitnesstracker.model.ExerciseStatsDTO;

public class ExerciseStatsActivity extends AppCompatActivity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_stats);
        container = findViewById(R.id.statsContainer);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExerciseStatsDTO> stats = FitnessSystemController.getInstance().getAllExerciseStats();

            runOnUiThread(() -> {
                for(ExerciseStatsDTO s : stats) {
                    CardView card = new CardView(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                    lp.setMargins(0,0,0,24);
                    card.setLayoutParams(lp);
                    card.setRadius(12f);
                    card.setCardElevation(6f);
                    card.setContentPadding(24,24,24,24);

                    LinearLayout content = new LinearLayout(this);
                    content.setOrientation(LinearLayout.VERTICAL);

                    TextView name = new TextView(this);
                    name.setText(s.getExerciseName());
                    name.setTextSize(18f);
                    name.setTypeface(null, android.graphics.Typeface.BOLD);
                    name.setTextColor(getColor(R.color.primary));

                    TextView data = new TextView(this);
                    boolean isCardio = "Cardio".equalsIgnoreCase(s.getType());

                    if (isCardio) {
                        // Cardio tylko czas
                        data.setText("⏱ Rekord Czasu: " + s.getMaxWeight() + " min");
                    } else {
                        // Siła Max Ciężar i Objętość
                        // emoji w kodzie wyglądaja dziwnie ale działa
                        data.setText("🏆 Max Ciężar: " + s.getMaxWeight() + " kg\n" +
                                "🏋️ Max Objętość: " + s.getMaxVolume() + " kg");
                    }
                    data.setPadding(0,12,0,0);

                    content.addView(name);
                    content.addView(data);
                    card.addView(content);
                    container.addView(card);
                }
            });
        });
    }
}