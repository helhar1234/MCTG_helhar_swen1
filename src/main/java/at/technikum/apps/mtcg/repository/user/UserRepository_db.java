package at.technikum.apps.mtcg.repository.user;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;
import at.technikum.server.http.HttpStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository_db implements UserRepository {
    // DB CONNECTION
    private final Database database;

    public UserRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String CREATE_USER_SQL = "INSERT INTO users (user_id, username, password, isAdmin) VALUES (?,?,?,?)";
    private final String SEARCH_USERNAME_SQL = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
    private final String FIND_USER_SQL = "SELECT * FROM users WHERE username = ?";
    private final String FIND_USER_BY_ID_SQL = "SELECT * FROM users WHERE user_id = ?";
    private final String SAVE_USERDATA_SQL = "INSERT INTO userdata (user_fk, name) VALUES (?, ?)";
    private final String ADD_CARD_TO_USER_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";

    // IMPLEMENTATIONS

    /**
     * Saves a user's information in the database.
     *
     * @param user The User object containing user information to be saved.
     * @return An Optional containing the saved User object if the operation is successful, or an empty Optional if it fails.
     * @throws HttpStatusException If there is an error during the save operation or a database connection issue.
     */
    @Override
    public Optional<User> saveUser(User user) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start a transaction

            try (PreparedStatement createUserStatement = connection.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
                createUserStatement.setString(1, user.getId());
                createUserStatement.setString(2, user.getUsername());
                createUserStatement.setString(3, user.getPassword());
                createUserStatement.setBoolean(4, user.isAdmin());
                int userAffectedRows = createUserStatement.executeUpdate();

                if (userAffectedRows == 1) {
                    try (ResultSet generatedKeys = createUserStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            try (PreparedStatement saveUserDataStatement = connection.prepareStatement(SAVE_USERDATA_SQL)) {
                                saveUserDataStatement.setString(1, user.getId());
                                saveUserDataStatement.setString(2, user.getUsername());
                                if (saveUserDataStatement.executeUpdate() == 1) {
                                    connection.commit(); // Commit the transaction
                                    return Optional.of(user); // Return the user
                                }
                            } catch (SQLException e) {
                                connection.rollback(); // Rollback the transaction
                                System.out.println("Error during user data save: " + e.getMessage());
                                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during user data save: " + e.getMessage());
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("Database connection error: " + e.getMessage());
                        throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
                    }
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during user save: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during user save: " + e.getMessage());
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return empty if the operation fails
    }


    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     * @throws HttpStatusException If there is an error during the execution or a database connection issue.
     */
    @Override
    public boolean isUsernameExists(String username) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_USERNAME_SQL)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") > 0; // If count is greater than 0, username exists
                }
            } catch (SQLException e) {
                System.out.println("Error executing isUsernameExists: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing isUsernameExists: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if an error occurs or if username doesn't exist
    }


    /**
     * Retrieves a user by their username from the database.
     *
     * @param username The username of the user to retrieve.
     * @return An Optional containing the User object if found, or an empty Optional if not found or if an exception occurs.
     * @throws HttpStatusException If there is an error during the execution or a database connection issue.
     */
    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_SQL)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a User object
                    User user = convertResultSetToUser(resultSet);
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                System.out.println("Error executing findByUsername: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing findByUsername: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if user not found or if an error occurs
    }


    /**
     * Updates user data in the database.
     *
     * @param id       The ID of the user whose data needs to be updated.
     * @param userData The new user data to be updated.
     * @return The updated UserData object if the operation is successful, or null if it fails.
     * @throws HttpStatusException If there is an error during the update operation or a database connection issue.
     */
    @Override
    public UserData updateUserData(String id, UserData userData) {
        StringBuilder sql = new StringBuilder("UPDATE userdata SET ");
        List<Object> parameters = new ArrayList<>();
        if (userData.getName() != null) {
            sql.append("name=?, ");
            parameters.add(userData.getName());
        }
        if (userData.getBio() != null) {
            sql.append("bio=?, ");
            parameters.add(userData.getBio());
        }
        if (userData.getImage() != null) {
            sql.append("image=?, ");
            parameters.add(userData.getImage());
        }

        // Remove the last comma and space if any parameters were added
        if (parameters.isEmpty()) {
            // No fields to update
            return userData;
        } else {
            sql.setLength(sql.length() - 2); // Remove the last comma and space
        }

        sql.append(" WHERE user_fk=? RETURNING *");
        parameters.add(id);

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    statement.setObject(i + 1, parameters.get(i));
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        connection.commit(); // Commit the transaction
                        return convertResultSetToUserData(resultSet);
                    } else {
                        connection.rollback(); // Rollback the transaction
                        return null;
                    }
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error updating user data: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating user data: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }


    /**
     * Adds a card to a user's stack in the database.
     *
     * @param userId The ID of the user to whom the card will be added.
     * @param card   The card to be added to the user's stack.
     * @return true if the card was successfully added, false otherwise.
     * @throws HttpStatusException If there is an error during the execution or a database connection issue.
     */
    @Override
    public boolean addCardToStack(String userId, Card card) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement addCardStmt = connection.prepareStatement(ADD_CARD_TO_USER_SQL)) {
                addCardStmt.setString(1, userId);
                addCardStmt.setString(2, card.getId());

                int affectedRows = addCardStmt.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Error adding card to user's stack: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding card to user's stack: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }


    /**
     * Retrieves a user by their ID from the database.
     *
     * @param id The ID of the user to retrieve.
     * @return An Optional containing the User object if found, or an empty Optional if not found or if an exception occurs.
     * @throws HttpStatusException If there is an error during the execution or a database connection issue.
     */
    @Override
    public Optional<User> findUserById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_USER_BY_ID_SQL)) {

            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming convertResultSetToUser is implemented correctly to map the ResultSet to a User object
                    User user = convertResultSetToUser(resultSet);
                    return Optional.of(user);
                }
            } catch (SQLException e) {
                System.out.println("Error executing findUserById: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error executing findUserById: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
        return Optional.empty(); // Return an empty Optional if user not found or if an error occurs
    }


    private UserData convertResultSetToUserData(ResultSet resultSet) throws SQLException {
        UserData userData = new UserData();
        userData.setName(resultSet.getString("name"));
        userData.setBio(resultSet.getString("bio"));
        userData.setImage(resultSet.getString("image"));
        return userData;
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
