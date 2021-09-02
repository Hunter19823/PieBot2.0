package pie.ilikepiefoo2.piebot.modules;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pie.ilikepiefoo2.piebot.PieBot;
import pie.ilikepiefoo2.piebot.api.PieBotModule;
import pie.ilikepiefoo2.piebot.api.SlashCommandBase;
import pie.ilikepiefoo2.piebot.api.SlashCommandParameter;
import pie.ilikepiefoo2.piebot.api.SubSlashCommand;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@SlashCommandBase(
        name = "firewithfire",
        description = "A series of piebot commands.",
        server = {629916668684009503l}
)
@PieBotModule
public class FireWithFire {

    public static Server fwfServer;
    public static TextChannel newApplicantsChannel;

    public static void onApiBuild( DiscordApiBuilder builder )
    {
        builder.setIntents(Intent.DIRECT_MESSAGES, Intent.GUILD_MEMBERS, Intent.GUILDS);
    }

    public static void onApiLoad( DiscordApi api )
    {
        fwfServer = api.getServerById(629916668684009503l).get();
        newApplicantsChannel = fwfServer.getTextChannelById(871875405232107562L).get();



//        SlashCommandHandler.createSlashCommand(FireWithFire.class);//.createForServer(FireWithFireServer).whenComplete(onComplete);
    }

    @SubSlashCommand(
            name = "notify",
            description = "Have PieBot 2.0 send a message to a role."
    )
    public static void messageRole(
            InteractionImmediateResponseBuilder immediateResponse,
            InteractionFollowupMessageBuilder followUpResponse,
            @SlashCommandParameter(
                    type = SlashCommandOptionType.ROLE,
                    name = "ROLE",
                    description = "The role you are sending a message to.",
                    required = true
            )
            Role role,
            @SlashCommandParameter(
                    type = SlashCommandOptionType.STRING,
                    name = "MESSAGE",
                    description = "The message being sent to the role.",
                    required = true
            )
            String message,
            User sender
    )
    {
        if(sender.getId() == PieBot.PIE_ID || sender.getId() == 161283503860875264l){
            immediateResponse.setContent("Now attempting to message role: "+role.getName()+" with message \""+message+"\".").setFlags(MessageFlag.EPHEMERAL).respond();
            AtomicInteger usersMessaged = new AtomicInteger();
            AtomicInteger failedMessages = new AtomicInteger();
            Collection<User> users = role.getUsers();
            int totalUsers = users.size();
            long followUpId = followUpResponse.setContent("Now attempting to message role: "+role.getName()+" with message \""+message+"\".").setFlags(MessageFlag.EPHEMERAL).send().join().getId();
            users.parallelStream().forEach( user -> {
                PrivateChannel privateChannel = createPrivateChannel(user);
                if(privateChannel != null) {
                    privateChannel.sendMessage(message + "\n\n(Sent by: \"" + sender.getName() + "\" ("+sender.getMentionTag()+"))").whenComplete(( msg, ex ) -> {
                        if(ex == null){
                            usersMessaged.getAndIncrement();
                        }else{
                            failedMessages.getAndIncrement();
                            ex.printStackTrace();
                        }
                        followUpResponse.setContent(
                                String.format("Total Messaged: %d out of %d\nSuccessful Messages: %d\nFailed Messages: %d",
                                        (usersMessaged.get()+failedMessages.get()),
                                        totalUsers,
                                        usersMessaged.get(),
                                        failedMessages.get()
                                )
                        ).setFlags(MessageFlag.EPHEMERAL).update(followUpId);
                    });
                }else{
                    failedMessages.getAndIncrement();
                    System.err.println("Was not able to create private channel with user.");
                }
            });
        } else {
            immediateResponse.setContent("You do not have access to this command!").setFlags(MessageFlag.EPHEMERAL).respond();
        }
    }

