package nbatools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;
import org.json.JSONArray;

public class SetPlayerGames implements Runnable{

    private String gameId;
    private String date;

    SetPlayerGames(String gameId) {
        this.gameId = gameId;
        this.date = DateTimeFormatter.ofPattern("yyyy-MM-DD").format(LocalDateTime.now()).toString();
    }

    public void run() {
        while (true) {
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
                String homeId = jo.getJSONObject("game").getJSONObject("homeTeam").get("teamId").toString();
                String awayId = jo.getJSONObject("game").getJSONObject("awayTeam").get("teamId").toString();

                Map<String, String> env = System.getenv();

                String connectionString = env.get("DBSTRING");
                Connection conn = DriverManager.getConnection(connectionString);
                PreparedStatement gameStatement = conn.prepareStatement("INSERT INTO nbastats.games (game_id, game_date, home_team_id, away_team_id) VALUES (?, ?, ?, ?);");
                gameStatement.setString(1, gameId);
                gameStatement.setString(2, date);
                gameStatement.setString(3, homeId);
                gameStatement.setString(4, awayId);
                gameStatement.executeUpdate();
                TimeUnit.SECONDS.sleep(1);
                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id, team_id) VALUES (?, ?, ?);");
                    statement.setString(1, id);
                    statement.setString(2, gameId);
                    statement.setString(3, homeId);
                    statement.executeUpdate();
                }
                roster = jo.getJSONObject("game").getJSONObject("awayTeam").getJSONArray("players");
                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id, team_id) VALUES (?, ?, ?);");
                    statement.setString(1, id);
                    statement.setString(2, gameId);
                    statement.setString(3, awayId);
                    statement.executeUpdate();
                }
                conn.close();
                System.out.println("Player game rows and game row for game " + gameId + " have been set");
                break;

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("============================================================================================================================================================================================================================");
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e1) {
                    System.out.println("Sleep interrupted");
                }
            }
        }
    }

    public void run(boolean test) {
        try {
            URL rosterUrl = URI.create("https://stats.nba.com/stats/boxscoretraditionalv3?EndPeriod=0&EndRange=0&GameID=" + gameId + "&RangeType=0&StartPeriod=0&StartRange=0").toURL();
            HttpURLConnection connection = (HttpURLConnection) rosterUrl.openConnection();
            connection.setRequestProperty("Host", "stats.nba.com");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            //connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("x-nba-stats-origin", "stats");
            connection.setRequestProperty("x-nba-stats-token", "true");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Referer", "https://stats.nba.com/");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Cache-Control", "no-cache");

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            JSONObject jo = new JSONObject(sb.toString());

            JSONArray roster = jo.getJSONObject("boxScoreTraditional").getJSONObject("homeTeam").getJSONArray("players");

            String homeId = jo.getJSONObject("boxScoreTraditional").get("homeTeamId").toString();
            String awayId = jo.getJSONObject("boxScoreTraditional").get("awayTeamId").toString();

            Map<String, String> env = System.getenv();

            String connectionString = env.get("DBSTRING");

            try {
                Connection conn = DriverManager.getConnection(connectionString);

                PreparedStatement statement = conn.prepareStatement("INSERT INTO nbastats.games (game_id, game_date, home_team_id, away_team_id) VALUES (?, ?, ?, ?);");
                statement.setString(1, gameId);
                statement.setString(2, date);
                statement.setString(3, homeId);
                statement.setString(4, awayId);
                statement.executeUpdate();

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id, team_id) VALUES (?, ?, ?);");
                    statement.setString(1, id);
                    statement.setString(2, gameId);
                    statement.setString(3, homeId);
                    statement.executeUpdate();
                }

                roster = jo.getJSONObject("boxScoreTraditional").getJSONObject("awayTeam").getJSONArray("players");

                for (Object r : roster) {
                    JSONObject t = (JSONObject) r;
                    String id = t.get("personId").toString();
                    //System.out.println(id);
                    statement = conn.prepareStatement("INSERT INTO nbastats.[player_game_stats] (player_id, game_id, team_id) VALUES (?, ?, ?);");
                    statement.setString(1, id);
                    statement.setString(2, gameId);
                    statement.setString(3, awayId);
                    statement.executeUpdate();
                }

                conn.close();
                System.out.println("Player game rows and game row for game " + gameId + " have been set");

            } catch (Exception e) {
                System.out.print(e.toString());
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
