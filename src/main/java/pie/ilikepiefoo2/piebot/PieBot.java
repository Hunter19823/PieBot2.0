package pie.ilikepiefoo2.piebot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;
import pie.ilikepiefoo2.piebot.api.SlashCommandHandler;
import pie.ilikepiefoo2.piebot.config.ConfigLoader;
import pie.ilikepiefoo2.piebot.config.ModuleLoader;
import pie.ilikepiefoo2.piebot.modules.DMController;
import pie.ilikepiefoo2.piebot.modules.FireWithFire;
import pie.ilikepiefoo2.piebot.modules.TestModule;
import pie.ilikepiefoo2.piebot.modules.swe.SoftwareEngineeringModule;

import java.util.Scanner;

public class PieBot {
    private static DiscordApiBuilder builder = new DiscordApiBuilder();
    public final DiscordApi api;
    public final Server thePieShop;
    public static final long PIE_ID = 186245219535159297l;

    public PieBot()
    {
        ModuleLoader.passArgumentsToModules(builder);
        builder.setToken(ConfigLoader.getApiKey());
        this.api = builder.login().join();
        ModuleLoader.passArgumentsToModules(api);
        ModuleLoader.updateAllSlashCommands(api);
        this.thePieShop = api.getServerById(837886869105672214l).get();
    }

    public static void main( String[] args )
    {
        ModuleLoader.registerModule(SlashCommandHandler.class);
        //ModuleLoader.registerModule(FireWithFire.class);
        ModuleLoader.registerModule(new TestModule());
        ModuleLoader.registerSlashCommand(FireWithFire.class);
        ModuleLoader.registerModule(new DMController());
        ModuleLoader.registerModule(SoftwareEngineeringModule.class);
        ModuleLoader.registerSlashCommand(SoftwareEngineeringModule.class);

        PieBot bot = new PieBot();

        Scanner input = new Scanner(System.in);
        System.out.println("Type exit to stop.");
        while(!input.next().equalsIgnoreCase("exit"))
        {
            System.out.println("Exit command not received. Please Type 'exit' to stop.");
        }
        System.out.println("Now disconnecting...");
        input.close();
        bot.api.disconnect();
        System.out.println("Bot has been disconnected.");
    }
}
