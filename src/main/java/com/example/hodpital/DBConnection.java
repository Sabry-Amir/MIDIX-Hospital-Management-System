package com.example.hodpital;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.lang.Class.forName;

public class DBConnection {

    public Connection DBLink;

    public Connection getConnection() {
        String DatabaseName = "hospital_db";
        String DatabaseUser = "";
        String DatabasePassword = "";
        String DatabaseURL = "jdbc:sqlserver://localhost:1433;DatabaseName=hospital_db;encrypt=true;trustServerCertificate=true;integratedSecurity=true;";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DBLink = DriverManager.getConnection(DatabaseURL,DatabaseUser,DatabasePassword);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return DBLink;
    }
}
