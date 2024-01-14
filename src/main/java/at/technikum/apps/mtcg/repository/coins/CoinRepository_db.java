package at.technikum.apps.mtcg.repository.coins;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CoinRepository_db implements CoinRepository {
    // DB CONNECTION
    private final Database database;

    public CoinRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String UPDATE_COINS_SQL = "UPDATE users SET coins = coins + ? WHERE user_id = ? AND coins + ? >= 0";

    // IMPLEMENTATIONS

    /**
     * Updates the coin balance of a user in the database.
     *
     * @param userId The unique identifier of the user.
     * @param price  The amount to be added or deducted from the user's coin balance.
     * @return True if the coin balance is successfully updated, false otherwise.
     * @throws HttpStatusException If there is an error during the update or a database connection issue.
     */
    @Override
    public boolean updateCoins(String userId, int price) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to update the user's coin balance
            try (PreparedStatement updateCoinsStmt = connection.prepareStatement(UPDATE_COINS_SQL)) {
                updateCoinsStmt.setInt(1, price); // Set the price (or coin change)
                updateCoinsStmt.setString(2, userId); // Set the user ID
                updateCoinsStmt.setInt(3, price); // Repeat setting the price

                // Execute the update and check the affected rows
                int affectedRows = updateCoinsStmt.executeUpdate();
                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction if the update didn't affect any rows
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error updating coins: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating coins: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }

}
