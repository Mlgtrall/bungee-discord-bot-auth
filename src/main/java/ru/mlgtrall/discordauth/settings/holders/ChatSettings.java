package ru.mlgtrall.discordauth.settings.holders;

import ch.jalu.configme.SettingsHolder;

public class ChatSettings implements SettingsHolder {

    private ChatSettings(){}

    public static class Commands implements SettingsHolder{

        public static class Auth implements SettingsHolder{

        }

        public static class Login implements SettingsHolder{

        }

        public static class Reg implements SettingsHolder{

        }
    }
}
