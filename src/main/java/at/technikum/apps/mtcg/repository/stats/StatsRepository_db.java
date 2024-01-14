package at.technikum.apps.mtcg.repository.stats;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatsRepository_db implements StatsRepository {
    //DB CONNECTION
    private final Database database;

    public StatsRepository_db(Database database){
        this.database = database;
    }

    //SQL STATEMENTS
    private final String GET_WINS_SQL = "SELECT COUNT(*) AS wins FROM battles WHERE winner_fk = ?";
    private final String GET_BATTLES_SQL = "SELECT COUNT(*) AS battles FROM battles WHERE player_a_fk = ? OR player_b_fk = ?";

    //IMPLEMENTATIONS
    @Override
    public int getUserWins(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_WINS_SQL)) {

            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("wins");
                }
            } catch (SQLException e) {
                System.out.println("Error during getting wins: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during getting wins: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return 0;
    }

    @Override
    public int getUserBattles(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BATTLES_SQL)) {

            statement.setString(1, id);
            statement.setString(2, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("battles");
                }
            } catch (SQLException e) {
                System.out.println("Error during getting Battles: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during getting Battles: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return 0;
    }
}
