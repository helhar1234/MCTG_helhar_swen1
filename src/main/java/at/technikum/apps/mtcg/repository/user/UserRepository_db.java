package at.technikum.apps.mtcg.repository.user;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.entity.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository_db implements UserRepository {
    // DB CONNECTION
    private final Database database = new Database();

    // SQL STATEMENTS
    private final String CREATE_USER_SQL = "INSERT INTO users (user_id, username, password, isAdmin) VALUES (?,?,?,?)";
    private final String SEARCH_USERNAME_SQL = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
    private final String FIND_USER_SQL = "SELECT * FROM users WHERE username = ?";
    private final String FIND_USER_BY_ID_SQL = "SELECT * FROM users WHERE user_id = ?";
    private final String SAVE_USERDATA_SQL = "INSERT INTO userdata (user_fk, name) VALUES (?, ?)";
    private final String UPDATE_COINS_SQL = "UPDATE users SET coins = coins + ? WHERE user_id = ? AND coins + ? >= 0";
    private final String UPDATE_ELO_SQL = "UPDATE users SET elorating = GREATEST(0, elorating + ?) WHERE user_id = ?";
    private final String ADD_CARD_TO_USER_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";

    // IMPLEMENTATIONS
    @Override
    public Optional<User> saveUser(User user) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

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
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during user save: " + e.getMessage());
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return Optional.empty(); // Return empty if failed
    }


    @Override
    public boolean isUsernameExists(String username) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_USERNAME_SQL)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

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
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return Optional.empty(); // Return an empty Optional if user not found or if exception occurs
    }


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
        if (!parameters.isEmpty()) {
            sql.setLength(sql.length() - 2);
        } else {
            // No fields to update
            return userData;
        }

        sql.append(" WHERE user_fk=? RETURNING *");
        parameters.add(id);

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a UserData object
                    return convertResultSetToUserData(resultSet);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating user data: " + e.getMessage());
            // Handle exception, possibly rethrow as a unchecked exception or a custom exception
        }
        return null; // Or handle this case as you see fit
    }


    @Override
    public boolean updateCoins(String userId, int price) {
        boolean success = false;

        // Prepare SQL statement that updates the user's coins by a certain price (which could be negative)
        // This SQL uses the existing value of coins in the user table

        try (Connection connection = database.getConnection();
             PreparedStatement updateCoinsStmt = connection.prepareStatement(UPDATE_COINS_SQL)) {

            updateCoinsStmt.setInt(1, price);
            updateCoinsStmt.setString(2, userId);
            updateCoinsStmt.setInt(3, price); // To ensure the final balance is not negative

            // Execute the update statement
            int affectedRows = updateCoinsStmt.executeUpdate();

            // Check if the update was successful
            success = affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Error updating coins: " + e.getMessage());
            // You might want to handle specific SQL exceptions based on your business logic
        }
        return success;
    }

    @Override
    public boolean addCardToStack(String userId, Card card) {
        try (Connection connection = database.getConnection();
             PreparedStatement addCardStmt = connection.prepareStatement(ADD_CARD_TO_USER_SQL)) {

            addCardStmt.setString(1, userId);
            addCardStmt.setString(2, card.getId());

            // Execute the insert statement
            int affectedRows = addCardStmt.executeUpdate();

            // Check if the insert was successful
            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Error adding card to user's stack: " + e.getMessage());
            return false;
        }
    }


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
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return Optional.empty(); // Return an empty Optional if user not found or if exception occurs
    }

    @Override
    public boolean updateELO(String userId, int eloToAdd) {
        boolean success = false;
        try (Connection connection = database.getConnection();
             PreparedStatement updateELOStmt = connection.prepareStatement(UPDATE_ELO_SQL)) {

            updateELOStmt.setInt(1, eloToAdd);
            updateELOStmt.setString(2, userId);

            // Execute the update statement
            int affectedRows = updateELOStmt.executeUpdate();

            // Check if the update was successful
            success = affectedRows == 1;
        } catch (SQLException e) {
            System.out.println("Error updating elo: " + e.getMessage());
            // You might want to handle specific SQL exceptions based on your business logic
        }
        return success;
    }


    private UserData convertResultSetToUserData(ResultSet resultSet) throws SQLException {
        // Implement this method to convert a ResultSet to a UserData object.
        // Extract values from the resultSet and populate a new UserData object.
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
