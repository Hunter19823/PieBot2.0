package pie.ilikepiefoo2.piebot.modules.swe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import pie.ilikepiefoo2.piebot.PieBot;

import java.nio.charset.IllegalCharsetNameException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerManager {
    private final DiscordApi API;
    private final long SERVER_ID;
    private final long CATEGORY_ID;
    private Map<String,Map<Integer, Long>> classChannelsMap;
    private static final Permissions channel_access_permissions;
    private static final Permissions channel_access_denied_permissions;
    static {
        channel_access_permissions =
            new PermissionsBuilder()
                .setAllowed(PermissionType.READ_MESSAGES)
                .build();
        channel_access_denied_permissions =
            new PermissionsBuilder()
                    .setDenied(PermissionType.READ_MESSAGES)
                    .build();
    }

    public ServerManager( DiscordApi api, long SERVER_ID, long CATEGORY_ID )
    {
        this.API = api;
        this.SERVER_ID = SERVER_ID;
        this.CATEGORY_ID = CATEGORY_ID;
        init();
    }

    private void init()
    {
        Server server = getServer();
        ChannelCategory class_channel_category = getChannelCategory();
        class_channel_category = server.getChannelCategoryById(753791567449292820l).get();
        this.classChannelsMap = new HashMap<>();
        class_channel_category.getChannels().stream().parallel().forEach(
                this::mapClassTextChannel
        );
        System.out.println(this.classChannelsMap.size());
    }

    public void updateChannelList()
    {
        this.classChannelsMap.clear();
        getChannelCategory().getChannels().stream().parallel().forEach(
                this::mapClassTextChannel
        );
    }

    public Optional<CompletableFuture<Void>> addUserToChannel( User user, String subject, Integer number)
    {
        if(isChannelValid(subject,number)){
            return Optional.of(
                    getSubjectChannel(subject, number).createUpdater()
                            .addPermissionOverwrite(user,channel_access_permissions)
                            .update()
            );
        }else{
            // TODO "invalid channel" response.
            return Optional.empty();
        }
    }

    public Optional<CompletableFuture<Void>> removeUserFromChannel( User user, String subject, Integer number)
    {
        if(isChannelValid(subject,number)){
            return Optional.of(
                    getSubjectChannel(subject, number).createUpdater()
                            .addPermissionOverwrite(user,channel_access_denied_permissions)
                            .update()
            );
        }else{
            // TODO "invalid channel" response.
            return Optional.empty();
        }
    }

    public ServerTextChannelBuilder createNewChannel( String subject, Integer number)
    {
        Server server = getServer();
        ServerTextChannelBuilder builder = new ServerTextChannelBuilder(server);
        builder.addPermissionOverwrite(server.getEveryoneRole(),channel_access_denied_permissions);
        builder.setName(String.format("%s-%d",subject,number));
        builder.setCategory(getChannelCategory());
        return builder;
    }

    public void askPieForPermission(User user, InteractionFollowupMessageBuilder followupMessageBuilder, String subject, Integer number)
    {
        User pie = API.getUserById(PieBot.PIE_ID).join();
        MessageBuilder builder = new MessageBuilder();
        builder.setContent(String.format("%s would like to create the channel \"%s-%d\".%n",user.getNicknameMentionTag(),subject,number));
        builder.addComponents(
                ActionRow.of(
                        Button.success("accept", "Create channel"),
                        Button.danger("deny","Deny channel creation.")
                )
        );
        long messageID = builder.send(pie).join().getId();

        pie.getPrivateChannel().get().addMessageComponentCreateListener( (event) -> {
            if(event.getMessageComponentInteraction().getMessageId() == messageID) {
                switch (event.getMessageComponentInteraction().getCustomId()) {
                    case "accept":
                        createNewChannel(subject, number).create().whenComplete(( channel, exc ) -> {
                            if (exc != null) {
                                followupMessageBuilder.setContent(String.format("%s There was an error creating channel \"%s\". Check logs for details.%n", user.getMentionTag(), channel.getName())).send();
                                System.err.printf("Error creating channel \"%s\".%n", channel.getName());
                                exc.printStackTrace();
                            } else {
                                followupMessageBuilder.setContent(String.format("%s The Channel \"%s\" has been created successfully.%n", user.getMentionTag(),channel.getName())).send();
                                System.out.printf("Channel \"%s\" successfully created.%n", channel.getName());
                                channel.createUpdater().addPermissionOverwrite(user,channel_access_permissions).update();
                            }
                        });
                        break;
                    case "deny":
                        System.out.println("Denying request to create channel.");
                        followupMessageBuilder.setContent(String.format("%s I'm sorry, but your request to make the channel \"%s-%d\" has been denied.%n",user.getMentionTag(),subject,number)).send();
                        break;
                }
                event.getMessageComponentInteraction().asButtonInteraction().get().getMessage().get().delete();
            }
        });
    }

    private Long mapClassTextChannel( ServerChannel channel)
    {
        ServerTextChannel serverTextChannel = channel.asServerTextChannel().get();
        if(!serverTextChannel.getName().equalsIgnoreCase("info")) {
            String[] temp = serverTextChannel.getName().split("-");
            Integer subjectNumber;
            String subjectName;
            Map<Integer, Long> subjectNumberMap;
            if (temp.length != 2)
                throw new IllegalCharsetNameException("The name \"" + serverTextChannel.getName() + "\" is an invalid channel name.");
            subjectName = temp[ 0 ];
            subjectNumber = Integer.parseInt(temp[ 1 ]);
            if (classChannelsMap.containsKey(subjectName)) {
                subjectNumberMap = classChannelsMap.get(subjectName);
            } else {
                subjectNumberMap = new HashMap<>();
                classChannelsMap.put(subjectName, subjectNumberMap);
            }
            return subjectNumberMap.put(subjectNumber, serverTextChannel.getId());
        }else{
            return 0l;
        }
    }

    private Server getServer()
    {
        return API.getServerById(SERVER_ID).get();
    }

    private ChannelCategory getChannelCategory()
    {
        return API.getChannelCategoryById(CATEGORY_ID).get();
    }

    private ServerTextChannel getSubjectChannel(String subject, Integer number)
    {
        return API.getServerTextChannelById(classChannelsMap.get(subject).get(number)).get();
    }

    public boolean isChannelValid(String subject, Integer number)
    {
        updateChannelList();
        return classChannelsMap.containsKey(subject) && classChannelsMap.get(subject).containsKey(number);
    }
}
