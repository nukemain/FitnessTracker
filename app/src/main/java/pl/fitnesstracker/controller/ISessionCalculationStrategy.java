package pl.fitnesstracker.controller;

import pl.fitnesstracker.model.SessionRecord;

public interface ISessionCalculationStrategy {
    // Metoda wywoływana po dodaniu każdej serii/ćwiczenia
    void calculateAndPersist(int userId, SessionRecord record);

    // Metoda wywoływana przy zakończeniu sesji (dla Cardio - czas)
    void finalizeSessionStats(int userId, String duration);
}