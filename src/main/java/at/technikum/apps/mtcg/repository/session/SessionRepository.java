package at.technikum.apps.mtcg.repository.session;

import at.technikum.apps.mtcg.entity.User;

import java.sql.SQLException;
import java.util.Optional;

public interface SessionRepository {
    Optional<String> findTokenByUserId(String userId) throws SQLException;

    boolean deleteToken(String userId) throws SQLException;

    Optional<User> findByToken(String token) throws SQLException;

    Optional<String> generateToken(User user) throws SQLException;

    boolean authenticateToken(String token) throws SQLException;
}
