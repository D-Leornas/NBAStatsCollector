package nbatools;

import java.net.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StatsProcessor implements Runnable{
    
    private String gameId;
    private String actions;

    StatsProcessor(String gameId, String actions) {
        this.gameId = gameId;
        this.actions = actions;
    }

    public void run() {

        try {

            JSONObject jo = new JSONObject(actions);
            JSONArray actions = jo.getJSONArray("actions");

            for (Object temp : actions) {
                JSONObject action = (JSONObject) temp;
                Runnable r = new StatPutter(action, gameId);
                Thread t = new Thread(r);
                t.start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
