package pl.fitnesstracker.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static DatabaseConnector instance;
    private final Connection connection;

    // dane do połaczenia z bazą danych sę celowo zhardkodowane
    // pozwala to na połaczenie z nasza zahostowaną bazą danych
    // inaczej należało by samemu zahostować bazę
    // w prawdziwym projekcie mającym być wykorzystywanym przez faktycznych klientów to nie miało by miejsca
    private static final String URL = ""; //not on github lmao
    private static final String USER = "";
    private static final String PASSWORD = "";
    private DatabaseConnector() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Połączono z bazą danych!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie znaleziono sterownika PostgreSQL.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się połączyć z bazą danych.");
        }
    }

    public static DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        } else {
            try {
                if (instance.connection == null || instance.connection.isClosed()) {
                    instance = new DatabaseConnector();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static boolean isConnected() {
        try {
            DatabaseConnector db = getInstance();
            return db.connection != null && !db.connection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
