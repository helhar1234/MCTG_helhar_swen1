package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository_db implements UserRepository{
    private final Database database = new Database();

    private final String CREATE_USER_SQL = "INSERT INTO users (user_id, username, password, isAdmin) VALUES (?,?,?,?)";
    private final String SEARCH_USERNAME_SQL = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
    private final String FIND_USER_SQL = "SELECT * FROM users WHERE username = ?";
    @Override
    public boolean saveUser(User user) {
        try (
            Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(CREATE_USER_SQL)) {

            statement.setString(1, user.getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPassword());
            statement.setBoolean(4, user.isAdmin());

            int affectedRows = statement.executeUpdate();

            return affectedRows == 1;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
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
