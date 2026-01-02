package pl.fitnesstracker.model;

import java.sql.Timestamp;

public class Note {
    private Integer id;
    private Integer sessionId;
    private String content; // tresc_notatki
    private Timestamp dateAdded; // data_dodania

    public Note() {}

    public Note(Integer sessionId, String content) {
        this.sessionId = sessionId;
        this.content = content;
    }

    // Gettery i Settery
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Timestamp dateAdded) {
        this.dateAdded = dateAdded;
    }
}
