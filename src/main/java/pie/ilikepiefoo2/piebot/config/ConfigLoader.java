package pie.ilikepiefoo2.piebot.config;

public class ConfigLoader {
    private static final String API_KEY = System.getenv().get("PIEBOT_KEY");

    public static String getApiKey(){
        return API_KEY;
    }



}
