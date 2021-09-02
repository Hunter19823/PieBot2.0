package pie.ilikepiefoo2.piebot.modules.swe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

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

    public CompletableFuture<ServerTextChannel> createNewChannel( String subject, Integer number)
    {
        Server server = getServer();
        ServerTextChannelBuilder builder = new ServerTextChannelBuilder(server);
        builder.addPermissionOverwrite(server.getEveryoneRole(),channel_access_denied_permissions);
        builder.setName(String.format("%s-%d",subject,number));
        builder.setCategory(getChannelCategory());
        return builder.create();
    }

    private Long mapClassTextChannel( ServerChannel channel)
    {
        ServerTextChannel serverTextChannel = channel.asServerTextChannel().get();
        String[] temp = serverTextChannel.getName().split("-");
        Integer subjectNumber;
        String subjectName;
        Map<Integer, Long> subjectNumberMap;
        if(temp.length != 2)
            throw new IllegalCharsetNameException("The name \""+serverTextChannel.getName()+"\" is an invalid channel name.");
        subjectName = temp[0];
        subjectNumber = Integer.parseInt(temp[1]);
        if(classChannelsMap.containsKey(subjectName)){
            subjectNumberMap = classChannelsMap.get(subjectName);
        }else{
            subjectNumberMap = new HashMap<>();
            classChannelsMap.put(subjectName,subjectNumberMap);
        }
        return subjectNumberMap.put(subjectNumber,serverTextChannel.getId());
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

    private boolean isChannelValid(String subject, Integer number)
    {
        return classChannelsMap.containsKey(subject) && classChannelsMap.get(subject).containsKey(number);
    }
}
