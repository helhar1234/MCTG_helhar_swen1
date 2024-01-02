package at.technikum.apps.mtcg.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/my_mtcg_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";


    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}