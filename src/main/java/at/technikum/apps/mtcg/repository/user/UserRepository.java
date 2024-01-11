package at.technikum.apps.mtcg.repository.user;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;

import java.util.Optional;
// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public interface UserRepository {

    Optional<User> saveUser(User user);

    boolean isUsernameExists(String username);

    Optional<User> findByUsername(String username);

    UserData updateUserData(String id, UserData userData);

    boolean updateCoins(String userId, int price);

    boolean addCardToStack(String userId, Card card);

    Optional<User> findUserById(String id);

    boolean updateELO(String userId, int eloToAdd);

}
