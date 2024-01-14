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
    private final Database database;

    public SessionRepository_db(Database database) {
        this.database = database;
    }

    //SQL STATEMENTS
    private final String FIND_TOKEN_BY_USER_SQL = "SELECT token_name FROM access_token WHERE user_fk = ?";
    private final String SAVE_TOKEN_SQL = "INSERT INTO access_token(user_fk, token_name) VALUES (?, ?) RETURNING token_name";
    private final String DELETE_EXPIRED_TOKEN_SQL = "DELETE FROM access_token WHERE token_timestamp < (CURRENT_TIMESTAMP - INTERVAL '20 MINUTE')";
    private final String AUTH_TOKEN_SQL = "SELECT * FROM access_token WHERE token_name = ? AND token_timestamp >= (CURRENT_TIMESTAMP - INTERVAL '20 MINUTE')";
    private final String FIND_USER_BY_TOKEN_SQL = "SELECT u.* FROM users u INNER JOIN access_token at ON u.user_id = at.user_fk WHERE at.token_name = ?";
    private final String DELETE_TOKEN_SQL = "DELETE FROM access_token WHERE user_fk = ?";


    //IMPLEMENTATIONS

    /**
     * Generates and saves a token for a user in the database.
     *
     * @param user The User object for whom the token is to be generated.
     * @return An Optional containing the generated token if successful, or an empty Optional if not.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public Optional<String> generateToken(User user) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to generate and save a token
            try (PreparedStatement statement = connection.prepareStatement(SAVE_TOKEN_SQL)) {
                statement.setString(1, user.getId());
                statement.setString(2, user.getUsername() + "-mtcgToken");

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the generated token from the result set
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


    /**
     * Finds a user by their authentication token.
     *
     * @param token The authentication token.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an error during the retrieval or a database connection issue.
     */
    @Override
    public Optional<User> findByToken(String token) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_BY_TOKEN_SQL)) {

            statement.setString(1, token); // Set the token in the SQL query

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Convert the ResultSet to a User object
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
        return Optional.empty();
    }


    /**
     * Retrieves the authentication token for a specific user by their user ID.
     *
     * @param userId The unique identifier of the user.
     * @return An Optional containing the token if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an error during the retrieval or a database connection issue.
     */
    @Override
    public Optional<String> findTokenByUserId(String userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_TOKEN_BY_USER_SQL)) {

            statement.setString(1, userId); // Set the user ID in the SQL query
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve the token from the result set
                    String token = resultSet.getString("token_name");
                    return Optional.ofNullable(token); // Return the token wrapped in an Optional
                }
            } catch (SQLException e) {
                System.out.println("Error executing findTokenByUserId: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing findTokenByUserId: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if the token is not found or if an exception occurs
    }

    /**
     * Deletes a specific user's authentication token from the database.
     *
     * @param userId The unique identifier of the user whose token is to be deleted.
     * @return True if the token is successfully deleted, false otherwise.
     * @throws HttpStatusException If there is an error during the deletion or a database connection issue.
     */
    @Override
    public boolean deleteToken(String userId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to delete the token
            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_TOKEN_SQL)) {
                deleteStmt.setString(1, userId); // Set the user ID

                deleteStmt.executeUpdate(); // Execute the update
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


    /**
     * Deletes expired authentication tokens from the database.
     *
     * @return True if the expired tokens are successfully deleted, false otherwise.
     * @throws HttpStatusException If there is an error during the deletion or a database connection issue.
     */
    private boolean deleteExpiredTokens() {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to delete expired tokens
            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_EXPIRED_TOKEN_SQL)) {
                deleteStmt.executeUpdate(); // Execute the update
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


    /**
     * Authenticates a user's token by checking its validity in the database.
     *
     * @param token The authentication token to be verified.
     * @return True if the token is valid and exists, false otherwise.
     * @throws HttpStatusException If there is an error during token authentication or a database connection issue.
     */
    public boolean authenticateToken(String token) {
        // Delete expired tokens first
        if (!deleteExpiredTokens()) {
            return false; // Return false if failed to delete expired tokens
        }

        try (Connection connection = database.getConnection();
             PreparedStatement authStmt = connection.prepareStatement(AUTH_TOKEN_SQL)) {

            authStmt.setString(1, token); // Set the token in the authentication statement

            // Execute the authentication statement
            try (ResultSet resultSet = authStmt.executeQuery()) {
                return resultSet.next(); // Return true if the token is found and valid
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
