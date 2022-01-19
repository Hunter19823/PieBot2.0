package pie.ilikepiefoo2.piebot.modules.swe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import pie.ilikepiefoo2.piebot.PieBot;

import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ServerManager {
    private final DiscordApi API;
    private final long SERVER_ID;
    private final long CATEGORY_ID;
    private String channel_list = "Empty";
    private Map<String,Map<Integer, Long>> classChannelsMap;
    private Set<Long> channelIDs;
    private final ServerMemberJoinHandler joinHandler;
    private static final Permissions channel_access_permissions;
    private static final Permissions channel_access_denied_permissions;
    public static final Logger LOG = LogManager.getLogger(ServerManager.class);

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
        this.classChannelsMap = new HashMap<>();
        this.joinHandler = new ServerMemberJoinHandler();
        this.channelIDs = new HashSet<>();
        updateChannelList();
        registerInviteListener();
    }

    public void updateChannelList()
    {
        this.classChannelsMap.clear();
        getChannelCategory().getChannels().stream().parallel().forEach(
                (channel) -> {
                    mapClassTextChannel(channel);
                    if(channel.asServerTextChannel().isPresent()){
                        if(!channelContainsInviteLink(channel.asServerTextChannel().get())){
                            createInviteLink(channel.asServerTextChannel().get());
                        }
                    }
                }
        );
        List<String> channelList = new ArrayList<>();
        classChannelsMap.forEach(
                ( s, integerLongMap ) -> integerLongMap.forEach(
                        ( integer, aLong ) -> channelList.add(String.format("%s-%d%n",s,integer))
                )
        );
        Collections.sort(channelList);
        StringBuilder builder = new StringBuilder();
        channelList.parallelStream().forEachOrdered(
                builder::append
        );
        channelList.clear();
        this.channel_list = builder.toString();
    }

    private void registerInviteListener(){
        getServer().getInvites().whenComplete((invites, exc) ->{
            if(exc != null){
                System.err.println(exc);
            }else {
                joinHandler.reset(invites);
                getServer().addServerMemberJoinListener(joinHandler);
            }
        });
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
            LOG.error("Internal Error: Invalid channel, cannot add user to invalid channel.");
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
            LOG.error("Internal Error: Invalid channel, cannot remove user from invalid channel.");
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
                                LOG.error("Error creating channel \"${}\".%n", channel.getName());
                                LOG.error(exc);
                                exc.printStackTrace();
                            } else {
                                followupMessageBuilder.setContent(String.format("%s The Channel \"%s\" has been created successfully.%n", user.getMentionTag(),channel.getName())).send();
                                LOG.info("Channel \"${}\" successfully created.%n", channel.getName());
                                channel.createUpdater().addPermissionOverwrite(user,channel_access_permissions).update();
                            }
                        });
                        break;
                    case "deny":
                        LOG.info("Request to create channel was denied.");
                        followupMessageBuilder.setContent(String.format("%s I'm sorry, but your request to make the channel \"%s-%d\" has been denied.%n",user.getMentionTag(),subject,number)).send();
                        break;
                }
                event.getMessageComponentInteraction()
                        .asButtonInteraction()
                        .get()
                        .getMessage()
                        .get()
                        .delete();
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
            channelIDs.add(serverTextChannel.getId());
            return subjectNumberMap.put(subjectNumber, serverTextChannel.getId());
        }else{
            return 0l;
        }
    }

    private void createInviteLink(ServerTextChannel channel){
        channel
                .createInviteBuilder()
                .setAuditLogReason("Generating Channel-Linked Invite...")
                .setNeverExpire()
                .create()
                .whenComplete((invite, exc) -> {
                    if(exc != null){
                        LOG.error("Error creating invite for channel \"%s\".%n",channel.getName());
                        LOG.error(exc);
                    }else{
                        channel
                            .createUpdater()
                            .setTopic(
                                    "Invite Your Classmates using this link: "+invite.getUrl().toString()
                            )
                            .setAuditLogReason("Updating Channel Topic with permanent Discord Link.")
                            .update()
                            .whenComplete((update, exc2) -> {
                                if(exc2 != null){
                                    LOG.error("Error updating channel \"%s\" with permanent Discord Link.%n",channel.getName());
                                    LOG.error(exc2);
                                }
                            });
                    }
                });
    }

    private boolean channelContainsInviteLink(ServerTextChannel channel){
        return channel.getTopic().contains("Invite Your Classmates using this link:");
    }

    public String getChannel_list()
    {
        return this.channel_list;
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

    private class ServerMemberJoinHandler implements ServerMemberJoinListener {
        private Map<Long,Integer> previousInviteCount = new HashMap<>();

        public void reset(Collection<RichInvite> invites) {
            previousInviteCount.clear();
            for (RichInvite inv : invites) {
                if(channelIDs.contains(inv.getChannel().get().getId())) {
                    previousInviteCount.put(inv.getChannelId(), inv.getUses());
                }
            }
        }

        public void findInviteChange(ServerMemberJoinEvent event) {
            event.getServer().getInvites().whenComplete((invites, exc) -> {
                if (exc != null) {
                    System.err.printf("Error getting invites for server \"%s\".%n", event.getServer().getName());
                    System.err.println(exc);
                }else{
                    invites.forEach(inv -> {
                        if(inv.getChannel().isPresent()) {
                            if(channelIDs.contains(inv.getChannel().get().getId())){
                                if(previousInviteCount.get(inv.getChannelId()) != inv.getUses()){
                                    inv.getChannel().get().createUpdater()
                                            .addPermissionOverwrite(event.getUser(), channel_access_permissions)
                                            .update()
                                            .whenComplete((update, exc2) -> {
                                                if(exc2 != null){
                                                    System.err.printf("Error updating channel \"%s\" with new permissions.%n",inv.getChannel().get().getName());
                                                }else {
                                                    System.out.printf("Updated channel \"%s\" with new permissions.%n",inv.getChannel().get().getName());
                                                }
                                            });
                                }
                                previousInviteCount.put(inv.getChannelId(), inv.getUses());
                            }
                        }
                    });
                }
            });
        }

        /**
         * This method is called every time a user joins a server.
         *
         * @param event The event.
         */
        @Override
        public void onServerMemberJoin( ServerMemberJoinEvent event ) {
            System.out.printf("User \"%s\" joined server \"%s\".%n", event.getUser().getName(), event.getServer().getName());
            findInviteChange(event);

        }
    }
}
