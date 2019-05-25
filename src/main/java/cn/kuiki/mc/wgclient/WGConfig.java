package cn.kuiki.mc.wgclient;

public final class WGConfig {
    protected String serverName;
    protected String hostURL;

    public WGConfig(String serverName, String hostURL) {
        this.serverName = serverName;
        this.hostURL = hostURL;
    }

    public String getHostURL() {
        return hostURL;
    }

    public void setHostURL(String hostURL) {
        this.hostURL = hostURL;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

}
