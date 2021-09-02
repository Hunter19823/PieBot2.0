package pie.ilikepiefoo2.piebot.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE)
public @interface SlashCommandGroup {
    public String name();
    public String description();
}