    private static PrivateChannel createPrivateChannel( User user )
    {
        System.out.println("Attempting to send message to "+user.getName()+"...");
        PrivateChannel privateChannel = null;
        try {
            privateChannel = user.getPrivateChannel().orElse(user.openPrivateChannel().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return privateChannel;
    }

    @SubSlashCommand(
            name = "dm",
            description = "Direct messages a user."
    )
    public static void messageUser( InteractionImmediateResponseBuilder immediateResponse,
                                    InteractionFollowupMessageBuilder followUpResponse,
                                    @SlashCommandParameter(
                                           type = SlashCommandOptionType.USER,
                                           name = "USER",
                                           description = "The role you are sending a message to.",
                                           required = true
                                   )
                                                User user,
                                    @SlashCommandParameter(
                                           type = SlashCommandOptionType.STRING,
                                           name = "MESSAGE",
                                           description = "The message being sent to the role.",
                                           required = true
                                   )
                                           String message,
                                    User sender
    ){
        if(sender.getId() == PieBot.PIE_ID  || sender.getId() == 161283503860875264l){
            immediateResponse.setContent("Now attempting to message user: "+user.getName()+" with message \""+message+"\".").setFlags(MessageFlag.EPHEMERAL).respond();
            PrivateChannel privateChannel = createPrivateChannel(user);
            if(privateChannel != null) {
                privateChannel.sendMessage(message + "\n\n(Sent by: \"" + sender.getName() + "\" ("+sender.getMentionTag()+"))").whenComplete(( msg, ex ) -> {
                    if(ex == null){
                        followUpResponse.setContent(
                                "Message sent successfully."
                        ).setFlags(MessageFlag.EPHEMERAL).send();
                    }else{
                        followUpResponse.setContent(
                                "Message Failed To Send."
                        ).setFlags(MessageFlag.EPHEMERAL).send();
                        ex.printStackTrace();
                    }
                });
            }else{
                followUpResponse.setContent(
                        "Message Failed To Send."
                ).setFlags(MessageFlag.EPHEMERAL).send();
                System.err.println("Was not able to create private channel with user.");
            }
        } else {
            immediateResponse.setContent("You do not have access to this command!").setFlags(MessageFlag.EPHEMERAL).respond();
        }
    }


    @SubSlashCommand(
            name = "signup",
            description = "Interested In Joining Fire With Fire? Use this command to sign up now!"
    )
    public static void applicationSignup(
            @SlashCommandParameter(
                    name = "IGN",
                    description = "What is your Albion In Game Name?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String ign,
            @SlashCommandParameter(
                    name = "FAME",
                    description = "What is your total fame in Albion?",
                    type = SlashCommandOptionType.INTEGER,
                    required = true
            )
        Integer fame,
            @SlashCommandParameter(
                    name = "TIMEZONE",
                    description = "What is your local TimeZone?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String timeZone,
            @SlashCommandParameter(
                    name = "PLAYTIME",
                    description = "How many hours do you play weekly?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String weeklyPlaytime,
            @SlashCommandParameter(
                    name = "FAVORITE_ROLE",
                    description = "What is your favorite and/or most played Role with what weapon(s)?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String favoriteWeaponOrRole,
        @SlashCommandParameter(
                name = "ZVZ_EXPERIENCED",
                description = "Do you have Experience in ZvZ?",
                type = SlashCommandOptionType.STRING,
                required = true
        )
        String zvz,
            @SlashCommandParameter(
                    name = "GVG_EXPERIENCED",
                    description = "Do you have Experience in GvG?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String gvg,
            @SlashCommandParameter(
                    name = "ADDITIONAL_COMMENTS",
                    description = "Do you have any additional questions, comments, or concerns that you would like to add?",
                    type = SlashCommandOptionType.STRING,
                    required = true
            )
        String additonalComments,
        User sender,
        InteractionImmediateResponseBuilder immediateResponseBuilder,
        InteractionFollowupMessageBuilder followupMessageBuilder
    )
    {
        immediateResponseBuilder.setFlags(MessageFlag.EPHEMERAL);
        followupMessageBuilder.setFlags(MessageFlag.EPHEMERAL);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(String.format("My IGN is: \"%s\"",ign));
        builder.setDescription(String.format("What are my favorite and/or most played Roles with what weapon(s)?\n%s",favoriteWeaponOrRole));
        builder.setAuthor(sender);
        builder.addField("My Total Fame is:",fame+"");
        builder.addInlineField("My Timezone is: ",timeZone);
        builder.addInlineField("My Weekly Playtime is: ", weeklyPlaytime);
        builder.addField("Do I have Experience in ZvZ?", zvz);
        builder.addField("Do I have Experience in GvG?", gvg);
        if(additonalComments != null)
            builder.addField("I would also like to add: ", additonalComments);
        immediateResponseBuilder.setContent("Please wait a moment while your application is being sent...").respond();
        newApplicantsChannel.sendMessage(builder).whenComplete(
                (( message, throwable ) -> {
                    if(throwable == null){
                        followupMessageBuilder
                                .setContent("You application has been forwarded to the proper place. Please allow guild staff some time to look over your application, and they'll be sure to message you when they are ready.")
                                .send();
                    }else{
                        throwable.printStackTrace();
                        followupMessageBuilder
                                .setContent("I'm sorry, an error has occured processing your request. Please content privately message @ILIKEPIEFOO2#4987 for assistance.")
                                .send();
                    }
                })
        );

    }
}
