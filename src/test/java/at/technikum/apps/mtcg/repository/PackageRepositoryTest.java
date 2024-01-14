package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.repository.packages.PackageRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageRepositoryTest {

    @Test
    void savePackageShouldReturnTrueWhenPackageIsSavedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedSavePackageStatement = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedSavePackageStatement);
        when(mockedSavePackageStatement.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true);
        when(mockedResultSet.getString("package_id")).thenReturn("packageId");

        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);

        boolean result = packageRepository.savePackage("packageId");

        assertTrue(result);
    }

    @Test
    void addCardToPackageShouldReturnTrueWhenCardIsAddedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedConnectStmt = mock(PreparedStatement.class);

        // Set up the mock behavior for the Database and Connection
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        // Mock PreparedStatement for the CONNECT_CARDS_PACKAGES_SQL
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedConnectStmt);
        // Mock the executeUpdate behavior to return 1, indicating a row was affected (inserted)
        when(mockedConnectStmt.executeUpdate()).thenReturn(1);

        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "packageId";
        String cardId = "cardId";

        // Call the method under test
        boolean result = packageRepository.addCardToPackage(packageId, cardId);

        // Verify that the PreparedStatement was set correctly and executed
        verify(mockedConnectStmt).setString(1, cardId);
        verify(mockedConnectStmt).setString(2, packageId);
        verify(mockedConnectStmt).executeUpdate();
    }


    @Test
    void getPackageCardsByIdShouldReturnAllCardsInPackage() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedFindCardsStmt = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedFindCardsStmt);
        when(mockedFindCardsStmt.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, false); // Simulate multiple cards in the package

        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "package123";

        Card[] cards = packageRepository.getPackageCardsById(packageId);

        assertNotNull(cards);
        assertTrue(cards.length > 0);
        // Assert specific fields of cards if necessary
    }

    @Test
    void deletePackageShouldReturnTrueWhenPackageIsDeletedSuccessfully() throws SQLException {
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedDeletePackageStmt = mock(PreparedStatement.class);

        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedDeletePackageStmt);
        when(mockedDeletePackageStmt.executeUpdate()).thenReturn(1); // Indicate successful deletion

        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "package123";

        boolean deletionResult = packageRepository.deletePackage(packageId);

        assertTrue(deletionResult);
        verify(mockedDeletePackageStmt).setString(1, packageId);
        verify(mockedDeletePackageStmt).executeUpdate();
    }

}
