package pl.fitnesstracker.model;

import java.math.BigDecimal;

public class ExerciseStatsDTO {
    private String exerciseName;
    private String type;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;

    public ExerciseStatsDTO(String exerciseName, String type, BigDecimal maxWeight, BigDecimal maxVolume) {
        this.exerciseName = exerciseName;
        this.type = type;
        this.maxWeight = maxWeight;
        this.maxVolume = maxVolume;
    }

    public String getExerciseName() {
        return exerciseName;
    }
    public String getType() {
        return type;
    }
    public BigDecimal getMaxWeight() {
        return maxWeight != null ? maxWeight : BigDecimal.ZERO;
    }

    public BigDecimal getMaxVolume() {
        return maxVolume != null ? maxVolume : BigDecimal.ZERO;
    }
}