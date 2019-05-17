package cn.kuiki.mc.wgclient;

public final class WGConfig {
    protected String hostURL;

    public WGConfig(String hostURL) {
        this.hostURL = hostURL;
    }

    public String getHostURL() {
        return hostURL;
    }

    public void setHostURL(String hostURL) {
        this.hostURL = hostURL;
    }

}
