package nbatools;

import java.net.*;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;

public class StatsCollector implements Runnable{
    
    private String gameId;
    private int index = 0;
    private Map<String, String> uniques = new HashMap<String, String>();

    public StatsCollector(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public void run() {

        Runnable setUpRows = new SetPlayerGames(gameId);
        Thread s = new Thread(setUpRows);
        s.start();
        try {
            s.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileWriter out;

        try {

            // out file needs to be taken from the hash map
            out = new FileWriter("output-files/" + gameId + ".txt");

            ExecutorService es = Executors.newCachedThreadPool();
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
                    
                    for (int i = 0; i < actions.length(); i++) {
                        String key = actions.getJSONObject(i).get("possession").toString() + actions.getJSONObject(i).get("actionNumber").toString() + actions.getJSONObject(i).get("actionType");
                        if (!uniques.containsKey(key)) {
                            uniques.put(key, actions.get(i).toString());
                            message += actions.get(i).toString() + ",";
                            //System.out.println(actions.get(i).toString());
                        } else if (actions.getJSONObject(i).get("actionType").toString().contains("pt")) {
                            if (!actions.getJSONObject(i).get("shotResult").toString().equals(new JSONObject(uniques.get(key)).get("shotResult").toString()) || 
                                !actions.getJSONObject(i).get("actionType").toString().equals(new JSONObject(uniques.get(key)).get("actionType").toString())) {
                                System.out.println(actions.getJSONObject(i).get("shotResult").toString() + " | " + new JSONObject(uniques.get(key)).get("shotResult").toString());
                                JSONObject badStatMessage = new JSONObject(uniques.get(key));
                                Thread t = new Thread(new StatCorrector(badStatMessage, gameId));
                                t.start();
                                t.join();
                                uniques.remove(key);
                                uniques.put(key, actions.get(i).toString());
                            }
                        }
                    }

                    if (message != "{\"actions\":[") {
                        message = message.substring(0, message.length()-1);
                        message += "]}";
                        Runnable r = new StatsProcessor(gameId, message);
                        es.execute(r);


                        if ((actions.getJSONObject(actions.length()-1)).get("description").toString().equals("Game End")) {
                            FileWriter finalOut = new FileWriter("output-files/" + gameId + "_rest.txt");
                            finalOut.write(sb.toString());
                            finalOut.close();
                            break;
                        } else if (actions.getJSONObject(actions.length()-1).get("actionType").toString().equals("period")) {
                            if (actions.getJSONObject(actions.length()-1).get("subType").toString().equals("end")) {
                                if (Integer.parseInt(actions.getJSONObject(actions.length()-1).get("period").toString()) >= 4) {
                                    int homeScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreHome").toString());
                                    int awayScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreAway").toString());
                                    if (homeScore != awayScore) {
                                        FileWriter finalOut = new FileWriter("output-files/" + gameId + "_final.txt");
                                        finalOut.write(sb.toString());
                                        finalOut.close();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    System.out.println("============================================================================================================================================================================================================================");
                    TimeUnit.SECONDS.sleep(10);

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

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);

            StringBuilder outBuilder = new StringBuilder();
            outBuilder.append("{\"actions\": [");

            for (String k : uniques.keySet()) {
                outBuilder.append(uniques.get(k) + ",\n");
            }

            outBuilder.deleteCharAt(outBuilder.length() - 1);
            outBuilder.append("]}");

            out.write(outBuilder.toString());
            out.close();
            System.out.println("Game " + gameId + " has concluded");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void run(boolean test) {
        SetPlayerGames setUpRows = new SetPlayerGames(gameId);
        setUpRows.run(true);

        FileWriter out;

        try {

            out = new FileWriter("output-files/" + gameId + "_test.txt");
            out.write("[\n");
            
            ExecutorService es = Executors.newCachedThreadPool();
            while (true) {
                try {
                    URL url = URI.create("https://stats.nba.com/stats/playbyplayv3?EndPeriod=0&GameID=" + gameId + "&StartPeriod=0").toURL();
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
                    
                    JSONObject game = jo.getJSONObject("game");
                    JSONArray actions = game.getJSONArray("actions");
                    String message = "{\"actions\":[";

                    if (actions.length() > index) {
                        for (int i = index; i < actions.length(); i++) {
                            //Gives all the next actions
                            //System.out.println(actions.get(i));
                            out.write(actions.get(i).toString() + ",");
                            message += actions.get(i).toString() + ",";
                            //System.out.println(actions.get(i).toString());
                        }
                        index = actions.length();
                    }

                    message = message.substring(0, message.length()-1);
                    message += "]}";
                    Runnable r = new StatsProcessorTester(gameId, message);
                    es.execute(r);
                    

                    if ((actions.getJSONObject(actions.length()-1)).get("description").toString().equals("Game End")) {
                        break;
                    } else if (actions.getJSONObject(actions.length()-1).get("actionType").toString().equals("period")) {
                        if (actions.getJSONObject(actions.length()-1).get("subType").toString().equals("end")) {
                            if (Integer.parseInt(actions.getJSONObject(actions.length()-1).get("period").toString()) >= 4) {
                                int homeScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreHome").toString());
                                int awayScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreAway").toString());
                                if (homeScore != awayScore)
                                    break;
                            }
                        }
                    }

                    System.out.println("============================================================================================================================================================================================================================");
                    TimeUnit.SECONDS.sleep(30);

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("============================================================================================================================================================================================================================");
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            es.shutdown();
            System.out.println("done waiting: " + String.valueOf(es.awaitTermination(5, TimeUnit.MINUTES)));

            out.write("]");
            out.close();
            System.out.println("Game " + gameId + " has concluded");

        } catch (Exception e) {
            System.out.println(e.toString());
        }              
    }

    public void run(FileReader in) {
        SetPlayerGames setUpRows = new SetPlayerGames(gameId);
        setUpRows.run(true);

        FileWriter out;

        try {

            out = new FileWriter("output-files/" + gameId + "-test.txt");
            out.write("[\n");

            ExecutorService es = Executors.newCachedThreadPool();
            while (true) {
                try {
                    
                    BufferedReader br = new BufferedReader(in);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    JSONObject jo = new JSONObject(sb.toString());
                    
                    JSONArray actions = jo.getJSONArray("actions");
                    String message = "{\"actions\":[";
                    
                    if (actions.length() > index) {
                        for (int i = index; i < actions.length(); i++) {
                            if (uniques.put(actions.get(i).toString(), actions.get(i).toString()) != null) {    
                                //Gives all the next actions
                                out.write(actions.get(i).toString() + ",");
                                message += actions.get(i).toString() + ",";
                                //System.out.println(actions.get(i).toString());
                            } else {
                                System.out.println("Duplicate JSONObject");
                            }

                        }
                        index = actions.length();
                    }

                    message = message.substring(0, message.length()-1);
                    message += "]}";
                    Runnable r = new StatsProcessor(gameId, message);
                    es.execute(r);


                    if ((actions.getJSONObject(actions.length()-1)).get("description").toString().equals("Game End")) {
                        break;
                    } else if (actions.getJSONObject(actions.length()-1).get("actionType").toString().equals("period")) {
                        if (actions.getJSONObject(actions.length()-1).get("subType").toString().equals("end")) {
                            if (Integer.parseInt(actions.getJSONObject(actions.length()-1).get("period").toString()) >= 4) {
                                int homeScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreHome").toString());
                                int awayScore = Integer.parseInt(actions.getJSONObject(actions.length()-1).get("scoreAway").toString());
                                if (homeScore != awayScore)
                                    break;
                            }
                        }
                    }

                    System.out.println("============================================================================================================================================================================================================================");
                    TimeUnit.SECONDS.sleep(30);

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

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);

            out.write("]");
            out.close();
            System.out.println("Game " + gameId + " has concluded");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
