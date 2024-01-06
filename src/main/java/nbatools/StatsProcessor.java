package nbatools;

import java.net.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class StatsProcessor {
    
    public void processStats(String fileName) {

        try {
            FileReader in = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        

    }

}
