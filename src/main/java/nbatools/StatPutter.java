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

            switch (actionType) {

                case "2pt":
                    break;

                case "3pt":

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
