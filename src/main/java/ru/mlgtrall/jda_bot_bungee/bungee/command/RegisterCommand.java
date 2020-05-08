package ru.mlgtrall.jda_bot_bungee.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import ru.mlgtrall.jda_bot_bungee.Main;
import ru.mlgtrall.jda_bot_bungee.bungee.util.ChatManager;
import ru.mlgtrall.jda_bot_bungee.bungee.util.CommandUtils;
import ru.mlgtrall.jda_bot_bungee.bungee.util.TitleManager;
import ru.mlgtrall.jda_bot_bungee.io.ConfigFiles;
import ru.mlgtrall.jda_bot_bungee.io.FileLoader;
import ru.mlgtrall.jda_bot_bungee.io.config.ConfigFile;
import ru.mlgtrall.jda_bot_bungee.io.config.YMLKeys;
import ru.mlgtrall.jda_bot_bungee.security.Hash;
import ru.mlgtrall.jda_bot_bungee.security.Password;

import java.util.Date;
import java.util.UUID;

public class RegisterCommand extends Command {
    Main plugin;
    //ChatManager chatManager;
    public RegisterCommand(@NotNull Main plugin){
        super("reg","","r");
        this.plugin = plugin;
        //this.chatManager = plugin.getChatManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(CommandUtils.isPlayer(sender))return;

        ProxiedPlayer player = (ProxiedPlayer) sender;
        FileLoader fileLoader = plugin.getFileLoader();
        ConfigFile playerDBFile = fileLoader.get(ConfigFiles.PLAYER_DB_YML);
        Configuration playerDB = playerDBFile.getConfig();
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        //Check database
        if(playerDB.contains(YMLKeys.DISCORD_ID.addBeforePath(playerName).getPath()) &&
                playerDB.contains(YMLKeys.PASSWORD.addBeforePath(playerName).getPath())){
            player.sendMessage(ChatManager.fromConfig("already_reg", true));
            return;
        }else if(!playerDB.contains(YMLKeys.DISCORD_ID.addBeforePath(playerName).getPath())){
            player.sendMessage(ChatManager.fromConfig("need_auth",true));
            player.sendMessage(ChatManager.fromConfig("join"));
            return;
        }

        if(args.length != 2){
            player.sendMessage(ChatManager.fromConfig("wrong_reg", true));
            TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
            return;
        }

        //Check if passwords equal
        if(!args[0].equals(args[1])){
            player.sendMessage(ChatManager.fromConfig("password_not_equal",true));
            TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
            return;
        }

//        //Allow only allowed symbols for password
//        for(char c : args[0].toCharArray()){
//            if(!(c >= 'a' && c<= 'z' || c>= 'A' && c<= 'Z' || c>= '0' && c<='9' || c == '_')){
//                player.sendMessage(ChatManager.fromConfig("password_incorrect_char",true));
//                TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
//                return;
//            }
//        }
//
//        //Allow only less than max length
//        if(args[0].length()>32){
//            player.sendMessage(ChatManager.fromConfig("password_too_long", true));
//            //player.sendMessage(chatUtils.fromConfig("join_2"));
//            TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
//            return;
//        }
//
//        //Allow only more than min length
//        if(args[0].length()<6){
//            player.sendMessage(ChatManager.fromConfig("password_too_small", true));
//            //player.sendMessage(chatUtils.fromConfig("join_2"));
//            TitleManager.send(player, ChatManager.fromConfigRaw("title_reg"));
//            return;
//        }

        String password = Password.checkIfValid(args[0], player);
        if(password == null) return;

        String salt = Hash.createSaltStr();
        String hashedPassword = Hash.generateHash(password, salt);

        //playerDB.set(playerName + "." + YMLKeys.PASSWORD.toString(),password);
        //playerDB.set(YMLKeys.PASSWORD.getFullPath(playerName), password);
        playerDB.set(YMLKeys.SALT.addBeforePath(playerName).getPath(), salt);
        playerDB.set(YMLKeys.PASSWD_HASH.addBeforePath(playerName).getPath(), hashedPassword);
        playerDBFile.save();

        player.sendMessage(ChatManager.fromConfig("reg_ok"));

        Date now = new Date();
        String ip = player
                .getSocketAddress()
                .toString()
                .replaceAll("\\.","_")
                .split(":")[0];


        //playerDB.set(playerName+".REG_IP." + ip , now.toString());
        playerDB.set(YMLKeys.REG_IP.addBeforePath(playerName).addToPath(ip).getPath(), now.toString());
        playerDBFile.save();

        TitleManager.send(player, ChatManager.fromConfigRaw("title_login"));
        player.sendMessage(ChatManager.fromConfig("join3",true));
    }
}