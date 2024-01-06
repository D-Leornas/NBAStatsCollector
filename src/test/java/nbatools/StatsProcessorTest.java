package nbatools;

import org.junit.jupiter.api.Test;

public class StatsProcessorTest {
    
    @Test
    public void testParse() {
        StatsProcessor test = new StatsProcessor();
        test.processStats("output-files/0022300492.txt");
    }

}
