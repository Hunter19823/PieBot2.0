package pie.ilikepiefoo2.piebot.modules;

import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.listener.user.UserChangeNameListener;
import pie.ilikepiefoo2.piebot.api.PieBotModule;

import java.util.Optional;

@PieBotModule
public class TestModule {
    public TestModule()
    {

    }

    public void attachListener(DiscordApiBuilder builder)
    {
        System.out.println("Builder Received on non-static test module.");
    }

    public static void attachListeners( DiscordApiBuilder builder )
    {
        System.out.println("Builder Received on static test module.");
        System.out.println(builder);
    }

    public static class UserChangeName implements UserChangeNameListener {

        /**
         * This method is called every time a user changed their name.
         *
         * @param event The event.
         */
        @Override
        public void onUserChangeName( UserChangeNameEvent event )
        {

        }
    }
}
