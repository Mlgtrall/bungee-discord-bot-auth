package ru.mlgtrall.discordauth.bungee.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import ru.mlgtrall.discordauth.DiscordAuth;
import ru.mlgtrall.discordauth.data.AuthPlayer;
import ru.mlgtrall.discordauth.io.database.DataSource;
import ru.mlgtrall.discordauth.security.HashedPassword;
import ru.mlgtrall.discordauth.security.Password;
import ru.mlgtrall.discordauth.util.*;
import ru.mlgtrall.discordauth.security.Hash;

import javax.inject.Inject;
import java.util.Date;

import static ru.mlgtrall.discordauth.util.BungeeCommandUtils.isPlayer;
import static ru.mlgtrall.discordauth.util.StringUtils.socketAddressToIp;

public class RegisterCommand extends Command {

    @Inject
    private DiscordAuth pl;

    @Inject
    private DataSource db;

    @Inject
    private TaskScheduler scheduler;

    public RegisterCommand(){
        super("reg","discordauth.reg","r");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!isPlayer(sender)){
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String playerName = player.getName();

        AuthPlayer authPlayer = db.getPlayer(playerName);
        assert authPlayer != null;

        //TODO: complete more advanced state checking + move to another class
        if(authPlayer.getDiscordID() != null && authPlayer.getHashedPassword().isComplete()){
            player.sendMessage(BungeeChatConfig.fromConfig("already_reg", true));
            return;
        }else if(authPlayer.getDiscordID() == null) {
            player.sendMessage(BungeeChatConfig.fromConfig("need_auth", true));
            player.sendMessage(BungeeChatConfig.fromConfig("join"));
            return;
        }

        if(args.length != 2){
            player.sendMessage(BungeeChatConfig.fromConfig("wrong_reg", true));
            TitleManager.send(player, BungeeChatConfig.fromConfigRaw("title_reg"));
            return;
        }

        //Check if passwords equal
        if(!args[0].equals(args[1])){
            player.sendMessage(BungeeChatConfig.fromConfig("password_not_equal",true));
            TitleManager.send(player, BungeeChatConfig.fromConfigRaw("title_reg"));
            return;
        }

        String password = Password.returnIfValid(args[0], player);
        if(password == null){
            TitleManager.send(player, BungeeChatConfig.fromConfigRaw("title_reg"));
            return;
        }

        String salt = Hash.createSaltStr();
        String hash = Hash.generateHash(password, salt);

        HashedPassword completedPassword = new HashedPassword(hash, salt);
        authPlayer.setHashedPassword(completedPassword);

        //Message of reg is ok
        player.sendMessage(BungeeChatConfig.fromConfig("reg_ok"));

        Date now = new Date();
        String ip = socketAddressToIp(player.getSocketAddress());

        authPlayer.setRegIP(ip);
        //TODO: add completable future?
        scheduler.runAsync(pl, () -> db.savePlayer(authPlayer));

        //Message of login
        TitleManager.send(player, BungeeChatConfig.fromConfigRaw("title_login"));
        player.sendMessage(BungeeChatConfig.fromConfig("join3",true));
    }
}
