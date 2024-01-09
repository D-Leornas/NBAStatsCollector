package nbatools;

import java.net.*;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

public class StatsCollector implements Runnable{
    
    private String gameId;
    private int actionsLength = 0;

    public StatsCollector(String gameId) {
        this.gameId = gameId;
    }

    public void run() {

        Runnable setUpRows = new SetPlayerGames(gameId);
        Thread t = new Thread(setUpRows);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileWriter out;

        try {

            out = new FileWriter("output-files/" + gameId + ".txt");
            out.write("[\n");
            while (true) {
                try {
                    URL url = URI.create("https://cdn.nba.com/static/json/liveData/playbyplay/playbyplay_" + gameId + ".json").toURL();
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestProperty("accept", "application/json");
                    
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    JSONObject jo = new JSONObject(sb.toString());
                    
                    JSONObject game = jo.getJSONObject("game");
                    JSONArray actions = game.getJSONArray("actions");
                    String message = "{\"actions\":[";

                    if (actions.length() > actionsLength) {
                        for (int i = actionsLength; i < actions.length(); i++) {
                            //Gives all the next actions
                            //System.out.println(actions.get(i));
                            out.write(actions.get(i).toString() + ",");
                            message += actions.get(i).toString() + ",";
                            System.out.println(actions.get(i).toString());
                        }
                        actionsLength = actions.length();
                    }

                    message = message.substring(0, message.length()-1);
                    message += "]}";
                    Runnable r = new StatsProcessor(gameId, message);
                    new Thread(r).start();

                    if ((actions.getJSONObject(actions.length()-1)).get("description").toString().equals("Game End")) {
                        break;
                    }

                    System.out.println("============================================================================================================================================================================================================================");
                    TimeUnit.SECONDS.sleep(30);

                } catch (Exception e) {
                    System.out.println(e.toString());
                    System.out.println("============================================================================================================================================================================================================================");
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (InterruptedException e1) {
                        System.out.println("Sleep interrupted");
                    }
                }
            }

            out.write("]");
            out.close();
            System.out.println("Game " + gameId + " has concluded");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
