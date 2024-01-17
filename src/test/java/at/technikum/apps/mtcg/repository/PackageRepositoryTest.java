package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.repository.packages.PackageRepository_db;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PackageRepositoryTest {

    @Test
    void getPackageCardsByIdShouldReturnAllCardsInPackage() throws SQLException {
        // Create mock objects to simulate database interactions
        Database mockedDatabase = mock(Database.class);
        Connection mockedConnection = mock(Connection.class);
        PreparedStatement mockedFindCardsStmt = mock(PreparedStatement.class);
        ResultSet mockedResultSet = mock(ResultSet.class);

        // Configure mock objects to return expected results
        when(mockedDatabase.getConnection()).thenReturn(mockedConnection);
        when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedFindCardsStmt);
        when(mockedFindCardsStmt.executeQuery()).thenReturn(mockedResultSet);
        when(mockedResultSet.next()).thenReturn(true, true, false); // Simulate multiple cards in the package

        // Create an instance of the PackageRepository_db class to be tested
        PackageRepository_db packageRepository = new PackageRepository_db(mockedDatabase);
        String packageId = "package123";

        // Call the getPackageCardsById method and verify the result
        Card[] cards = packageRepository.getPackageCardsById(packageId);

        assertNotNull(cards);
        assertTrue(cards.length > 0);
        // Additional assertions on specific fields of cards can be added if necessary
    }


}
