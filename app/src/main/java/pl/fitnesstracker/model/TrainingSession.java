package pl.fitnesstracker.model;

import java.sql.Date;

public class TrainingSession {
    private Integer id;
    private Integer userId;
    private Integer planId;
    private Date sessionDate;
    private String duration;
    private String status;

    public TrainingSession() {}

    // Gettery i Settery
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }

    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
