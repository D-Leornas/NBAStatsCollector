package nbatools;

public class App {
    public static void main(String[] args) {
        try {
            GetGameIds appRunner = new GetGameIds();
            appRunner.start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
