package cn.kuiki.mc;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import cn.kuiki.mc.wgclient.WGClient;
import cn.kuiki.mc.wgclient.WGConfig;

public final class WeCraft extends JavaPlugin implements WeCraftInf {// 继承类
    private WGClient wgClient;

    @Override
    public void onEnable() {
        // 启用插件时自动发出
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        WGConfig wgConfig = new WGConfig(config.getString("manager-host"));
        this.wgClient = new WGClient(wgConfig, getLogger(), this);
        this.wgClient.start();
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

}
