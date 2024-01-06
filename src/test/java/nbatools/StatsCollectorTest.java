package nbatools;

import org.junit.jupiter.api.Test;

public class StatsCollectorTest{

    @Test
    public void testStatsCollector() throws InterruptedException {
        String gameId = "0022300477";
        Runnable test = new StatsCollector(gameId);
        Thread testThread = new Thread(test);
        //testThread.start();
        //testThread.join();
    }
}
