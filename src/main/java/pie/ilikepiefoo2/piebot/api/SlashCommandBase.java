package pie.ilikepiefoo2.piebot.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( ElementType.TYPE )
public @interface SlashCommandBase {
    public String name() default "test";
    public String description() default "Slash command description";
    public long[] server() default {};
}
