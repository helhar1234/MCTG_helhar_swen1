package at.technikum.apps.mtcg.repository.session;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SessionRepository_db implements SessionRepository {
    //DB CONNECTION
    private final Database database = new Database();

    //SQL STATEMENTS
    private final String FIND_TOKEN_BY_USER_SQL = "SELECT token_name FROM access_token WHERE user_fk = ?";
    private final String SAVE_TOKEN_SQL = "INSERT INTO access_token(user_fk, token_name) VALUES (?, ?) RETURNING token_name";
    private final String DELETE_EXPIRED_TOKEN_SQL = "DELETE FROM access_token WHERE token_timestamp < (CURRENT_TIMESTAMP - INTERVAL '20 MINUTE')";
    private final String AUTH_TOKEN_SQL = "SELECT * FROM access_token WHERE token_name = ? AND token_timestamp >= (CURRENT_TIMESTAMP - INTERVAL '20 MINUTE')";
    private final String FIND_USER_BY_TOKEN_SQL = "SELECT u.* FROM users u INNER JOIN access_token at ON u.user_id = at.user_fk WHERE at.token_name = ?";
    private final String DELETE_TOKEN_SQL = "DELETE FROM access_token WHERE user_fk = ?";


    //IMPLEMENTATIONS
    @Override
    public Optional<String> generateToken(User user) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement statement = connection.prepareStatement(SAVE_TOKEN_SQL)) {
                statement.setString(1, user.getId());
                statement.setString(2, user.getUsername() + "-mtcgToken");

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String returnedToken = resultSet.getString("token_name");
                        connection.commit(); // Commit the transaction
                        return Optional.of(returnedToken);
                    }
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error while generating token: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while generating token: " + e.getMessage());
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty();
    }


    @Override
    public Optional<User> findByToken(String token) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_BY_TOKEN_SQL)) {

            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming convertResultSetToUser is implemented correctly to map the ResultSet to a User object
                    User user = convertResultSetToUser(resultSet);
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                System.out.println("Error executing findByToken: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing findByToken: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if user not found or if exception occurs
    }

    @Override
    public Optional<String> findTokenByUserId(String userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TOKEN_BY_USER_SQL)) {

            statement.setString(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a User object
                    String token = resultSet.getString("token_name");
                    return Optional.ofNullable(token);
                }
            } catch (SQLException e) {
                System.out.println("Error executing findByUsername: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing findByUsername: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if user not found or if exception occurs
    }

    @Override
    public boolean deleteToken(String userId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_TOKEN_SQL)) {
                deleteStmt.setString(1, userId);
                deleteStmt.executeUpdate();
                connection.commit(); // Commit the transaction
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error deleting tokens: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting tokens: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    private boolean deleteExpiredTokens() {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_EXPIRED_TOKEN_SQL)) {
                deleteStmt.executeUpdate();
                connection.commit(); // Commit the transaction
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error deleting expired tokens: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting expired tokens: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    public boolean authenticateToken(String token) {
        // First, delete expired tokens
        if (!deleteExpiredTokens()) {
            return false; // Return false if failed to delete expired tokens
        }

        try (Connection connection = database.getConnection();
             PreparedStatement authStmt = connection.prepareStatement(AUTH_TOKEN_SQL)) {

            // Set the token in the authenticate statement
            authStmt.setString(1, token);

            // Execute authenticate statement
            try (ResultSet resultSet = authStmt.executeQuery()) {
                // Return true if the token is found
                return resultSet.next();
            } catch (SQLException e) {
                System.out.println("Error during token authentication: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during token authentication: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }

    private User convertResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getString("user_id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setCoins(resultSet.getInt("coins"));
        user.setEloRating(resultSet.getInt("eloRating"));
        user.setAdmin(resultSet.getBoolean("isAdmin"));
        return user;
    }

}
