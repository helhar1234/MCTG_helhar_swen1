package at.technikum.apps.mtcg.repository.packages;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackageRepository_db implements PackageRepository {
    // DB CONNECTION
    private final Database database;

    public PackageRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String SAVE_PACKAGE_SQL = "INSERT INTO packages (package_id) VALUES (?) RETURNING package_id";
    private final String CONNECT_CARDS_PACKAGES_SQL = "INSERT INTO cards_packages (card_fk, package_fk) VALUES (?,?)";
    private final String FIND_PACKAGE_BY_ID_SQL = "SELECT * FROM packages WHERE package_id = ?";
    private final String FIND_AVAILABLE_PACKAGE_BY_ID_SQL = "SELECT * FROM packages WHERE package_id = ? AND sold is false";
    private final String GET_FIRST_PACKAGE_NOT_POSSESSING_SQL = "SELECT p.package_id FROM packages p WHERE NOT EXISTS (SELECT 1 FROM cards_packages cp JOIN user_cards uc ON cp.card_fk = uc.card_fk WHERE cp.package_fk = p.package_id AND uc.user_fk = ?) AND p.sold = false ORDER BY p.orderid ASC LIMIT 1";
    private final String FIND_CARDS_IN_PACKAGE_SQL = "SELECT c.* FROM cards c JOIN cards_packages cp ON c.card_id = cp.card_fk WHERE package_fk = ?";
    private final String UPDATE_PACKAGE_SOLD_SQL = "UPDATE packages SET sold = true WHERE package_id = ?";

    // IMPLEMENTATIONS

    /**
     * Saves a package to the database.
     *
     * @param id The unique identifier of the package to be saved.
     * @return True if the package is successfully saved, false otherwise.
     * @throws HttpStatusException If there is an error during the package saving process or a database connection issue.
     */
    @Override
    public boolean savePackage(String id) {
        boolean success = false;

        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to save the package
            try (PreparedStatement savePackageStatement = connection.prepareStatement(SAVE_PACKAGE_SQL)) {
                savePackageStatement.setString(1, id); // Set the package ID

                // Execute the query and process the result set
                try (ResultSet resultSet = savePackageStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the package ID from the result set
                        String returnedId = resultSet.getString("package_id");
                        // Check if the package ID is valid (non-null and not empty)
                        success = returnedId != null && !returnedId.isEmpty();
                    }
                }

                // Check the success flag to decide whether to commit or rollback
                if (success) {
                    connection.commit(); // Commit the transaction
                } else {
                    connection.rollback(); // Rollback the transaction if unsuccessful
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during package save: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during package save: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return success; // Return the success status
    }


    /**
     * Adds a card to a specific package in the database.
     *
     * @param packageId The unique identifier of the package.
     * @param cardId    The unique identifier of the card to be added.
     * @return True if the card is successfully added to the package, false otherwise.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public boolean addCardToPackage(String packageId, String cardId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to add the card to the package
            try (PreparedStatement connectStmt = connection.prepareStatement(CONNECT_CARDS_PACKAGES_SQL)) {
                connectStmt.setString(1, cardId);
                connectStmt.setString(2, packageId);

                // Execute the update and check the affected rows
                int affectedRows = connectStmt.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction if the update didn't affect any rows
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error connecting card to package: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error connecting card to package: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Retrieves a package from the database by its ID.
     *
     * @param id The unique identifier of the package to be retrieved.
     * @return An Optional containing the Package if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an error during the search or a database connection issue.
     */
    @Override
    public Optional<Package> findPackageById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_PACKAGE_BY_ID_SQL)) {

            findCardStmt.setString(1, id); // Set the package ID in the SQL query

            // Execute the query and process the result set
            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Convert the ResultSet to a Package object
                    Package aPackage = convertResultSetToPackage(resultSet);
                    return Optional.of(aPackage);
                }
            } catch (SQLException e) {
                System.out.println("Error finding package by ID: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding package by ID: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if no package is found
    }


    /**
     * Retrieves all cards contained in a specific package.
     *
     * @param packageId The unique identifier of the package.
     * @return An array of Card objects contained in the specified package.
     * @throws HttpStatusException If there is an error during the retrieval or a database connection issue.
     */
    @Override
    public Card[] getPackageCardsById(String packageId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findCardsStmt = connection.prepareStatement(FIND_CARDS_IN_PACKAGE_SQL)) {

            findCardsStmt.setString(1, packageId); // Set the package ID in the SQL query

            try (ResultSet resultSet = findCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    // Convert each ResultSet entry to a Card object and add to the list
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            } catch (SQLException e) {
                System.out.println("Error finding cards in package: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding cards in package: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }

        return cards.toArray(new Card[0]); // Convert the list of cards to an array and return it
    }


    /**
     * Retrieves the ID of the first package not yet owned by a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return The package ID, or null if no such package is found.
     * @throws HttpStatusException If there is an error during the search or a database connection issue.
     */
    @Override
    public String getFirstPackageNotPossessing(String userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_FIRST_PACKAGE_NOT_POSSESSING_SQL)) {

            stmt.setString(1, userId); // Set the user ID in the SQL query

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve and return the package ID from the result set
                    return resultSet.getString("package_id");
                }
            } catch (SQLException e) {
                System.out.println("Error finding first package not possessing: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding first package not possessing: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return null; // Return null if no package is found
    }


    /**
     * Retrieves an available package from the database by its ID.
     *
     * @param packageId The unique identifier of the package to be retrieved.
     * @return An Optional containing the Package if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an error during the search or a database connection issue.
     */
    @Override
    public Optional<Package> getAvailablePackages(String packageId) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_AVAILABLE_PACKAGE_BY_ID_SQL)) {

            findCardStmt.setString(1, packageId); // Set the package ID in the SQL query

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    Package aPackage = convertResultSetToPackage(resultSet);
                    return Optional.of(aPackage); // Return the package wrapped in an Optional
                }
            } catch (SQLException e) {
                System.out.println("Error finding package by ID: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding package by ID: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty(); // Return an empty Optional if no package is found
    }

    /**
     * Deletes a package from the database, or marks it as sold.
     *
     * @param packageId The unique identifier of the package to be deleted or marked as sold.
     * @return True if the package is successfully deleted or updated, false otherwise.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public boolean deletePackage(String packageId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction for database integrity
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to delete the package or update its status
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE_PACKAGE_SOLD_SQL)) {
                stmt.setString(1, packageId); // Set the package ID

                // Execute the update and check the affected rows
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction if the update didn't affect any rows
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error updating package sold status: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating package sold status: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    private Package convertResultSetToPackage(ResultSet resultSet) throws SQLException {
        Package aPackage = new Package();
        aPackage.setId(resultSet.getString("package_id"));
        aPackage.setPrice(resultSet.getInt("price"));
        aPackage.setSold(resultSet.getBoolean("sold"));
        return aPackage;
    }

    private Card convertResultSetToCard(ResultSet resultSet) throws SQLException {
        // Implement this method to convert a ResultSet to a Card object
        // Extract values from the resultSet and populate a new Card object
        Card card = new Card();
        card.setId(resultSet.getString("card_id"));
        card.setName(resultSet.getString("name"));
        card.setDamage(resultSet.getInt("damage"));
        card.setElementType(resultSet.getString("elementtype"));
        card.setCardType(resultSet.getString("cardtype"));
        return card;
    }
}
