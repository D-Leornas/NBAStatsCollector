package nbatools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TestPastGames {
    
    @Test
    public void testStatProcessor() throws IOException {
        //GetGameIds test = new GetGameIds();
        //test.test("0022300507");
    }

    @Test
    public void testStatCollector() throws IOException {
        try {
            FileReader in = new FileReader("output-files/0022300541.txt");
            GetGameIds test = new GetGameIds();
            test.test("0022300541", in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
