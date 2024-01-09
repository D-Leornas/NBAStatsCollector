package nbatools;

import java.net.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public class StatsProcessorTester implements Runnable{
    
    private String gameId;
    private String actions;

    StatsProcessorTester(String gameId, String actions) {
        this.gameId = gameId;
        this.actions = actions;
    }

    public void run() {

        try {

            JSONObject jo = new JSONObject(actions);
            JSONArray actions = jo.getJSONArray("actions");
            ExecutorService es = Executors.newCachedThreadPool();

            for (Object temp : actions) {
                JSONObject action = (JSONObject) temp;
                Runnable r = new StatsPutterTester(action, gameId);
                es.execute(r);
            }

            es.shutdown();
            System.out.println("done waiting: " + String.valueOf(es.awaitTermination(5, TimeUnit.MINUTES)));


        } catch (Exception e) {
            System.out.println("StatsProcessor");
            e.printStackTrace();
        }

    }

}
