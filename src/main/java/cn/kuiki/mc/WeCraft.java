package cn.kuiki.mc;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import cn.kuiki.mc.wgclient.WGClient;
import cn.kuiki.mc.wgclient.WGConfig;

public final class WeCraft extends JavaPlugin implements WeCraftInf, Listener {// 继承类
    private WGClient wgClient;

    @Override
    public void onEnable() {
        // 启用插件时自动发出
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        WGConfig wgConfig = new WGConfig(config.getString("server-name"), config.getString("manager-host"));
        this.wgClient = new WGClient(wgConfig, getLogger(), this);
        this.wgClient.sendRegisterMessageToChatroom();
        this.wgClient.start();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // 关闭插件时自动发出
    }

    @Override
    public void broadcast(String text) {
        this.getLogger().info("broadcast message: " + text);
        Bukkit.broadcastMessage(text);
    }

    // 玩家进入游戏
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.wgClient.sendPlayerJoinMessageToChatroom(event.getPlayer().getName());
    }

    // 玩家离开游戏
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.wgClient.sendPlayerLeaveMessageToChatroom(event.getPlayer().getName());
    }

    // 玩家发送聊天信息
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        this.wgClient.sendPlayerChatMessageToChatroom(event.getPlayer().getName(), event.getMessage());
    }

    // 玩家死亡消息
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        this.wgClient.sendPlayerDeathMessageToChatroom(event.getEntity().getName(), event.getDeathMessage());
    }

    // 玩家达成进度消息
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (!key.startsWith("recipes/")) {
            // 如果不是因为解锁合成进度，则汇报
            this.wgClient.sendPlayerAdvancementDoneMessageToChatroom(event.getPlayer().getName(), key);
        }
    }
}
