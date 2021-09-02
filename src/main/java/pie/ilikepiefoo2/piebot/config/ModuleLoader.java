package pie.ilikepiefoo2.piebot.config;


import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import pie.ilikepiefoo2.piebot.api.PieBotModule;
import pie.ilikepiefoo2.piebot.api.SlashCommandBase;
import pie.ilikepiefoo2.piebot.api.SlashCommandHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModuleLoader {
    private static List<Object> modules = new ArrayList<>();
    private static List<Class<?>> slashCommands = new ArrayList<>();

    public static <T extends Object> void registerModule( T module)
    {
        if(Class.class.isAssignableFrom(module.getClass())){
            if(((Class<?>) module).getAnnotation(PieBotModule.class) != null){
                modules.add(module);
                System.out.printf("Adding Static Module \"%s\" (\"%s\").%n", module, ((Class<?>) module).getName());
            }else{
                System.err.printf("Failed to add Static Module \"%s\" (\"%s\").%n", module,((Class<?>) module).getName());
            }
        }else{
            if(module.getClass().getAnnotation(PieBotModule.class) != null){
                modules.add(module);
                System.out.printf("Adding Non-Static Module \"%s\" (\"%s\").%n", module, module.getClass().getName());
            }else{
                System.err.printf("Failed to add Non-Static Module \"%s\" (\"%s\").%n", module,module.getClass().getName());
            }
        }
    }

    public static void registerSlashCommand(Class<?> command)
    {
        if(command.getAnnotation(SlashCommandBase.class) != null){
            slashCommands.add(command);
            System.out.printf("Adding Slash-Command Module \"%s\" (\"%s\").%n", command, command.getName());
        }else{
            System.err.printf("Failed to add Slash-Command Module \"%s\" (\"%s\").%n", command, command.getName());
        }
    }

    public static void passArgumentsToModules( Object... args )
    {
        System.out.println("Processing Modules with arguments "+Arrays.toString(args));
        if(!modules.isEmpty()) {
            System.out.printf("Number of Modules: %d%n", modules.size());
            modules.forEach(module -> {
                if(Class.class.isAssignableFrom(module.getClass())) {
                    findAndInvokeMatchingMethods(null, ((Class<?>) module).getMethods(), args);
                }else{
                    findAndInvokeMatchingMethods(module, module.getClass().getMethods(), args);
                }
            });
            System.out.println("Finished Processing all Modules.");
        }else {
            System.out.println("No Modules to process.");
        }
    }

    public static void updateAllSlashCommands( DiscordApi api)
    {
        api.getGlobalSlashCommands().join().parallelStream().forEachOrdered(
                slashCommand -> slashCommand.deleteGlobal().whenComplete(ModuleLoader::onSlashCommandDelete)
        );
        api.getServers().parallelStream().forEachOrdered(
                server -> server.getSlashCommands().join().parallelStream().forEachOrdered(slashCommand -> slashCommand.deleteForServer(server).whenComplete(ModuleLoader::onSlashCommandDelete).join())
        );
        slashCommands.parallelStream().forEachOrdered(
                command -> createSlashCommand(collectAllPossibleServers(command,api), SlashCommandHandler.createSlashCommand(command),api)
        );
    }

    private static void createSlashCommand(List<Optional<Server>> servers, SlashCommandBuilder builder, DiscordApi api)
    {
        if(servers.isEmpty()){
            builder.createGlobal(api).whenComplete(ModuleLoader::onSlashCommandCreate);
        }else{
            servers.parallelStream().forEach(
                    server -> {
                        if(server.isPresent()) {
                            builder.createForServer(server.get()).whenComplete(ModuleLoader::onSlashCommandCreate);
                        }else{
                            System.out.println("Skipping over invalid server.");
                        }
                    }
            );
        }
    }

    private static List<Optional<Server>> collectAllPossibleServers(Class<?> commandClass, DiscordApi api)
    {
        long[] serverIds = commandClass.getAnnotation(SlashCommandBase.class).server();
        Optional<Server>[] servers = new Optional[serverIds.length];
        for(int i=0; i<serverIds.length; i++) {
            servers[ i ] = api.getServerById(serverIds[ i ]);
            if(servers[i].isEmpty())
                System.err.println("Module Loader was Unable to find server ID \""+serverIds[i]+"\".");
        }
        return List.of(servers);
    }


    private static <T extends SlashCommand, E extends Throwable> void onSlashCommandCreate(T command, E exc)
    {
        if( exc == null){
            System.out.printf("Slash Command \"%s\" was successfully created.%n",command.getName());
        }else{
            System.err.println("Error while creating registering slash command.");
            exc.printStackTrace();
        }
    }

    private static <V extends Void, E extends Throwable> void onSlashCommandDelete(V result, E exception)
    {
        if(exception == null){
            System.out.println("A slash command was successfully deleted.");
        }else{
            System.err.println("A slash command has failed to be deleted.");
            exception.printStackTrace();
        }
    }

    private static void findAndInvokeMatchingMethods( Object module, Method[] methods, Object... args)
    {
        Arrays.stream(methods).forEachOrdered(
                method -> {
                    try {
                        if (hasListenerMethod(method, module == null,args)) {
                            invokeListenerMethod(method, module, args);
                        }else {
                            //System.out.println("Not a listener method.");
                        }
                    } catch(Throwable e){
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
        );
    }

    private static boolean hasListenerMethod( Method method, boolean isStatic, Object... args)
    {
        return method.getParameterCount() == args.length &&
                Modifier.isPublic(method.getModifiers()) &&
                Modifier.isStatic(method.getModifiers()) == isStatic &&
                method.getDeclaringClass() != Object.class &&
                argsMatch(method,args);
    }

    private static void invokeListenerMethod(Method method, Object module, Object... args)
    {
        System.out.printf("Invoking API Builder on%s Class: \"%s\" Method: \"%s\".%n",module != null ? " Object \""+module+"\"" : "",method.getDeclaringClass().getName(),method.getName());
        try{
            method.invoke(module, args);
        }catch( Throwable e){
            System.err.println("Error trying to invoke method: "+method.getName());
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean argsMatch(Method method, Object... args)
    {
        int count = method.getParameterCount();
        if(count != args.length)
            return false;
        Class<?>[] types = method.getParameterTypes();
        for(int i=0; i<count; i++)
            if(!types[i].isAssignableFrom(args[i].getClass()))
                return false;
        return true;
    }


}
