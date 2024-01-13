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
    private final Database database = new Database();
    // SQL STATEMENTS
    private final String GET_SCOREBOARD_SQL = "SELECT username, elorating FROM users WHERE isAdmin is false ORDER BY elorating DESC";

    // IMPLEMENTATIONS

    @Override
    public UserStats[] getScoreboard() {
        List<UserStats> scoreboard = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_SCOREBOARD_SQL)) {

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String username = resultSet.getString("username");
                    int eloRating = resultSet.getInt("elorating");
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

        return scoreboard.toArray(new UserStats[0]);
    }


}
