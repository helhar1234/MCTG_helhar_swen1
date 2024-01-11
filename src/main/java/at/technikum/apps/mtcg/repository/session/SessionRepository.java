package at.technikum.apps.mtcg.repository.session;

import at.technikum.apps.mtcg.entity.User;

import java.util.Optional;

public interface SessionRepository {
    Optional<String> findTokenByUserId(String userId);

    boolean deleteToken(String userId);

    Optional<User> findByToken(String token);

    Optional<String> generateToken(User user);

    boolean authenticateToken(String token);
}
