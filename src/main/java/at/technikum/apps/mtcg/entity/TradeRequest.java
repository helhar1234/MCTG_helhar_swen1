package at.technikum.apps.mtcg.entity;

public class TradeRequest {
    private String id;
    private String userId;
    private String username;
    private String cardToTrade;
    private String cardName;
    private int cardDamage;
    private String type;
    private int minimumDamage;

    public TradeRequest() {
    }

    public TradeRequest(String id, String cardToTrade, String type, int minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type;
        this.minimumDamage = minimumDamage;
    }

    public TradeRequest(String id, String userId, String username, String cardToTrade, String cardName, int cardDamage, String type, int minimumDamage) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.cardToTrade = cardToTrade;
        this.cardName = cardName;
        this.cardDamage = cardDamage;
        this.type = type;
        this.minimumDamage = minimumDamage;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardToTrade() {
        return cardToTrade;
    }

    public void setCardToTrade(String cardToTrade) {
        this.cardToTrade = cardToTrade;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMinimumDamage(int minimumDamage) {
        this.minimumDamage = minimumDamage;
    }

    public int getMinimumDamage() {
        return minimumDamage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public int getCardDamage() {
        return cardDamage;
    }

    public void setCardDamage(int cardDamage) {
        this.cardDamage = cardDamage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
