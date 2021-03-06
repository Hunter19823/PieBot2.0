package pie.ilikepiefoo2.piebot.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface SubSlashCommand {

    public String name();

    public String description();

    public boolean required() default false;
}
