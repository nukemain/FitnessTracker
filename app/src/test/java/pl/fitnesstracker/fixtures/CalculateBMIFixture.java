package pl.fitnesstracker.fixtures;

import pl.fitnesstracker.logic.BMICalculator;

public class CalculateBMIFixture {

    private double weight;
    private double height;

    // Metoda wywoływana przez FitNesse do ustawienia wagi
    public void setWeight(double weight) {
        this.weight = weight;
    }

    // Metoda wywoływana przez FitNesse do ustawienia wzrostu
    public void setHeight(double height) {
        this.height = height;
    }

    // Metoda wywoływana przez FitNesse do pobrania wyniku i porównania go z oczekiwanym
    public double bmi() {
        BMICalculator calculator = new BMICalculator();
        return calculator.calculate(weight, height);
    }
}
