package pie.ilikepiefoo2.piebot.api;

import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;

@Target( ElementType.PARAMETER)
public @interface SlashCommandChoicesString {
    public String[] choiceName() default {};
    public String[] choiceValue() default {};
}
