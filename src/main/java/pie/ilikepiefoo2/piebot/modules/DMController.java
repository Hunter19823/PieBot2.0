package pie.ilikepiefoo2.piebot.modules;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.channel.user.PrivateChannelCreateListener;
import org.javacord.api.listener.message.MessageCreateListener;
import pie.ilikepiefoo2.piebot.api.PieBotModule;

@PieBotModule
public class DMController implements PrivateChannelCreateListener, MessageCreateListener {

    public void onAPIBuilder( DiscordApiBuilder builder )
    {
        builder.addPrivateChannelCreateListener(this::onPrivateChannelCreate);
    }

    public void onAPILoad( DiscordApi api )
    {

    }

    /**
     * This method is called every time a private channel is created.
     *
     * @param event The event.
     */
    @Override
    public void onPrivateChannelCreate( PrivateChannelCreateEvent event )
    {
        event.getChannel().addMessageCreateListener(this::onMessageCreate);
    }

    /**
     * This method is called every time a message is created.
     *
     * @param event The event.
     */
    @Override
    public void onMessageCreate( MessageCreateEvent event )
    {
        if(event.getMessageAttachments().isEmpty()) {
            System.out.printf("Direct Message from %s: \"%s\"%n", event.getMessageAuthor().getDiscriminatedName(), event.getMessage().getContent());
        }else{
            System.out.printf("Direct Message containing %d attachments from %s: \"%s\"%n", event.getMessageAttachments().size(), event.getMessageAuthor().getDiscriminatedName(), event.getMessage().getContent());
            event.getMessageAttachments().forEach(messageAttachment -> System.out.printf("Attachment URL: %s",messageAttachment.getUrl().toString()));
            System.out.println();
        }
    }
}
