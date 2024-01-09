package nbatools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.sql.PreparedStatement;

import org.json.JSONObject;

public class StatsPutterTester implements Runnable{

    private String actionType;
    private JSONObject message;
    private String gameId;
    
    StatsPutterTester(JSONObject message, String gameId) {
        this.actionType = message.get("description").toString();
        this.message = message;
        this.gameId = gameId;
    }

    public void run() {

        Map<String, String> env = System.getenv();

        String connectionString = env.get("DBSTRING");

        try {
            Connection conn = DriverManager.getConnection(connectionString);
            String pid = null;
            PreparedStatement statement;

            if (message.get("actionType").toString().contains("Shot")) {
                pid = message.get("personId").toString();
                if (actionType.contains("3PT")) {
                    if (message.get("actionType").toString().equals("Missed Shot")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, [3pa] = [3pa] + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float), [3pp] = CAST([3pm] AS float)/CAST([3pa] AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, [3pa] = [3pa] + 1, [3pm] = [3pm] + 1, pts = pts + 3 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float), [3pp] = CAST([3pm] AS float)/CAST([3pa] AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                    }
                } else {
                    
                    if (message.get("actionType").toString().equals("Missed Shot")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, pts = pts + 2 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        
                    }
                }
            } else if (actionType.contains("REBOUND")) {
                pid = message.get("personId").toString();
                statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET reb = reb + 1 WHERE player_id = ? AND game_id = ?;");
                statement.setInt(1, Integer.parseInt(pid));
                statement.setInt(2, Integer.parseInt(gameId));
                statement.executeUpdate();
            } else if (actionType.contains("STEAL")) {
                pid = message.get("personId").toString();
                statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET stl = stl + 1 WHERE player_id = ? AND game_id = ?;");
                statement.setInt(1, Integer.parseInt(pid));
                statement.setInt(2, Integer.parseInt(gameId));
                statement.executeUpdate();
            } else if (actionType.contains("BLOCK")) {
                pid = message.get("personId").toString();
                statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET blk = blk + 1 WHERE player_id = ? AND game_id = ?;");
                statement.setInt(1, Integer.parseInt(pid));
                statement.setInt(2, Integer.parseInt(gameId));
                statement.executeUpdate();
            } else if (actionType.contains("FOUL")) {
                pid = message.get("personId").toString();
                statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET pf = pf + 1 WHERE player_id = ? AND game_id = ?;");
                statement.setInt(1, Integer.parseInt(pid));
                statement.setInt(2, Integer.parseInt(gameId));
                statement.executeUpdate();
            } else if (actionType.contains("Free Throw")) {
                pid = message.get("personId").toString();
                if (actionType.contains("MISS")) {
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fta = fta + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ftp = CAST(ftm AS float)/CAST(fta AS float) WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                } else {
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fta = fta + 1, ftm = ftm + 1, pts = pts + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ftp = CAST(ftm AS float)/CAST(fta AS float) WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                }
            } else if (actionType.contains("Turnover")) {
                pid = message.get("personId").toString();
                statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET tov = tov + 1 WHERE player_id = ? AND game_id = ?;");
                statement.setInt(1, Integer.parseInt(pid));
                statement.setInt(2, Integer.parseInt(gameId));
                statement.executeUpdate();
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
