package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.apps.mtcg.entity.UserStats;

import java.util.Optional;

public interface UserRepository {
    boolean authenticateToken(String token);

    boolean saveUser(User user);

    boolean isUsernameExists(String username);

    Optional<User> findByUsername(String username);

    Optional<String> generateToken(User user);

    UserData updateUserData(String id, UserData userData);

    Optional<User> findByToken(String token);

    boolean updateCoins(String userId, int price);

    boolean addCardToStack(String userId, Card card);

    UserStats[] getScoreboard();

    Optional<String> findTokenByUserId(String userId);

    boolean deleteToken(String userId);
}
