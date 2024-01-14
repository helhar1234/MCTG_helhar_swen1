package at.technikum.apps.mtcg.repository.scoreboard;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.dto.UserStats;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardRepository_db implements ScoreboardRepository {
    // DB CONNECTION
    private final Database database;

    public ScoreboardRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String GET_SCOREBOARD_SQL = "SELECT username, elorating FROM users WHERE isAdmin is false ORDER BY elorating DESC LIMIT 100";

    // IMPLEMENTATIONS

    /**
     * Retrieves the scoreboard from the database, which includes user stats like username and ELO rating.
     *
     * @return An array of UserStats objects representing the scoreboard.
     * @throws HttpStatusException If there is an error during retrieval or a database connection issue.
     */
    @Override
    public UserStats[] getScoreboard() {
        List<UserStats> scoreboard = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_SCOREBOARD_SQL)) {

            // Execute the query and process the result set
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    // Extract user details from the result set
                    String username = resultSet.getString("username");
                    int eloRating = resultSet.getInt("elorating");

                    // Create a new UserStats object and add it to the scoreboard list
                    scoreboard.add(new UserStats(username, eloRating));
                }
            } catch (SQLException e) {
                System.out.println("Error retrieving scoreboard: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving scoreboard: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }

        // Convert the list of UserStats to an array and return it
        return scoreboard.toArray(new UserStats[0]);
    }


}
