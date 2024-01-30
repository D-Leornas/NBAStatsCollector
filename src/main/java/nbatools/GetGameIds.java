package nbatools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import org.json.JSONObject;
import org.json.JSONArray;

public class GetGameIds {

    public void start() throws IOException{

        URL url = URI.create("https://cdn.nba.com/static/json/liveData/scoreboard/todaysScoreboard_00.json").toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();


        connection.setRequestProperty("accept", "application/json");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            JSONObject jo = new JSONObject(sb.toString());

            JSONArray games = jo.getJSONObject("scoreboard").getJSONArray("games");

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

        System.out.println("All statcollector threads set");
    }

    public void test(String testGameId) throws IOException{

        StatsCollector r = new StatsCollector(testGameId);
        r.run(true);

        System.out.println("test statcollector thread set");
    }

    public void test(String testGameId, FileReader in) throws IOException{

        StatsCollector r = new StatsCollector(testGameId);
        r.run(in);

        System.out.println("test statcollector thread set");
    }
    
}
