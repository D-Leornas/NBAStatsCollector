package nbatools;

import java.io.IOException;
import java.net.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.json.simple.JSONArray;

public class GetGameIds {
    
    public static void main(String[] args) throws IOException{

        URL url = URI.create("https://cdn.nba.com/static/json/liveData/scoreboard/todaysScoreboard_00.json").toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        try {
            Object obj = new JSONParser().parse(br);
            JSONObject jo = (JSONObject) obj;

            JSONArray games = ((JSONArray) ((JSONObject) jo.get("scoreboard")).get("games"));

            for (Object g : games) {
                JSONObject j = (JSONObject) g;
                String gid = j.get("gameId").toString();
                System.out.println(gid);
                //System.out.println("Starting thread");
                Runnable r = new StatsCollector(gid);
                new Thread(r).start();
            }

        } catch (Exception e) {
            System.err.println(e.toString());
        }

    }
    
}
