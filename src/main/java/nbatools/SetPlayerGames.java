package nbatools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;

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
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            JSONObject jo = new JSONObject(sb.toString());

            JSONArray roster = jo.getJSONObject("game").getJSONObject("homeTeam").getJSONArray("players");

            Map<String, String> env = System.getenv();

            String connectionString = env.get("DBSTRING");

            try {
                Connection conn = DriverManager.getConnection(connectionString);

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id) VALUES (?, ?);");
                    statement.setInt(1, Integer.parseInt(id));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                }

                roster = jo.getJSONObject("game").getJSONObject("awayTeam").getJSONArray("players");

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id) VALUES (?, ?);");
                    statement.setInt(1, Integer.parseInt(id));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                }

                conn.close();
                System.out.println("Player game rows for game " + gameId + " have been set");

            } catch (Exception e) {
                System.out.print(e.toString());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
