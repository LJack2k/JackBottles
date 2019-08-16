/*
#
#   This file has been inspired by the guys at DiscordSRV. They make a very awesome
#   Discord plugin for Spigot that I recommend to everybody. Since I'm a beginner
#   coder I learned a lot from their code. Copied a lot of the structure of DiscordSRV
#   because it is so damm good.
#
#   You should totaly check them out at https://www.spigotmc.org/resources/discordsrv.18494/
#   or their github https://github.com/Scarsz/DiscordSRV
#
*/

package com.ljack2k.JackBottles.Utils;

import com.ljack2k.JackBottles.JackBottles;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigUtil {

	public static void migrate() {
        Version configVersion = JackBottles.config().getString("ConfigVersion").split("\\.").length == 3 ? Version.valueOf(JackBottles.config().getString("ConfigVersion")) : Version.valueOf("1." + JackBottles.config().getString("ConfigVersion"));
        Version pluginVersion = Version.valueOf(JackBottles.getPlugin().getDescription().getVersion());

        if (configVersion.equals(pluginVersion)) return; // no migration necessary
        if (configVersion.greaterThan(pluginVersion)) {
            JackBottles.warning("You're attempting to use a higher config version than the plugin. Things might not work correctly.");
            return;
        }

        JackBottles.info("Your JackBottles config file was outdated; attempting migration...");
        try {
            if (configVersion.greaterThanOrEqualTo(Version.forIntegers(1, 13, 0))) {
                // messages
                File messagesFrom = new File(JackBottles.getPlugin().getDataFolder(), "messages.yml-build." + configVersion + ".old");
                File messagesTo = JackBottles.getPlugin().getMessagesFile();
                FileUtils.moveFile(messagesTo, messagesFrom);
                LangUtil.saveMessages();
                copyYmlValues(messagesFrom, messagesTo);
                LangUtil.reloadMessages();

                // config
                File configFrom = new File(JackBottles.getPlugin().getDataFolder(), "config.yml-build." + configVersion + ".old");
                File configTo = JackBottles.getPlugin().getConfigFile();
                FileUtils.moveFile(configTo, configFrom);
                LangUtil.saveConfig();
                copyYmlValues(configFrom, configTo);
                JackBottles.getPlugin().reloadConfig();
            } else {
                // messages
                File messagesFrom = new File(JackBottles.getPlugin().getDataFolder(), "config.yml");
                File messagesTo = JackBottles.getPlugin().getMessagesFile();
                LangUtil.saveMessages();
                copyYmlValues(messagesFrom, messagesTo);
                LangUtil.reloadMessages();

                // config
                File configFrom = new File(JackBottles.getPlugin().getDataFolder(), "config.yml-build." + configVersion + ".old");
                File configTo = JackBottles.getPlugin().getConfigFile();
                FileUtils.moveFile(configTo, configFrom);
                LangUtil.saveConfig();
                copyYmlValues(configFrom, configTo);
                JackBottles.getPlugin().reloadConfig();
            }
            JackBottles.info("Successfully migrated configuration files to version " + JackBottles.config().getString("ConfigVersion"));
        } catch(Exception e){
            JackBottles.error("Failed migrating configs: " + e.getMessage());
        }
    }

    private static void copyYmlValues(File from, File to) {
        try {
            List<String> oldConfigLines = Arrays.stream(FileUtils.readFileToString(from, Charset.forName("UTF-8")).split(System.lineSeparator() + "|\n")).collect(Collectors.toList());
            List<String> newConfigLines = Arrays.stream(FileUtils.readFileToString(to, Charset.forName("UTF-8")).split(System.lineSeparator() + "|\n")).collect(Collectors.toList());

            Map<String, String> oldConfigMap = new HashMap<String, String>();
            for (String line : oldConfigLines) {
                if (line.startsWith("#") || line.startsWith("-") || line.isEmpty()) continue;
                String[] lineSplit = line.split(":", 2);
                if (lineSplit.length != 2) continue;
                String key = lineSplit[0];
                String value = lineSplit[1].trim();
                oldConfigMap.put(key, value);
            }

            Map<String, String> newConfigMap = new HashMap<String, String>();
            for (String line : newConfigLines) {
                if (line.startsWith("#") || line.startsWith("-") || line.isEmpty()) continue;
                String[] lineSplit = line.split(":", 2);
                if (lineSplit.length != 2) continue;
                String key = lineSplit[0];
                String value = lineSplit[1].trim();
                newConfigMap.put(key, value);
            }

            for (String key : oldConfigMap.keySet()) {
                if (newConfigMap.containsKey(key) && !key.startsWith("ConfigVersion")) {
                    JackBottles.debug("Migrating config option " + key + " with value " + oldConfigMap.get(key) + " to new config");
                    newConfigMap.put(key, oldConfigMap.get(key));
                }
            }

            for (String line : newConfigLines) {
                if (line.startsWith("#") || line.startsWith("ConfigVersion") || line.isEmpty()) continue;
                String key = line.split(":")[0];
                if (oldConfigMap.containsKey(key))
                    newConfigLines.set(newConfigLines.indexOf(line), key + ": " + newConfigMap.get(key));
            }

            FileUtils.writeStringToFile(to, String.join(System.lineSeparator(), newConfigLines), Charset.forName("UTF-8"));
        } catch (Exception e) {
            JackBottles.warning("Failed to migrate config: " + e.getMessage());
            e.printStackTrace();
        }
    }

}