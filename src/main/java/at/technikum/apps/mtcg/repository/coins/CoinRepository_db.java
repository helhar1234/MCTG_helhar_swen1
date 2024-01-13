package at.technikum.apps.mtcg.repository.coins;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CoinRepository_db implements CoinRepository{
    // DB CONNECTION
    private final Database database = new Database();
    // SQL STATEMENTS
    private final String UPDATE_COINS_SQL = "UPDATE users SET coins = coins + ? WHERE user_id = ? AND coins + ? >= 0";
    // IMPLEMENTATIONS
    @Override
    public boolean updateCoins(String userId, int price) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement updateCoinsStmt = connection.prepareStatement(UPDATE_COINS_SQL)) {
                updateCoinsStmt.setInt(1, price);
                updateCoinsStmt.setString(2, userId);
                updateCoinsStmt.setInt(3, price);

                int affectedRows = updateCoinsStmt.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Error updating coins: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating coins: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }
}
