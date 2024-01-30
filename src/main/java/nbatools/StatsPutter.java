package nbatools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;

public class StatsPutter implements Runnable{

    private String actionType;
    private JSONObject message;
    private String gameId;
    
    StatsPutter(JSONObject message, String gameId) {
        this.actionType = message.get("actionType").toString();
        this.message = message;
        this.gameId = gameId;
    }

    public void run() {

        Map<String, String> env = System.getenv();

        String connectionString = env.get("DBSTRING");

        while (true) {
            try {
                Connection conn = DriverManager.getConnection(connectionString);
                String pid = null;
                PreparedStatement statement;

                if (actionType == "2pt") {
                    String apid = null;
                    pid = message.get("personId").toString();
                    if (message.get("shotResult").toString().equals("Missed")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " 2PT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, pts = pts + 2 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " 2PT make");
                        if (message.has("assistPersonId")) {
                            apid = message.get("assistPersonId").toString();
                            statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ast = ast + 1 WHERE player_id = ? AND game_id = ?;");
                            statement.setInt(1, Integer.parseInt(apid));
                            statement.setInt(2, Integer.parseInt(gameId));
                            statement.executeUpdate();
                            //System.out.println(apid + " assist");
                        }
                    }
                    statement = conn.prepareStatement("SELECT pts FROM nbastats.player_game_stats WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet points = statement.executeQuery();
                    ImportantStatChecker.CheckPoints(pid, gameId, points.getInt("pts"));
                    
                    if (apid != null) {
                        statement = conn.prepareStatement("SELECT ast FROM nbastats.player_game_stats WHERE player_id = ?;");
                        statement.setInt(1, Integer.parseInt(apid));
                        ResultSet assists = statement.executeQuery();
                        ImportantStatChecker.CheckAssists(apid, gameId, assists.getInt("ast"));
                    }
                } else if (actionType == "3pt") {
                    pid = message.get("personId").toString();
                    String apid = null;
                    if (message.get("shotResult").toString().equals("Missed")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, [3pa] = [3pa] + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float), [3pp] = CAST([3pm] AS float)/CAST([3pa] AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " 3PT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, [3pa] = [3pa] + 1, [3pm] = [3pm] + 1, pts = pts + 3 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float), [3pp] = CAST([3pm] AS float)/CAST([3pa] AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " 2PT make");

                        if (message.has("assistPersonId")) {
                            apid = message.get("assistPersonId").toString();
                            statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ast = ast + 1 WHERE player_id = ? AND game_id = ?;");
                            statement.setInt(1, Integer.parseInt(apid));
                            statement.setInt(2, Integer.parseInt(gameId));
                            statement.executeUpdate();
                            //System.out.println(apid + " assist");
                        }

                    }
                        
                    statement = conn.prepareStatement("SELECT pts FROM nbastats.player_game_stats WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet points = statement.executeQuery();
                    ImportantStatChecker.CheckPoints(pid, gameId, points.getInt("pts"));
                    
                    if (apid != null) {
                        statement = conn.prepareStatement("SELECT ast FROM nbastats.player_game_stats WHERE player_id = ?;");
                        statement.setInt(1, Integer.parseInt(apid));
                        ResultSet assists = statement.executeQuery();
                        ImportantStatChecker.CheckAssists(apid, gameId, assists.getInt("ast"));
                    }
                    
                } else if (actionType == "rebound") {
                    if (message.get("description").toString().contains("TEAM"))
                        break;
                    
                    pid = message.get("personId").toString();

                    if (message.get("subType").toString().equals("defensive")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET reb = reb + 1, dreb = dreb + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " dreb");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET reb = reb + 1, oreb = oreb + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " oreb");
                    }

                    statement = conn.prepareStatement("SELECT reb FROM nbastats.player_game_stats WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet rebounds = statement.executeQuery();
                    ImportantStatChecker.CheckRebounds(pid, gameId, rebounds.getInt("reb"));

                } else if (actionType == "steal") {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET stl = stl + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    //System.out.println(pid + " steal");
                    statement = conn.prepareStatement("SELECT stl FROM nbastats.player_game_stats WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet steals = statement.executeQuery();
                    ImportantStatChecker.CheckSteals(pid, gameId, steals.getInt("stl"));
                } else if (actionType == "block") {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET blk = blk + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    //System.out.println(pid + " block");
                    statement = conn.prepareStatement("SELECT blk FROM nbastats.player_game_stats WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet blocks = statement.executeQuery();
                    ImportantStatChecker.CheckBlocks(pid, gameId, blocks.getInt("blk"));
                } else if (actionType == "foul") {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET pf = pf + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    //System.out.println(pid + " foul");
                } else if (actionType == "freethrow") {
                    pid = message.get("personId").toString();
                    if (message.get("shotResult").toString().equals("Missed")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fta = fta + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ftp = CAST(ftm AS float)/CAST(fta AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " FT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fta = fta + 1, ftm = ftm + 1, pts = pts + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ftp = CAST(ftm AS float)/CAST(fta AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        //System.out.println(pid + " FT make");
                    }
                } else if (actionType == "turnover") {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET tov = tov + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    //System.out.println(pid + " turnover");
                }

                conn.close();
                break;

            } catch (Exception e) {
                e.printStackTrace();
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

}
