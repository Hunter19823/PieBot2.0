package pie.ilikepiefoo2.piebot.modules;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import pie.ilikepiefoo2.piebot.PieBot;
import pie.ilikepiefoo2.piebot.api.PieBotModule;
import pie.ilikepiefoo2.piebot.api.SlashCommandBase;
import pie.ilikepiefoo2.piebot.api.SlashCommandHandler;
import pie.ilikepiefoo2.piebot.api.SlashCommandParameter;
import pie.ilikepiefoo2.piebot.api.SubSlashCommand;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@SlashCommandBase(
        name = "piebot",
        description = "A series of piebot commands."
)
@PieBotModule
public class MySlashCommands {

    public static void attachMyListener( DiscordApiBuilder builder )
    {
        builder.setIntents(Intent.DIRECT_MESSAGES, Intent.GUILD_MEMBERS, Intent.GUILDS);
    }

    public static void onApiLoad( DiscordApi api )
    {
        Server ThePieShop = api.getServerById(837886869105672214l).get();

//        SlashCommandHandler.createSlashCommand(MySlashCommands.class);//.createForServer(ThePieShop).whenComplete(onComplete);
    }

//    @SubSlashCommand(
//            name = "message",
//            description = "Have PieBot 2.0 send a message to a role."
//    )
//    public static void messageRole(
//            InteractionImmediateResponseBuilder immediateResponse,
//            InteractionFollowupMessageBuilder followUpResponse,
//            @SlashCommandParameter(
//                    type = SlashCommandOptionType.ROLE,
//                    name = "ROLE",
//                    description = "The role you are sending a message to.",
//                    required = true
//            )
//            Role role,
//            @SlashCommandParameter(
//                    type = SlashCommandOptionType.STRING,
//                    name = "MESSAGE",
//                    description = "The message being sent to the role.",
//                    required = true
//            )
//            String message,
//            User sender
//    )
//    {
//        if(sender.getId() == PieBot.PIE_ID){
//            immediateResponse.setContent("Now attempting to message role: "+role.getName()+" with message \""+message+"\".").respond();
//            AtomicInteger usersMessaged = new AtomicInteger();
//            AtomicInteger failedMessages = new AtomicInteger();
//            Collection<User> users = role.getUsers();
//            int totalUsers = users.size();
//            long followUpId = followUpResponse.setContent("Now attempting to message role: "+role.getName()+" with message \""+message+"\".").send().join().getId();
//            users.parallelStream().forEach( user -> {
//                PrivateChannel privateChannel = createPrivateChannel(user);
//                if(privateChannel != null) {
//                    privateChannel.sendMessage(message + "\n\n(Sent by: \"" + sender.getName() + "\" ("+sender.getMentionTag()+"))").whenComplete(( msg, ex ) -> {
//                        if(ex == null){
//                            usersMessaged.getAndIncrement();
//                        }else{
//                            failedMessages.getAndIncrement();
//                            ex.printStackTrace();
//                        }
//                        followUpResponse.setContent(
//                                String.format("Total Messaged: %d out of %d\nSuccessful Messages: %d\nFailed Messages: %d",
//                                        (usersMessaged.get()+failedMessages.get()),
//                                        totalUsers,
//                                        usersMessaged.get(),
//                                        failedMessages.get()
//                                )
//                        ).update(followUpId);
//                    });
//                }else{
//                    failedMessages.getAndIncrement();
//                    System.err.println("Was not able to create private channel with user.");
//                }
//            });
//        } else {
//            immediateResponse.setContent("You do not have access to this command!").respond();
//        }
//    }
//
//    private static PrivateChannel createPrivateChannel( User user )
//    {
//        System.out.println("Attempting to send message to "+user.getName()+"...");
//        PrivateChannel privateChannel = null;
//        try {
//            privateChannel = user.getPrivateChannel().orElse(user.openPrivateChannel().get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        return privateChannel;
//    }
//
//    @SubSlashCommand(
//            name = "dm",
//            description = "Direct messages a user."
//    )
//    public static void messageUser(InteractionImmediateResponseBuilder immediateResponse,
//                                   InteractionFollowupMessageBuilder followUpResponse,
//                                   @SlashCommandParameter(
//                                           type = SlashCommandOptionType.USER,
//                                           name = "USER",
//                                           description = "The role you are sending a message to.",
//                                           required = true
//                                   )
//                                               User user,
//                                   @SlashCommandParameter(
//                                           type = SlashCommandOptionType.STRING,
//                                           name = "MESSAGE",
//                                           description = "The message being sent to the role.",
//                                           required = true
//                                   )
//                                               String message,
//                                   User sender
//    ){
//        if(sender.getId() == PieBot.PIE_ID){
//            immediateResponse.setContent("Now attempting to message user: "+user.getName()+" with message \""+message+"\".").respond();
//            PrivateChannel privateChannel = createPrivateChannel(user);
//            if(privateChannel != null) {
//                privateChannel.sendMessage(message + "\n\n(Sent by: \"" + sender.getName() + "\" ("+sender.getMentionTag()+"))").whenComplete(( msg, ex ) -> {
//                    if(ex == null){
//                        followUpResponse.setContent(
//                                "Message sent successfully."
//                        ).send();
//                    }else{
//                        followUpResponse.setContent(
//                                "Message Failed To Send."
//                        ).send();
//                        ex.printStackTrace();
//                    }
//                });
//            }else{
//                followUpResponse.setContent(
//                        "Message Failed To Send."
//                ).send();
//                System.err.println("Was not able to create private channel with user.");
//            }
//        } else {
//            immediateResponse.setContent("You do not have access to this command!").respond();
//        }
//    }
}
