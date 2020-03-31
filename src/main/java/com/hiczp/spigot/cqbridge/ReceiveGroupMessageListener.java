package com.hiczp.spigot.cqbridge;

import cc.moecraft.icq.event.EventHandler;
import cc.moecraft.icq.event.IcqListener;
import cc.moecraft.icq.event.events.message.EventGroupMessage;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReceiveGroupMessageListener extends IcqListener {
    private JavaPlugin plugin;
    private Long groupId;
    private boolean useGroupCard;
    private String messageFormat;
    private boolean removeColorInMessage;
    private ConfigurationSection cqCodeConfig;
    private boolean ignoreEmptyMessage;
    private boolean async;

    public ReceiveGroupMessageListener(JavaPlugin plugin, Long groupId, boolean useGroupCard, String messageFormat, boolean removeColorInMessage, ConfigurationSection cqCodeConfig, boolean ignoreEmptyMessage, boolean async) {
        this.plugin = plugin;
        this.groupId = groupId;
        this.useGroupCard = useGroupCard;
        this.messageFormat = messageFormat;
        this.removeColorInMessage = removeColorInMessage;
        this.cqCodeConfig = cqCodeConfig;
        this.ignoreEmptyMessage = ignoreEmptyMessage;
        this.async = async;
    }

    @EventHandler
    public void handler(EventGroupMessage event) {
        if (groupId != null && !groupId.equals(event.getGroupId())) return;

        Runnable task = () -> process(event);
        if (async) {
            CompletableFuture.runAsync(task);
        } else {
            task.run();
        }
    }

    private void process(EventGroupMessage event) {
        String message = event.message;
        if (removeColorInMessage) message = message.replaceAll("§.", "");
        if (cqCodeConfig.getBoolean("removeAll")) {
            message = message.replaceAll("\\[CQ:.*]", "");
        } else {
            //TODO
        }

        if (ignoreEmptyMessage && message.trim().isEmpty()) return;

        String name = event.getSender().getInfo().getNickname();
        if (useGroupCard) {
            String groupCard = event.getGroupSender().getInfo().getCard();
            if (groupCard != null && !groupCard.isEmpty()) name = groupCard;
        }
        Map<String, String> values = new HashMap<>();
        values.put("name", name);
        values.put("message", message);
        plugin.getServer().broadcastMessage(StrSubstitutor.replace(messageFormat, values));
    }

    private String convertCqCode(String cqCode) {
        return cqCode;
    }
}
