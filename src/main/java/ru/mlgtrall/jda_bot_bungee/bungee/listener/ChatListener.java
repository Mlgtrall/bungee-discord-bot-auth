package ru.mlgtrall.jda_bot_bungee.bungee.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import ru.mlgtrall.jda_bot_bungee.Main;
import ru.mlgtrall.jda_bot_bungee.bungee.connection.Connection;
import ru.mlgtrall.jda_bot_bungee.bungee.util.ChatManager;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {
    Main plugin;
    ProxiedPlayer player;
    UUID uuid;
    HashMap<UUID, Integer> playerTriesMap;

    public ChatListener(@NotNull Main plugin){
        this.plugin = plugin;

        playerTriesMap = new HashMap<>();

    }

    @EventHandler
    public void onChatEvent(@NotNull ChatEvent event){
        if(!(event.getSender() instanceof ProxiedPlayer)){
            //plugin.getLogger().info("only player can execute this command!");
            return;
        }
        player = (ProxiedPlayer) event.getSender();
        uuid = player.getUniqueId();
        String message = event.getMessage();
        List<UUID> verifiedMembers = plugin.getVerifiedMembers();
        playerTriesMap.put(uuid, 0);

        if(verifiedMembers.contains(uuid)) return;

        //Check if message is command
        if(!event.isCommand() && !event.isProxyCommand()){

            player.sendMessage(ChatManager.fromConfig("not_command",true));
            event.setCancelled(true);
            triesCounter(event);
            return;
        }

        if(message.contains("/authme") || message.contains("/reg") || message.contains("/login") || message.contains("/r") || message.contains("/l")){
            triesCounter(event);
            return;
        }else{
            player.sendMessage(ChatManager.fromConfig("not_command",true));
            event.setCancelled(true);
            triesCounter(event);
        }

    }

    private void triesCounter(ChatEvent event) {
        int count = playerTriesMap.get(uuid);
        ++count;
        if(count >= 10){
            Connection.kick(event, ChatManager.fromConfig("disconnect"));
            playerTriesMap.remove(uuid);
        }else{
            playerTriesMap.put(uuid, count);
        }
    }
}
