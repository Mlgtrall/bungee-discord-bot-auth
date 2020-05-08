package ru.mlgtrall.jda_bot_bungee.bungee.command;

import net.dv8tion.jda.core.entities.Member;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import ru.mlgtrall.jda_bot_bungee.Main;
import ru.mlgtrall.jda_bot_bungee.bungee.connection.Connection;
import ru.mlgtrall.jda_bot_bungee.bungee.util.ChatManager;
import ru.mlgtrall.jda_bot_bungee.bungee.util.CommandUtils;
import ru.mlgtrall.jda_bot_bungee.bungee.util.TitleManager;
import ru.mlgtrall.jda_bot_bungee.io.ConfigFiles;
import ru.mlgtrall.jda_bot_bungee.io.FileLoader;
import ru.mlgtrall.jda_bot_bungee.io.config.ConfigFile;
import ru.mlgtrall.jda_bot_bungee.io.config.YMLKeys;
import ru.mlgtrall.jda_bot_bungee.jda.BotFactory;
import ru.mlgtrall.jda_bot_bungee.jda.util.JDAConfigUtils;

import java.util.Map;
import java.util.UUID;

public class AuthCommand extends Command {
    private final Main plugin;
    private static final BotFactory botFactory = BotFactory.getInstance();

    public AuthCommand(@NotNull Main plugin){
        super("authme", "");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(CommandUtils.isPlayer(sender))return;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        FileLoader fileLoader = plugin.getFileLoader();
        ConfigFile playerDBFile = fileLoader.get(ConfigFiles.PLAYER_DB_YML);
        Configuration playerDB = playerDBFile.getConfig();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        //Check for player data in database
        if(playerDB.contains(YMLKeys.DISCORD_ID.addBeforePath(playerName).getPath())){
            player.sendMessage(ChatManager.fromConfig("ALREADY_AUTH", true));
            return;
        }

        if(args.length != 1){
            TitleManager.send(player, ChatManager.fromConfigRaw("title_auth"));
            player.sendMessage(ChatManager.fromConfig("WRONG_AUTH_ARGS", true));
            return;
        }

        Map<String, String> nameCodeMap = plugin.getNameCodeMap();
        
        //Проверка на валидность кода
        String actualcode = nameCodeMap.get(playerName);
        assert actualcode != null;

        if(!actualcode.equals(args[0])){
            TitleManager.send(player, ChatManager.fromConfigRaw("title_auth"));
            player.sendMessage(ChatManager.fromConfig("wrong_code", true));
            return;
        }

       Map<String ,String > nameMineIdDiscordMap = plugin.getNameMineIdDiscordMap();

        String discordid = nameMineIdDiscordMap.get(playerName);
        assert discordid != null;
        //String discordid = playerDB.getString(playerName + ".DISCORD_ID");
        Member target = botFactory.getGuild().getMemberById(discordid);

        if(target == null){ //Player left discord
            nameCodeMap.remove(playerName);
            nameMineIdDiscordMap.remove(playerName);
            playerDB.set(YMLKeys.DISCORD_ID.addBeforePath(playerName).getPath(), null);
            player.sendMessage(ChatManager.fromConfig("no_discord", true));
            Connection.kick(player,"");
            return;
        }

        playerDB.set(YMLKeys.MINE_UUID.addBeforePath(playerName).getPath(), playerUUID.toString());
        playerDB.set(YMLKeys.DISCORD_ID.addBeforePath(playerName).getPath(), discordid);
        playerDBFile.save();

        //Removing data from local HashMaps
        nameCodeMap.remove(playerName);
        nameMineIdDiscordMap.remove(playerName);

        //Сообщение об удачной аутентификации
        player.sendMessage(ChatManager.fromConfig("done_auth", true));
        target.getUser().openPrivateChannel().complete().sendMessage(JDAConfigUtils.fromConfig("done_auth")).queue();

        //Сообщение о необходимой регистрации
        TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
        player.sendMessage(ChatManager.fromConfig("join_2", true));


    }


}
