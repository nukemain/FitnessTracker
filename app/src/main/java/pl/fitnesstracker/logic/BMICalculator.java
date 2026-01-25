package pl.fitnesstracker.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BMICalculator {

    /**
     * Oblicza wskaźnik BMI.
     * @param weightKg Waga w kilogramach.
     * @param heightCm Wzrost w centymetrach.
     * @return Obliczony wskaźnik BMI, zaokrąglony do dwóch miejsc po przecinku.
     */
    public double calculate(double weightKg, double heightCm) {
        if (heightCm <= 0 || weightKg <= 0) {
            return 0.0;
        }

        double heightInMeters = heightCm / 100.0;
        double bmi = weightKg / (heightInMeters * heightInMeters);

        // Zaokrąglamy wynik do 2 miejsc po przecinku
        BigDecimal bd = new BigDecimal(Double.toString(bmi));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
