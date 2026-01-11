package pl.fitnesstracker.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class User {
    private Integer id;
    private String email;
    private String password; // Hash
    private BigDecimal weight;
    private Integer height;
    private String trainingGoal;
    private Timestamp registrationDate;
    private String role = "user"; // Domyślnie "user"

    public User() {}

    public User(String email, String password, BigDecimal weight, Integer height, String trainingGoal) {
        this.email = email;
        this.password = password;
        this.weight = weight;
        this.height = height;
        this.trainingGoal = trainingGoal;
        this.role = "user";
    }


    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public String getTrainingGoal() { return trainingGoal; }
    public void setTrainingGoal(String trainingGoal) { this.trainingGoal = trainingGoal; }

    public Timestamp getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Timestamp registrationDate) { this.registrationDate = registrationDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", trainingGoal='" + trainingGoal + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
