package pie.ilikepiefoo2.piebot.modules.swe;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pie.ilikepiefoo2.piebot.PieBot;
import pie.ilikepiefoo2.piebot.api.PieBotModule;
import pie.ilikepiefoo2.piebot.api.SlashCommandBase;
import pie.ilikepiefoo2.piebot.api.SlashCommandHandler;
import pie.ilikepiefoo2.piebot.api.SlashCommandParameter;
import pie.ilikepiefoo2.piebot.api.SubSlashCommand;

@PieBotModule
@SlashCommandBase(
        name = "class",
        description = "Want to join a class?",
        server = {746435144876949546L}
)
public class SoftwareEngineeringModule {
    private static final long SERVER_ID = 746435144876949546l;
    private static final long CATEGORY_ID = 753791567449292820l;
    private static ServerManager MANAGER = null;

    public static void onApiBuild( DiscordApiBuilder builder )
    {
        builder.setIntents(Intent.DIRECT_MESSAGES, Intent.GUILD_MEMBERS, Intent.GUILDS);
    }

    public static void onApiLoad( DiscordApi api )
    {
        MANAGER = new ServerManager(api, SERVER_ID, CATEGORY_ID);


//        SlashCommandHandler.createSlashCommand(FireWithFire.class);//.createForServer(FireWithFireServer).whenComplete(onComplete);
    }

    @SubSlashCommand(
            name = "create",
            description = "This command creates a channel for a specific class."
    )
    public static void createCategoryCommand(
            @SlashCommandParameter(
                    name="subject",
                    type = SlashCommandOptionType.STRING,
                    description = "The subject for the class. It's also the abbreviation for the class. Ex: \"SER\"-232",
                    required = true
            )
            String subject,
            @SlashCommandParameter(
                    name="number",
                    type = SlashCommandOptionType.INTEGER,
                    description = "The number for the class. It's the number after the abbreviation for the class. Ex: SER-\"232\"",
                    required = true
            )
            Integer number,
            User sender,
            InteractionFollowupMessageBuilder followupMessageBuilder,
            InteractionImmediateResponseBuilder immediateResponseBuilder
    )
    {
        if(sender.getId() == PieBot.PIE_ID){
            immediateResponseBuilder.setContent(String.format("Now creating the \"%s-%d\" channel...%n",subject,number)).respond();
            MANAGER.createNewChannel(subject,number).whenComplete( (channel, exc) -> {
                if(exc != null){
                    followupMessageBuilder.setContent(String.format("There was an error creating channel \"%s\". Check logs for details.%n",channel.getName())).send();
                    System.err.printf("Error creating channel \"%s\".%n", channel.getName());
                    exc.printStackTrace();
                }else{
                    followupMessageBuilder.setContent(String.format("Channel \"%s\" created successfully.%n", channel.getName())).send();
                    System.out.printf("Channel \"%s\" successfully created.%n", channel.getName());
                }
            });
        }else{
            immediateResponseBuilder.setContent("You do not have access to this command!").respond();
        }
    }
    @SubSlashCommand(
            name = "join",
            description = "This command allows you to join your peers in a class specific channel."
    )
    public static void joinClassCommand(
            @SlashCommandParameter(
                    name="subject",
                    type = SlashCommandOptionType.STRING,
                    description = "The subject for the class. It's also the abbreviation for the class. Ex: \"SER\"-232",
                    required = true
            )
                    String subject,
            @SlashCommandParameter(
                    name="number",
                    type = SlashCommandOptionType.INTEGER,
                    description = "The number for the class. It's the number after the abbreviation for the class. Ex: SER-\"232\"",
                    required = true
            )
                    Integer number,
            User sender,
            InteractionFollowupMessageBuilder followupMessageBuilder,
            InteractionImmediateResponseBuilder immediateResponseBuilder
    ){
        if(sender.getId() == PieBot.PIE_ID){
            immediateResponseBuilder.setContent(String.format("Now creating the \"%s-%d\" channel...%n",subject,number)).respond();
            if(MANAGER.addUserToChannel(sender,subject,number).isEmpty()){
                immediateResponseBuilder.setContent("I'm sorry, the command you entered is invalid. Make sure you are typing a valid channel.").respond();
            }else{
                immediateResponseBuilder.setContent("I will now try to add you to the specified channel. Please wait...").respond();
                MANAGER.addUserToChannel(sender,subject,number).get().whenComplete(
                        (nothing,exc) -> {
                            if(exc != null){
                                followupMessageBuilder.setContent("I'm sorry, but an error has occurred. Tell @ILIKEPIEFOO2#4987 to check logs and fix it.").setFlags(MessageFlag.EPHEMERAL).send();
                                System.err.println("An error occurred while trying to add a user to a channel.");
                                exc.printStackTrace();
                            }else{
                                followupMessageBuilder.setContent(String.format("You now have access to the %s-%d channel.",subject,number)).setFlags(MessageFlag.EPHEMERAL).send();
                            }
                        }
                );
            }
        }else{
            immediateResponseBuilder.setContent("You do not have access to this command!").respond();
        }
    }
}
