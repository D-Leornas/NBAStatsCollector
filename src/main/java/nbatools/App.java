package nbatools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class App {
    public static void main(String[] args) {
        String connectionString = "jdbc:sqlserver://donnienba.database.windows.net:1433;database=nbastats;user=nbadmin;password=FireworkStand11!;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        try {
            Connection conn = DriverManager.getConnection(connectionString);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM nbastats.[2022-23].[players] WHERE player_name = 'LeBron James'");
            System.out.println(resultSet.toString());
        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }
}
