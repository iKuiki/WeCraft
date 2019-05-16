package cn.kuiki.mc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import cn.kuiki.mc.wgclient.WGClient;
import cn.kuiki.mc.wgclient.WGConfig;

public final class WeCraft extends JavaPlugin implements WeCraftInf,Listener {// 继承类
    private WGClient wgClient;

    @Override
    public void onEnable() {
        // 启用插件时自动发出
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        WGConfig wgConfig = new WGConfig(config.getString("manager-host"));
        this.wgClient = new WGClient(wgConfig, getLogger(), this);
        this.wgClient.start();
        getServer().getPluginManager().registerEvents(this, this);
        this.wgClient.sendTextMessageToChatroom("系统", "测试消息");
    }

    @Override
    public void onDisable() {
        // 关闭插件时自动发出
    }

    @Override
    public void broadcast(String text) {
        this.getLogger().info("broadcast message: " + text);
    }

    // 玩家进入游戏
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    this.wgClient.sendTextMessageToChatroom("","「"+event.getPlayer().getName() + "」进入了游戏");
  }

  // 玩家离开游戏
  @EventHandler
  public void onQuit(PlayerQuitEvent event){
    this.wgClient.sendTextMessageToChatroom("","「"+event.getPlayer().getName() + "」离开了游戏");
  }
  
  // 玩家发送聊天信息
  @EventHandler
  public void onChat(AsyncPlayerChatEvent event){
      this.wgClient.sendTextMessageToChatroom(event.getPlayer().getName(), event.getMessage());
  }
}
