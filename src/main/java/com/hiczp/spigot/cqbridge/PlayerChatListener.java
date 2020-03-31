package com.hiczp.spigot.cqbridge;

import cc.moecraft.icq.PicqBotX;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlayerChatListener implements Listener {
    private PicqBotX bot;
    private long groupId;
    private String format;
    private boolean removeColorInPlayerName;
    private boolean removeColorInMessage;
    private boolean ignoreEmptyMessage;
    private boolean async;

    public PlayerChatListener(PicqBotX bot, long groupId, String format, boolean removeColorInPlayerName, boolean removeColorInMessage, boolean ignoreEmptyMessage, boolean async) {
        this.bot = bot;
        this.groupId = groupId;
        this.format = format;
        this.removeColorInPlayerName = removeColorInPlayerName;
        this.removeColorInMessage = removeColorInMessage;
        this.ignoreEmptyMessage = ignoreEmptyMessage;
        this.async = async;
    }

    @EventHandler
    public void sendChatToQQ(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (ignoreEmptyMessage && message.trim().isEmpty()) return;
        String playerName = event.getPlayer().getDisplayName();
        if (removeColorInPlayerName) playerName = playerName.replaceAll("§.", "");
        if (removeColorInMessage) message = message.replaceAll("§.", "");

        Map<String, String> values = new HashMap<>();
        values.put("playerName", playerName);
        values.put("message", message);
        String formattedMessage = StrSubstitutor.replace(format, values);

        Runnable task = () -> bot.getAccountManager().getNonAccountSpecifiedApi().sendGroupMsg(groupId, formattedMessage);
        if (async) {
            CompletableFuture.runAsync(task);
        } else {
            task.run();
        }
    }
}
