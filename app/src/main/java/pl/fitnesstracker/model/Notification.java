package pl.fitnesstracker.model;

import java.sql.Timestamp;

public class Notification {
    private Integer id;
    private Integer userId;
    private String type;      // typ_powiadomienia
    private String message;   // tresc
    private Timestamp date;   // data_powiadomienia
    private String status;    // np. "Nowe", "Odczytane"

    public Notification() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
