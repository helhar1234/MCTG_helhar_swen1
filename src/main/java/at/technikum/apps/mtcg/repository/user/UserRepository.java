package at.technikum.apps.mtcg.repository.user;

import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;

import java.sql.SQLException;
import java.util.Optional;
// TODO: ADD COMMENTS & MAKE MORE ÜBERSICHTLICH

public interface UserRepository {

    Optional<User> saveUser(User user) throws SQLException;

    boolean isUsernameExists(String username) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    UserData updateUserData(String id, UserData userData) throws SQLException;

    boolean updateCoins(String userId, int price) throws SQLException;

    boolean addCardToStack(String userId, Card card) throws SQLException;

    Optional<User> findUserById(String id) throws SQLException;

    boolean updateELO(String userId, int eloToAdd) throws SQLException;

}
