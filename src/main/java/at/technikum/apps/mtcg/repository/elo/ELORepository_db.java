package at.technikum.apps.mtcg.repository.elo;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ELORepository_db implements ELORepository {
    // DB CONNECTION
    private final Database database;

    public ELORepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String UPDATE_ELO_SQL = "UPDATE users SET elorating = GREATEST(0, elorating + ?) WHERE user_id = ?";

    // IMPLEMENTATIONS

    /**
     * Updates the ELO rating of a user in the database.
     *
     * @param userId   The unique identifier of the user.
     * @param eloToAdd The amount to be added to (or subtracted from) the user's ELO rating.
     * @return True if the ELO rating is successfully updated, false otherwise.
     * @throws HttpStatusException If there is an error during the update or a database connection issue.
     */
    @Override
    public boolean updateELO(String userId, int eloToAdd) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to update the user's ELO rating
            try (PreparedStatement updateELOStmt = connection.prepareStatement(UPDATE_ELO_SQL)) {
                updateELOStmt.setInt(1, eloToAdd); // Set the ELO points to be added
                updateELOStmt.setString(2, userId); // Set the user ID

                // Execute the update and check the affected rows
                int affectedRows = updateELOStmt.executeUpdate();
                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction if the update didn't affect any rows
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error updating elo: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating elo: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e.getMessage());
        }
    }

}
