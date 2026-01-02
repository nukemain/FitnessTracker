package pl.fitnesstracker.model;

public class Exercise {
    private Integer id;
    private Integer userId;
    private String name;
    private String description;
    private String category;
    private String type;

    public Exercise() {}

    public Exercise(Integer userId, String name, String description, String category, String type) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.type = type;
    }

    // Gettery i Settery
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
