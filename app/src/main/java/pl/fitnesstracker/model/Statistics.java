package pl.fitnesstracker.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Statistics {
    private Integer id;
    private Integer userId;
    private Timestamp lastUpdate;     // data_aktualizacji
    private BigDecimal maxWeight;     // ciezar
    private Integer totalReps;        // liczba_powtorzen
    private Integer totalWorkouts;    // liczba_treningow
    private Integer totalRecords;     // liczba_rekordow
    private Integer caloriesBurned;
    public Statistics() {}

    // Gettery i Settery
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(BigDecimal maxWeight) {
        this.maxWeight = maxWeight;
    }

    public Integer getTotalReps() {
        return totalReps;
    }

    public void setTotalReps(Integer totalReps) {
        this.totalReps = totalReps;
    }

    public Integer getTotalWorkouts() {
        return totalWorkouts;
    }

    public void setTotalWorkouts(Integer totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }
    public Integer getCaloriesBurned() {
        return caloriesBurned != null ? caloriesBurned : 0;
    }
    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
}
