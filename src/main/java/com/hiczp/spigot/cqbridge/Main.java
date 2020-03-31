package com.hiczp.spigot.cqbridge;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.PicqConfig;
import cc.moecraft.icq.event.IcqListener;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    private PicqBotX bot;
    private Listener playerChatListener;

    private void start() {
        if (!getConfig().getBoolean("enable")) return;
        PicqConfig picqConfig = new PicqConfig(getConfig().getInt("bot.listenPort"))
                .setLogPath("")
                .setAccessToken(getConfig().getString("bot.accessToken"))
                .setSecret(getConfig().getString("bot.secret"))
                .setApiAsync(getConfig().getBoolean("bot.apiAsync"))
                .setApiRateLimited(getConfig().getBoolean("bot.apiRateLimited"))
                .setNoVerify(getConfig().getBoolean("bot.noVerify"))
                .setDebug(getConfig().getBoolean("bot.debug"));
        bot = new PicqBotX(picqConfig);
        bot.addAccount(
                getConfig().getString("bot.name"),
                getConfig().getString("bot.postUrl"),
                getConfig().getInt("bot.postPort")
        );
        long groupId = getConfig().getLong("qqGroupNumber");
        if (getConfig().getBoolean("qqChatToGame.enable")) {
            IcqListener receiveGroupMessageListener = new ReceiveGroupMessageListener(
                    this,
                    groupId,
                    getConfig().getBoolean("qqChatToGame.useGroupCard"),
                    getConfig().getString("qqChatToGame.format"),
                    getConfig().getBoolean("qqChatToGame.removeColorInMessage"),
                    getConfig().getConfigurationSection("qqChatToGame.cqCode"),
                    getConfig().getBoolean("qqChatToGame.ignoreEmptyMessage"),
                    getConfig().getBoolean("qqChatToGame.async")
            );
            bot.getEventManager().registerListeners(receiveGroupMessageListener);
        }
        if (getConfig().getBoolean("gameChatToQQ.enable")) {
            playerChatListener = new PlayerChatListener(
                    bot,
                    groupId,
                    getConfig().getString("gameChatToQQ.format"),
                    getConfig().getBoolean("gameChatToQQ.removeColorInPlayerName"),
                    getConfig().getBoolean("gameChatToQQ.removeColorInMessage"),
                    getConfig().getBoolean("gameChatToQQ.ignoreEmptyMessage"),
                    getConfig().getBoolean("gameChatToQQ.async")
            );
            getServer().getPluginManager().registerEvents(playerChatListener, this);
        }
        bot.startBot();
    }

    private void stop() {
        if (bot != null) {
            bot.getHttpServer().getServer().stop(0);
        }
        if (playerChatListener != null) {
            AsyncPlayerChatEvent.getHandlerList().unregister(playerChatListener);
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stop();
    }

    protected void reload() {
        reloadConfig();
        stop();
        start();
    }
}
