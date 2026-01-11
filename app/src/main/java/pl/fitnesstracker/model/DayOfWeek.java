package pl.fitnesstracker.model;

public class DayOfWeek {
    private Integer id;
    private Integer planId;
    private String dayName;

    public DayOfWeek() {}

    public DayOfWeek(Integer planId, String dayName) {
        this.planId = planId;
        this.dayName = dayName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }
}
