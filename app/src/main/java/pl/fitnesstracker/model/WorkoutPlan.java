package pl.fitnesstracker.model;

import java.sql.Timestamp;

public class WorkoutPlan {
    private Integer id;
    private Integer userId;
    private String planName;
    private String description;
    private Timestamp createdDate;
    private Boolean active;

    public WorkoutPlan() {}

    // Gettery i Settery
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
