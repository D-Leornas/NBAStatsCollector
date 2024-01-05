package nbatools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.io.FileWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.concurrent.TimeUnit;

public class StatsCollector implements Runnable{
    
    private String gameId;

    public StatsCollector(String gameId) {
        this.gameId = gameId;
    }

    public void run() {

        Runnable home = new SetPlayerGames(gameId);
        new Thread(home).start();

        try {
            TimeUnit.SECONDS.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                URL url = URI.create("https://cdn.nba.com/static/json/liveData/playbyplay/playbyplay_" + gameId + ".json").toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty("accept", "application/json");

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                Object obj = new JSONParser().parse(br);
                JSONObject jo = (JSONObject) obj;
                FileWriter out = new FileWriter(gameId + ".txt");

                out.write(jo.toString());
                System.out.println("Successfully written");
                out.close();

            } catch (Exception e) {
                System.out.println(e.toString());
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e1) {
                    
                }
            }
        }
    }
}
