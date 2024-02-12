package nbatools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

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
            ExecutorService es = Executors.newCachedThreadPool();

            for (Object temp : actions) {
                JSONObject action = (JSONObject) temp;
                Runnable r = new StatsPutter(action, gameId);
                es.execute(r);
                TimeUnit.MILLISECONDS.sleep(75);
            }

            es.shutdown();
            es.awaitTermination(5, TimeUnit.MINUTES);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
