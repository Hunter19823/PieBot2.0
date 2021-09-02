package pie.ilikepiefoo2.piebot.api;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PieBotModule
public class SlashCommandHandler {
    public static Map<String, Map<String, Method>> registeredSlashCommands = new HashMap<>();

    public static void onApiBuilder( DiscordApiBuilder builder )
    {
        builder.addSlashCommandCreateListener( event -> {
            Map<String,Method> possibleCommands = registeredSlashCommands.get(event.getSlashCommandInteraction().getCommandName());
            if(possibleCommands != null){
                SlashCommandInteractionOption option = event.getSlashCommandInteraction().getFirstOption().get();
                Method method = possibleCommands.get(option.getName());
                if(method != null){
                    processSlashCommandInteraction(method, option, event.getApi(), event);
                }
            }
        });
    }
    public static void onApi( DiscordApi api )
    {
        
    }

    public static void processSlashCommandInteraction( Method method, SlashCommandInteractionOption option, DiscordApi api, SlashCommandCreateEvent event){
        Object[] methodArgs = new Object[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();
        SlashCommandParameter parameter;
        Map<Class<?>, Object> additionalParameterValues = new HashMap<>() {{
            put(DiscordApi.class, api);
            put(SlashCommandCreateEvent.class, event);
            put(SlashCommandInteractionOption.class, option);
            put(Interaction.class, event.getInteraction());
            put(User.class, event.getSlashCommandInteraction().getUser());
            put(Server.class, event.getSlashCommandInteraction().getServer().orElse(null));
            put(TextChannel.class, event.getSlashCommandInteraction().getChannel().orElse(null));
            put(InteractionImmediateResponseBuilder.class, event.getSlashCommandInteraction().createImmediateResponder());
            put(InteractionFollowupMessageBuilder.class, event.getSlashCommandInteraction().createFollowupMessageBuilder());
        }};
        int optionIndex = 0;
        for(int i=0; i<methodArgs.length; i++){
            parameter = parameters[i].getAnnotation(SlashCommandParameter.class);
            if(parameter != null){
                switch(parameter.type()){
                    case ROLE -> methodArgs[i] = option.getOptionRoleValueByIndex(optionIndex++).orElse(null);
                    case USER -> methodArgs[i] = option.getOptionUserValueByIndex(optionIndex++).orElse(null);
                    case STRING -> methodArgs[i] = option.getOptionStringValueByIndex(optionIndex++).orElse("");
                    case BOOLEAN -> methodArgs[i] = option.getOptionBooleanValueByIndex(optionIndex++).orElse(false);
                    case CHANNEL -> methodArgs[i] = option.getOptionChannelValueByIndex(optionIndex++).orElse(null);
                    case INTEGER -> methodArgs[i] = option.getOptionIntValueByIndex(optionIndex++).orElse(null);
                    case MENTIONABLE -> methodArgs[i] = option.getOptionMentionableValueByIndex(optionIndex++).orElse(null);
                    default -> throw new IllegalArgumentException("Invalid Parameter Type!");
                }
            }else{
                if(additionalParameterValues.containsKey(parameters[i].getType())){
                    methodArgs[i] = additionalParameterValues.get(parameters[i].getType());
                }else{
                    throw new IllegalArgumentException("Invalid Parameter Type!");
                }
            }
        }
        try {
            method.invoke(null, methodArgs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static SlashCommandBuilder createSlashCommand( Class<?> moduleClass )
    {
        SlashCommandBase slashCommandBase = moduleClass.getAnnotation(SlashCommandBase.class);
        List<SlashCommandOption> subCommands = new ArrayList<>();
        Map<String, Method> subCommandMap = new HashMap<>();

        Arrays.stream(moduleClass.getMethods()).forEachOrdered(
                method -> {
                    SubSlashCommand subCommand = method.getAnnotation(SubSlashCommand.class);
                    if(subCommand != null){
                        SlashCommandParameter parameter;
                        SlashCommandChoicesInt intChoices;
                        SlashCommandChoicesString stringChoices;
                        String[] choiceNames;
                        String[] choiceValues;

                        Parameter[] types = method.getParameters();
                        List<SlashCommandOption> options = new ArrayList<>();
                        List<SlashCommandOptionChoice> choices = new ArrayList<>();

                        for(int i=0; i<method.getParameterCount(); i++){
                            parameter = types[i].getAnnotation(SlashCommandParameter.class);
                            intChoices = types[i].getAnnotation(SlashCommandChoicesInt.class);
                            stringChoices = types[i].getAnnotation(SlashCommandChoicesString.class);
                            if( parameter != null ) {
                                if(intChoices != null){
                                    choiceNames = intChoices.choiceNames();
                                    for(int j=0; j<choiceNames.length; j++)
                                        choices.add(SlashCommandOptionChoice.create(choiceNames[j],j));
                                    options.add(SlashCommandOption.createWithChoices(parameter.type(), parameter.name(), parameter.description(), parameter.required(),choices));
                                }else if(stringChoices != null){
                                    choiceNames = stringChoices.choiceName();
                                    choiceValues = stringChoices.choiceValue();
                                    for(int j=0; j<choiceNames.length; j++)
                                        choices.add(SlashCommandOptionChoice.create(choiceNames[j],choiceValues[j]));
                                    options.add(SlashCommandOption.createWithChoices(parameter.type(), parameter.name(), parameter.description(), parameter.required(),choices));
                                }else {
                                    options.add(SlashCommandOption.create(parameter.type(), parameter.name(), parameter.description(), parameter.required()));
                                }
                            }
                        }
                        if(options.isEmpty()) {
                            subCommands.add(SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, subCommand.name(), subCommand.description(), subCommand.required()));
                        }else{
                            subCommands.add(SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, subCommand.name(), subCommand.description(), options));
                        }
                        subCommandMap.put(subCommand.name(),method);
                    }
                }
        );
        SlashCommandHandler.registeredSlashCommands.put(slashCommandBase.name(),subCommandMap);

        //SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, slashCommandBase.name(), slashCommandBase.description(),subCommands);
        return SlashCommand.with(slashCommandBase.name(),slashCommandBase.description(),subCommands);
    }
}
