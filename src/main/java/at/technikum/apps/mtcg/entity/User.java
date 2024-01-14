package at.technikum.apps.mtcg.entity;

public class User {
    private String id;
    private String username;
    private String password;
    private int coins;
    private int eloRating;
    private boolean isAdmin;

    public User() {
    }

    public User(String id, String username, String password, int coins, int eloRating, boolean isAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.eloRating = eloRating;
        this.isAdmin = isAdmin;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
// for testing
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getEloRating() {
        return eloRating;
    }

    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
