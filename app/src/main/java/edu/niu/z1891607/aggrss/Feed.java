package edu.niu.z1891607.aggrss;

public class Feed {
    private int id;
    private final String title;
    private final String url;
    private boolean enabled = true;

    public Feed(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public void setId(int id) { this.id = id; }

    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
