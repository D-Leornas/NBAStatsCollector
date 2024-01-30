package nbatools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

public class test {
    
    static private DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private LocalDateTime lastDateTime = LocalDateTime.from(f.parse("0001-01-01T01:01:01Z"));

    public static void run() {

        LocalDateTime test = LocalDateTime.from(f.parse("2024-01-12T00:35:45Z"));
        System.out.println(test.compareTo(LocalDateTime.from(f.parse("2023-01-12T00:35:45Z"))));

    }

}
