package nbatools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

public class SetPlayerGames implements Runnable{

    private String gameId;

    SetPlayerGames(String gameId) {
        this.gameId = gameId;
    }

    public void run() {
        try {
            URL rosterUrl = URI.create("https://cdn.nba.com/static/json/liveData/boxscore/boxscore_" + gameId + ".json").toURL();
            HttpURLConnection connection = (HttpURLConnection) rosterUrl.openConnection();
            connection.setRequestProperty("accept", "application/json");

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            Object obj = new JSONParser().parse(br);
            JSONObject jo = (JSONObject) obj;

            JSONArray roster = (JSONArray) ((JSONObject) ((JSONObject) jo.get("game")).get("homeTeam")).get("players");

            String connectionString = "jdbc:sqlserver://donnienba.database.windows.net:1433;database=nbastats;user=nbadmin;password=FireworkStand11!;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
            try {
                Connection conn = DriverManager.getConnection(connectionString);

                roster = (JSONArray) ((JSONObject) ((JSONObject) jo.get("game")).get("homeTeam")).get("players");

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[2022-23].[player_game_stats] (player_id, game_id) VALUES (?, ?);");
                    statement.setInt(1, Integer.parseInt(id));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                }

                roster = (JSONArray) ((JSONObject) ((JSONObject) jo.get("game")).get("awayTeam")).get("players");

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[2022-23].[player_game_stats] (player_id, game_id) VALUES (?, ?);");
                    statement.setInt(1, Integer.parseInt(id));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                }

                conn.close();

            } catch (Exception e) {
                System.out.print(e.toString());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
