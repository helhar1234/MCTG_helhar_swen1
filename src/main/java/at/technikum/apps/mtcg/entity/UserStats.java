package at.technikum.apps.mtcg.entity;

public class UserStats {
    private String username;
    private int eloRating;

    public UserStats(){}

    // Constructor
    public UserStats(String username, int eloRating) {
        this.username = username;
        this.eloRating = eloRating;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getEloRating() {
        return eloRating;
    }

    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }

}
