package pl.fitnesstracker.model;

import java.math.BigDecimal;

public class SessionRecord {
    private Integer id;
    private Integer sessionId;
    private Integer exerciseId;
    private Exercise exerciseDetails;
    private Integer sets;
    private Integer reps;
    private BigDecimal weight;

    public SessionRecord() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Exercise getExerciseDetails() {
        return exerciseDetails;
    }

    public void setExerciseDetails(Exercise exerciseDetails) {
        this.exerciseDetails = exerciseDetails;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
