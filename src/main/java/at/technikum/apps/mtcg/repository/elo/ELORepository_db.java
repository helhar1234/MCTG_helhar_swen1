package at.technikum.apps.mtcg.repository.elo;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ELORepository_db implements ELORepository{
    // DB CONNECTION
    private final Database database = new Database();


    // SQL STATEMENTS
    private final String UPDATE_ELO_SQL = "UPDATE users SET elorating = GREATEST(0, elorating + ?) WHERE user_id = ?";

    // IMPLEMENTATIONS
    @Override
    public boolean updateELO(String userId, int eloToAdd) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement updateELOStmt = connection.prepareStatement(UPDATE_ELO_SQL)) {
                updateELOStmt.setInt(1, eloToAdd);
                updateELOStmt.setString(2, userId);

                int affectedRows = updateELOStmt.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Error updating elo: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating elo: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }
}
