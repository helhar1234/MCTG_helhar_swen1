package at.technikum.apps.mtcg.repository.coins;

public interface CoinRepository {
    boolean updateCoins(String userId, int price);
}
