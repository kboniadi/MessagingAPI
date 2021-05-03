package io.github.API;

public class MessageResultAPI {
    private final String channel;
    private final String json;

    public MessageResultAPI(String channel, String json) {
        this.channel = channel;
        this.json = json;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return json;
    }
}
