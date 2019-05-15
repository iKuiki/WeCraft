package cn.kuiki.mc;

import org.bukkit.plugin.java.JavaPlugin;
import cn.kuiki.mc.wgclient.WGClient;

public final class WeCraft extends JavaPlugin {// 继承类
    private WGClient wgClient;

    @Override
    public void onEnable() {
        // 启用插件时自动发出
        this.wgClient = new WGClient(getLogger());
        this.wgClient.Test();
    }

    @Override
    public void onDisable() {
        // 关闭插件时自动发出
    }
}
