package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.entity.*;

import java.util.Optional;
// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH
// TODO: MAKE SESSION-REPO SEPERATE
// TODO: MAKE STATS+SCOREBOARD-REPO SEPERATE

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

    Optional<User> findUserById(String id);

    boolean updateELO(String userId, int eloToAdd);

    int getUserWins(String id);

    int getUserBattles(String id);
}
