package nbatools;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class TestPastGames {
    
    @Test
    public void testStatProcessor() throws IOException {
        GetGameIds test = new GetGameIds();
        test.test("0022300507");
    }

}
