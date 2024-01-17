package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.repository.battle.BattleRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.*;

public class BattleRepositoryTest {

    @Test
    void startBattleShouldHandleConcurrentAccess() throws SQLException, InterruptedException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        // Configure mock objects to return success when executeUpdate is called
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate()).thenReturn(1);

        // Create an instance of the BattleRepository_db class to be tested
        BattleRepository_db battleRepository = new BattleRepository_db(mockedDatabase);

        // Simulate concurrent access by creating two threads
        Runnable task1 = () -> battleRepository.startBattle("battle1", "host1", "opponent1");
        Runnable task2 = () -> battleRepository.startBattle("battle2", "host2", "opponent2");

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Verify that the startBattle method sets the necessary parameters and is called twice
        verify(mockedStatement, times(6)).setString(anyInt(), anyString()); // Parameters are set 6 times (3 per call)
        verify(mockedStatement, times(2)).executeUpdate(); // Method is called twice (once per call)
    }

}
