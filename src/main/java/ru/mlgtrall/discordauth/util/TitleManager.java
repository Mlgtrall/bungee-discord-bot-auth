package ru.mlgtrall.discordauth.util;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static ru.mlgtrall.discordauth.util.TimeUtils.timeToTicks;

//TODO: make configurable settings
public final class TitleManager {

    private TitleManager(){}

    public static class TitleBuilder{

    }


    @Contract("_ -> param1")
    public static @NotNull Title configure(@NotNull Title title){
        title.fadeIn(timeToTicks(3, TimeUnit.SECONDS));
        title.fadeOut(timeToTicks(3, TimeUnit.SECONDS));
        title.stay(timeToTicks(10, TimeUnit.SECONDS));
        return title;
    }

    @Contract("_, _, _, _, _ -> param1")
    public static @NotNull Title configure(@NotNull Title title, int stay, int fadeIn, int fadeOut, TimeUnit timeUnit){
        title.fadeIn(timeToTicks(fadeIn, timeUnit));
        title.fadeOut(timeToTicks(fadeOut, timeUnit));
        title.stay(timeToTicks(stay, timeUnit));
        return title;
    }

    public static void send(ProxiedPlayer player, @NotNull String msg, @NotNull Title title){
        title.reset();
        configure(title);
        String[] strings = msg.split(BungeeChatConfig.lineSeparator);
        title.title(new TextComponent(strings[0]));
        for (int i = 1; i<strings.length; ++i){
            title.subTitle(new TextComponent(strings[i]));
        }
        title.send(player);
    }

    public static void send(ProxiedPlayer player, @NotNull String msg){
        Title title = ProxyServer.getInstance().createTitle();
        title.reset();
        configure(title);
        String[] strings = msg.split(BungeeChatConfig.lineSeparator);
        title.title(new TextComponent(strings[0]));
        for (int i = 1; i<strings.length; ++i){
            title.subTitle(new TextComponent(strings[i]));
        }
        title.send(player);
    }

    public static void send(ProxiedPlayer player, @NotNull String msg, int stay, int fadeIn, int fadeOut, TimeUnit timeUnit){
        Title title = ProxyServer.getInstance().createTitle();
        title.reset();
        configure(title, stay, fadeIn, fadeOut, timeUnit);
        String[] strings = msg.split(BungeeChatConfig.lineSeparator);
        title.title(new TextComponent(strings[0]));
        for (int i = 1; i<strings.length; ++i){
            title.subTitle(new TextComponent(strings[i]));
        }
        title.send(player);
    }

}
