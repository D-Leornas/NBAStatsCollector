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
                String pid;
                PreparedStatement statement;

                //System.out.println(actionType);

                if (actionType.equals("2pt")) {
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
                        System.out.println(pid + " 2PT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, pts = pts + 2 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        System.out.println(pid + " 2PT make");
                        if (message.has("assistPersonId")) {
                            apid = message.get("assistPersonId").toString();
                            statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ast = ast + 1 WHERE player_id = ? AND game_id = ?;");
                            statement.setInt(1, Integer.parseInt(apid));
                            statement.setInt(2, Integer.parseInt(gameId));
                            statement.executeUpdate();
                            System.out.println(apid + " assist");
                        }
                    }
                    statement = conn.prepareStatement("SELECT p.pts FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet points = statement.executeQuery();
                    
                    if (points.next()) ImportantStatChecker.CheckPoints(pid, gameId, points.getInt("pts"));
                    
                    if (apid != null) {
                        statement = conn.prepareStatement("SELECT p.ast FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                        statement.setInt(1, Integer.parseInt(apid));
                        ResultSet assists = statement.executeQuery();
                        if (assists.next()) ImportantStatChecker.CheckAssists(apid, gameId, assists.getInt("ast"));
                    }
                } else if (actionType.equals("3pt")) {
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
                        System.out.println(pid + " 3PT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fga = fga + 1, fgm = fgm + 1, [3pa] = [3pa] + 1, [3pm] = [3pm] + 1, pts = pts + 3 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET fgp = CAST(fgm AS float)/CAST(fga AS float), [3pp] = CAST([3pm] AS float)/CAST([3pa] AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        System.out.println(pid + " 2PT make");

                        if (message.has("assistPersonId")) {
                            apid = message.get("assistPersonId").toString();
                            statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ast = ast + 1 WHERE player_id = ? AND game_id = ?;");
                            statement.setInt(1, Integer.parseInt(apid));
                            statement.setInt(2, Integer.parseInt(gameId));
                            statement.executeUpdate();
                            System.out.println(apid + " assist");
                        }

                    }
                        
                    statement = conn.prepareStatement("SELECT p.pts FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet points = statement.executeQuery();
                    if (points.next()) ImportantStatChecker.CheckPoints(pid, gameId, points.getInt("pts"));
                    
                    if (apid != null) {
                        statement = conn.prepareStatement("SELECT p.ast FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                        statement.setInt(1, Integer.parseInt(apid));
                        ResultSet assists = statement.executeQuery();
                        if (assists.next())ImportantStatChecker.CheckAssists(apid, gameId, assists.getInt("ast"));
                    }
                    
                } else if (actionType.equals("rebound")) {
                    if (message.get("description").toString().contains("TEAM"))
                        break;
                    
                    pid = message.get("personId").toString();

                    if (message.get("subType").toString().equals("defensive")) {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET reb = reb + 1, dreb = dreb + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        System.out.println(pid + " dreb");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET reb = reb + 1, oreb = oreb + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        System.out.println(pid + " oreb");
                    }

                    statement = conn.prepareStatement("SELECT p.reb FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet rebounds = statement.executeQuery();
                    if (rebounds.next()) ImportantStatChecker.CheckRebounds(pid, gameId, rebounds.getInt("reb"));

                } else if (actionType.equals("steal")) {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET stl = stl + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    System.out.println(pid + " steal");
                    statement = conn.prepareStatement("SELECT p.stl FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet steals = statement.executeQuery();
                    if (steals.next()) ImportantStatChecker.CheckSteals(pid, gameId, steals.getInt("stl"));

                } else if (actionType.equals("block")) {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET blk = blk + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    System.out.println(pid + " block");
                    statement = conn.prepareStatement("SELECT p.blk FROM nbastats.player_game_stats AS p WHERE player_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    ResultSet blocks = statement.executeQuery();
                    if (blocks.next()) ImportantStatChecker.CheckBlocks(pid, gameId, blocks.getInt("blk"));

                } else if (actionType.equals("foul")) {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET pf = pf + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    System.out.println(pid + " foul");

                } else if (actionType.equals("freethrow")) {
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
                        System.out.println(pid + " FT miss");
                    } else {
                        statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET fta = fta + 1, ftm = ftm + 1, pts = pts + 1 WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        statement = conn.prepareStatement("UPDATE nbastats.player_game_stats SET ftp = CAST(ftm AS float)/CAST(fta AS float) WHERE player_id = ? AND game_id = ?;");
                        statement.setInt(1, Integer.parseInt(pid));
                        statement.setInt(2, Integer.parseInt(gameId));
                        statement.executeUpdate();
                        System.out.println(pid + " FT make");
                    }
                    
                } else if (actionType.equals("turnover")) {
                    pid = message.get("personId").toString();
                    statement = conn.prepareStatement("UPDATE nbastats.[player_game_stats] SET tov = tov + 1 WHERE player_id = ? AND game_id = ?;");
                    statement.setInt(1, Integer.parseInt(pid));
                    statement.setInt(2, Integer.parseInt(gameId));
                    statement.executeUpdate();
                    System.out.println(pid + " turnover");
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
