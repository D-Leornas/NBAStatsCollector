package nbatools;

public class ImportantStatChecker {
    
    //These will go off every time a player's score is over that amount and they score again
    //Need to fix

    public static void CheckPoints(String pid, String gameId, int points) {
        if (points >= 40) {
            System.out.println(pid + " has " + points + " points!");
            //Send notification
        }
    }

    public static void CheckAssists(String apid, String gameId, int assists) {
        if (assists >= 10) {
            System.out.println(apid + " has " + assists + " assists!");
            //Send notification
        }
    }

    public static void CheckRebounds(String pid,String gameId, int rebounds) {
        if (rebounds >= 15) {
            System.out.println(pid + " has " + rebounds + " assists!");
            //Send notification
        }
    }

    public static void CheckSteals(String pid, String gameId, int steals) {
        if (steals >= 5) {
            System.out.println(pid + " has " + steals + " assists!");
            //Send notification
        }
    }

    public static void CheckBlocks(String pid, String gameId, int blocks) {
        if (blocks >= 5) {
            System.out.println(pid + " has " + blocks + " assists!");
            //Send notification 
        }
    }

}
