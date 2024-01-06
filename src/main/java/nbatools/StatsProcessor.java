package nbatools;

import java.net.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StatsProcessor {
    
    public void processStats(String fileName) {

        try {
            FileReader in = new FileReader(fileName);
            BufferedReader br = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]}");
            sb.insert(0, "{\"actions\":");
            br.close();
            String content = sb.toString();

            JSONObject jo = new JSONObject(content);
            JSONArray actions = jo.getJSONArray("actions");

            for (Object temp : actions) {
                JSONObject action = (JSONObject) temp;
                System.out.println(action.get("actionType"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
