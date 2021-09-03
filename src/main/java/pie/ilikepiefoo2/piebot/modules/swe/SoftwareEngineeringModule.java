package pie.ilikepiefoo2.piebot.modules.swe;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
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
    private static DiscordApi API = null;

    public static void onApiBuild( DiscordApiBuilder builder )
    {
        builder.setIntents(Intent.DIRECT_MESSAGES, Intent.GUILD_MEMBERS, Intent.GUILDS);
    }

    public static void onApiLoad( DiscordApi api )
    {
        API = api;
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
        subject = subject.toLowerCase().trim();
//        if(sender.getId() == PieBot.PIE_ID){
            if(!MANAGER.isChannelValid(subject,number)) {
                immediateResponseBuilder.setContent(String.format("Now requesting the creation of the \"%s-%d\" channel...%n", subject, number)).respond();
                MANAGER.askPieForPermission(sender, followupMessageBuilder, subject, number);
            }else {
                immediateResponseBuilder.setContent("I'm sorry, this channel already exists. However, I will instead add you to the channel to save you the hassle!").respond();
            }
//            immediateResponseBuilder.setContent(String.format("Now creating the \"%s-%d\" channel...%n",subject,number)).respond();
//            MANAGER.createNewChannel(subject,number).whenComplete( (channel, exc) -> {
//                if(exc != null){
//                    followupMessageBuilder.setContent(String.format("There was an error creating channel \"%s\". Check logs for details.%n",channel.getName())).send();
//                    System.err.printf("Error creating channel \"%s\".%n", channel.getName());
//                    exc.printStackTrace();
//                }else{
//                    followupMessageBuilder.setContent(String.format("Channel \"%s\" created successfully.%n", channel.getName())).send();
//                    System.out.printf("Channel \"%s\" successfully created.%n", channel.getName());
//                }
//            });
//        }else{
//            immediateResponseBuilder.setContent("You do not have access to this command!").respond();
//        }
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
        subject = subject.toLowerCase().trim();
//        if(sender.getId() == PieBot.PIE_ID){
            if(MANAGER.addUserToChannel(sender,subject,number).isEmpty()){
                immediateResponseBuilder.setContent(String.format("I'm sorry, the command you entered is either invalid, or the channel does not exist yet.\nDouble check that you spelled the class correctly. Does, \"%s-%d\", look correct?\nIf your command was correct then try creating the channel instead with `/class create`%n",subject,number)).respond();
            }else{
                immediateResponseBuilder.setContent("I will now try to add you to the specified channel. Please wait...").respond();
                String finalSubject = subject;
                MANAGER.addUserToChannel(sender,subject,number).get().whenComplete(
                        (nothing,exc) -> {
                            if(exc != null){
                                followupMessageBuilder.setContent(String.format("%s I'm sorry, but an error has occurred. Tell @ILIKEPIEFOO2#4987 to check logs and fix it.%n",sender.getMentionTag())).send();
                                System.err.println("An error occurred while trying to add a user to a channel.");
                                exc.printStackTrace();
                            }else{
                                followupMessageBuilder.setContent(String.format("%s You now have access to the %s-%d channel.%n", sender.getMentionTag(),finalSubject,number)).send();
                            }
                        }
                );
            }
//        }else{
//            immediateResponseBuilder.setContent("You do not have access to this command!").respond();
//        }
    }
    @SubSlashCommand(
            name = "leave",
            description = "This command allows you to leave a class specific channel."
    )
    public static void leaveClassCommand(
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
//        if(sender.getId() == PieBot.PIE_ID){
            subject = subject.toLowerCase().trim();
            if(MANAGER.addUserToChannel(sender,subject,number).isEmpty()){
                immediateResponseBuilder.setContent(String.format("I'm sorry, the command you entered is either invalid, or the channel does not exist yet.\nDouble check that you spelled the class correctly. Does, \"%s-%d\", look correct?%n",subject,number)).respond();
            }else{
                immediateResponseBuilder.setContent("I will now try to remove you from the specified channel. Please wait...").respond();
                String finalSubject = subject;
                MANAGER.removeUserFromChannel(sender,subject,number).get().whenComplete(
                        (nothing,exc) -> {
                            if(exc != null){
                                followupMessageBuilder.setContent(String.format("%s I'm sorry, but an error has occurred. Tell @ILIKEPIEFOO2#4987 to check logs and fix it.%n",sender.getMentionTag())).send();
                                System.err.println("An error occurred while trying to add a user to a channel.");
                                exc.printStackTrace();
                            }else{
                                followupMessageBuilder.setContent(String.format("You have now been removed from the %s-%d channel.", finalSubject,number)).send();
                            }
                        }
                );
            }
//        }else{
//            immediateResponseBuilder.setContent("You do not have access to this command!").respond();
//        }
    }
    @SubSlashCommand(
            name = "list",
            description = "List all the currently available class channels."
    )
    public static void listClassesCommand(
            InteractionImmediateResponseBuilder immediateResponseBuilder
    ){
        immediateResponseBuilder.setFlags(MessageFlag.EPHEMERAL);
        immediateResponseBuilder.setContent(MANAGER.getChannel_list());
        immediateResponseBuilder.respond();
    }
}
