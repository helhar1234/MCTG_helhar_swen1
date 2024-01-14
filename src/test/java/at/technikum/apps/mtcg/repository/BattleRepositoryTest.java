package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.repository.battle.BattleRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BattleRepositoryTest {

    @Test
    void findBattleByIdShouldReturnBattleWhenExists() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedStatement);
        when(mockedStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true); // Simulate that a battle is found

        BattleRepository_db battleRepository = new BattleRepository_db(mockedDatabase);
        String battleId = "battle123";

        Optional<BattleResult> battleResult = battleRepository.findBattleById(battleId);

        assertTrue(battleResult.isPresent());
    }

    @Test
    void startBattleShouldHandleConcurrentAccess() throws SQLException, InterruptedException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedStatement = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockedStatement);
        when(mockedStatement.executeUpdate()).thenReturn(1);

        BattleRepository_db battleRepository = new BattleRepository_db(mockedDatabase);

        // Simulate concurrent access
        Runnable task1 = () -> battleRepository.startBattle("battle1", "host1", "opponent1");
        Runnable task2 = () -> battleRepository.startBattle("battle2", "host2", "opponent2");

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Each call to startBattle sets three parameters on the PreparedStatement
        verify(mockedStatement, times(6)).setString(anyInt(), anyString());
        verify(mockedStatement, times(2)).executeUpdate(); // Verify method was called twice
    }



}
