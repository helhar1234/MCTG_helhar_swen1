package at.technikum.apps.mtcg.repository.wheel;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WheelOfFortuneRepository_db implements WheelOfFortuneRepository {
    // DB CONNECTION
    private final Database database;

    public WheelOfFortuneRepository_db(Database database){
        this.database = database;
    }

    // SQL STATEMENTS
    private final String GET_USER_SPINS_SQL = "SELECT COUNT(*) FROM wheelOfFortune WHERE user_fk = ? AND wheel_time = CURRENT_DATE";
    private final String SAVE_SPIN_SQL = "INSERT INTO wheelOfFortune (user_fk) VALUES (?)";

    // IMPLEMENTATIONS
    @Override
    public boolean hasUserSpun(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_USER_SPINS_SQL)) {

            stmt.setString(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) >= 1;
                }
            } catch (SQLException e) {
                System.out.println("Error checking if user has spun: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if user has spun: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false;

    }

    @Override
    public boolean saveSpin(String id) {
        boolean success = false;

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement saveSpinStatement = connection.prepareStatement(SAVE_SPIN_SQL)) {
                saveSpinStatement.setString(1, id);

                int affectedRows = saveSpinStatement.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    success = true;
                } else {
                    connection.rollback(); // Rollback the transaction
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during spin save: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during spin save: " + e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return success;
    }
}
