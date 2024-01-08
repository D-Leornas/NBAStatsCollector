package nbatools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.json.JSONObject;

public class StatPutter {

    private String actionType;
    private JSONObject message;
    
    StatPutter(JSONObject message) {
        actionType = message.get("actionType").toString();
        this.message = message;
    }

    public void putData() {

        Map<String, String> env = System.getenv();

        String connectionString = env.get("DBSTRING");

        try {
            Connection conn = DriverManager.getConnection(connectionString);
            String pid = null;

            switch (actionType) {

                case "2pt":
                    pid = message.get("personId").toString();
                    if (message.get("shotResult").toString().equals("Missed")) {
                        //fga + 1
                    } else {
                        //fga + 1, fgm + 1, pts + 2
                    }

                case "3pt":
                    pid = message.get("personId").toString();
                    if (message.get("shotResult").toString().equals("Missed")) {
                        //fga + 1, 3pa + 1
                    } else {
                        //fga + 1, 3pa + 1, fgm + 1, 3pm + 1, pts + 2
                        if (message.has("assistPersonId")) {
                            String apid = message.get("assistPersonId").toString();
                            //ast + 1
                        }
                    }

                case "rebound":
                    if (message.get("description").toString().contains("TEAM"))
                        break;
                    
                    pid = message.get("personId").toString();

                    if (message.get("subType").toString().equals("defensive")) {
                        //reb + 1, dreb + 1
                    } else {
                        //reb + 1, oreb + 1
                    }
                
                case "steal":
                    pid = message.get("personId").toString();
                    //stl + 1

                case "block":
                    pid = message.get("personId").toString();
                    //blk + 1

                case "foul":
                    pid = message.get("personId").toString();
                    //pf + 1

                case "freethrow":
                    pid = message.get("personId").toString();
                    if (message.get("shotResult").toString().equals("Missed")) {
                        //fta + 1
                    } else {
                        //fta + 1, ftm + 1
                    }
                case "turnover":
                    pid = message.get("personId").toString();
                    //to + 1
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
