package ru.mlgtrall.jda_bot_bungee_auth.discord;

import com.tjplaysnow.discord.object.Bot;
import com.tjplaysnow.discord.object.CommandConsoleManager;
import com.tjplaysnow.discord.object.ThreadHandle;
import lombok.Getter;
import net.dv8tion.jda.core.entities.*;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.jetbrains.annotations.NotNull;
import ru.mlgtrall.jda_bot_bungee_auth.Main;
import ru.mlgtrall.jda_bot_bungee_auth.bootstrap.InjectorContainer;
import ru.mlgtrall.jda_bot_bungee_auth.bootstrap.Reloadable;
import ru.mlgtrall.jda_bot_bungee_auth.discord.command.AuthMeCommand;
import ru.mlgtrall.jda_bot_bungee_auth.settings.Settings;
import ru.mlgtrall.jda_bot_bungee_auth.settings.holders.DiscordSettings;
import ru.mlgtrall.jda_bot_bungee_auth.util.Pair;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class DiscordBotService implements Reloadable {

    @Inject
    private Main pl;

    @Inject
    private Logger log;

    @Inject
    private Settings settings;

    @Inject
    private TaskScheduler scheduler;

    private List<String> listenableChannelsIds;

    private List<String> listenableChannelsNames;

    private List<String> requiredRolesIds;

    private List<String> requiredRolesNames;

    @Getter
    private DiscordBot bot;

    public DiscordBotService(){
    }

    @PostConstruct
    @Override
    public void reload() {
        listenableChannelSettings();
        requiredRolesSettings();
    }

    private void listenableChannelSettings(){

        listenableChannelsIds = new ArrayList<>();
        listenableChannelsNames = new ArrayList<>();

        List<String> ids = settings.getProperty(DiscordSettings.Channel.Text.Listen.BY_ID);
        List<String> names = settings.getProperty(DiscordSettings.Channel.Text.Listen.BY_NAME);

        listenableChannelsNames.addAll(names);
        listenableChannelsIds.addAll(ids);

    }

    private void requiredRolesSettings(){

        requiredRolesIds = new ArrayList<>();
        requiredRolesNames = new ArrayList<>();

        List<String> ids = settings.getProperty(DiscordSettings.Role.BY_ID);
        List<String> names = settings.getProperty(DiscordSettings.Role.BY_NAME);

        requiredRolesNames.addAll(names);
        requiredRolesIds.addAll(ids);
    }

    /**
     * Starting bot, using Discord API by tjplaysnow:
     * <br/>
     * <code>
     * bot = new Bot(jdaConfigUtils.fromConfig("token"), prefix);
     * bot.setBotThread(new ThreadHandle());
     * bot.setConsoleCommandManager(new CommandConsoleManager());
     * </code>
     * <br/><br/>
     * Classic JDA Style starting:
     * <br/>
     * <code>
     * try {
     *  jda = new JDABuilder(AccountType.BOT)
     *  .setToken(jdaUtils.fromConfig("TOKEN"))
     *  .addEventListeners(new GuildMessageListener(plugin,this))
     *  .setHttpClient(new OkHttpClient.Builder().build())
     *  .build()
     *  .awaitReady();
     *
     * } catch(LoginException | InterruptedException e) {
     *  e.printStackTrace();
     * }
     * </code>
    */
    public void startBot() {

        if(bot != null) return;

        String prefix = settings.getProperty(DiscordSettings.PREFIX);
        String token = settings.getProperty(DiscordSettings.TOKEN);

        Objects.requireNonNull(prefix, "Discord bot's prefix can't be null.");
        Objects.requireNonNull(token, "Discord bot's token can't be null.");

        log.info("Starting discord bot...");
        Bot bot = new Bot(token, prefix);
        log.info("Bot has started with prefix = \"" + prefix + "\"");

        log.info("Registering bot thread...");
        bot.setBotThread(new ThreadHandle());
        log.info("Done!");

        log.info("Registering bot command manager...");
        bot.setConsoleCommandManager(new CommandConsoleManager());
        log.info("Done!");

        log.info("Registering guild message listener...");
        bot.addCommand(InjectorContainer.get().getSingleton(AuthMeCommand.class));
        log.info("Done!");

        AtomicReference<Guild> relevantGuild = new AtomicReference<>();
        //Discord gives a wealthy guild object after about a 1 sec
        scheduler.schedule(pl, () -> {
            relevantGuild.set(bot.getBot().getGuilds().get(0));
            this.bot = DiscordBot.builder().tjBot(bot).relevantGuild(relevantGuild.get()).build();

            bot.setGame("Minecraft | MCFP");

            Guild guild = this.bot.getRelevantGuild();
            //Collecting channel entities from guild with ids and names
            Set<MessageChannel> channels = new HashSet<>();
            for(String id:listenableChannelsIds){
                MessageChannel channel = guild.getTextChannelById(id);
                if(channel!=null){
                    channels.add(channel);
                }
            }
            for(String name:listenableChannelsNames){
                List<TextChannel> channelsWithSameName = guild.getTextChannelsByName(name, false);
                for(MessageChannel channel : channelsWithSameName){
                    if(channel != null){
                        channels.add(channel);
                    }
                }
            }

            //Collecting role entities from guild with ids and names
            Set<Role> roles = new HashSet<>();
            for(String id:requiredRolesIds){
                Role role = guild.getRoleById(id);
                if(role != null){
                    roles.add(role);
                }
            }
            for(String name: requiredRolesNames){
                List<Role> rolesWithSameName = guild.getRolesByName(name, false);
                for(Role role: rolesWithSameName){
                    if(role!=null){
                        roles.add(role);
                    }
                }
            }

            log.info("Bot is listening channels:");
            int i = 1;
            for(MessageChannel channel : channels){
                log.info(i++ + ". Name: \"" + channel.getName() + "\"; Id: \"" + channel.getId()+"\"");
            }
            log.info("User required to have one of this roles:");
            i = 1;
            for(Role role : roles){
                log.info(i++ + ". Name: \"" + role.getName() + "\"; Id: \"" + role.getId()+"\"");

            }


            log.info("Assembling discord bot done!");
            }, 1, TimeUnit.SECONDS);
    }

    public boolean hasListenableChannel(@NotNull MessageChannel channel){
        return listenableChannelsIds.contains(channel.getId()) || listenableChannelsNames.contains(channel.getName());
    }

    public boolean hasListenableChannel(@NotNull String idOrName){
        return listenableChannelsIds.contains(idOrName) || listenableChannelsNames.contains(idOrName);
    }


    public boolean hasRequiredRole(@NotNull Member member){
        List<Role> memberRoles = member.getRoles();
        AtomicBoolean hasRole = new AtomicBoolean();
        memberRoles.forEach(role -> {
            if(requiredRolesIds.contains(role.getId()) || requiredRolesNames.contains(role.getName())) hasRole.set(true);
        });
        return hasRole.get();
    }

    public boolean isInGuildCurrently(@NotNull String userId){
        Guild guild = bot.getRelevantGuild();
        Member member = guild.getMemberById(userId);
        return member != null;
    }

}