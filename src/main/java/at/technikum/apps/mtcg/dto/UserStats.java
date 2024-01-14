package at.technikum.apps.mtcg.dto;

public class UserStats {
    private String username;
    private int eloRating;

    public UserStats() {
    }

    public UserStats(String username, int eloRating) {
        this.username = username;
        this.eloRating = eloRating;
    }

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
