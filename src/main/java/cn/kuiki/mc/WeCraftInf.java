package cn.kuiki.mc;

public interface WeCraftInf {
    // 服内广播
    public void broadcast(String text);

    // 执行服务器命令
    public void dispatchCommand(String text);
}
