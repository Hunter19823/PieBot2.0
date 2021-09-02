package pie.ilikepiefoo2.piebot.api;

import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Retention( RetentionPolicy.RUNTIME)
@Target( { ElementType.PARAMETER, ElementType.METHOD } )
public @interface SlashCommandParameter {
    public SlashCommandOptionType type();

    public String name();

    public String description();

    public boolean required() default false;
}
