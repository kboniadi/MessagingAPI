package io.github.API;

import io.github.API.utils.BufferWrapper;
import org.json.JSONObject;

public class PublishChain implements IMessage, IChannel, IExecute {
    private final String CHANNEL_KEY = "channels";
    private final String type;
    private String message;
    private String channel;
    private final BufferWrapper buffer;

    /**
     * Constructor
     * @param type json type
     * @param buffer shadow BufferWrapper instance
     * @author Kord Boniadi
     */
    PublishChain(String type, BufferWrapper buffer) {
        this.type = type;
        this.buffer = buffer;
        this.message = null;
        this.channel = null;
    }

    /**
     * @param json message in json form
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public PublishChain message(String json) {
        this.message = json;
        return this;
    }

    /**
     * writes the built data to the server
     * @author Kord Boniadi
     */
    @Override
    public void execute() {
        validate();
        JSONObject json = new JSONObject(this.message);
        json.put(CHANNEL_KEY, this.channel);
        json.put("type", this.type);
        buffer.writeLine(json.toString());
    }

    /**
     * Checks that both channel() and message() methods were called
     * @author Kord Boniadi
     */
    private void validate() {
        if (channel ==  null)
            throw new IllegalArgumentException("channel was not set");
        else if (message == null)
            throw new IllegalArgumentException("message was never created");
    }

    /**
     * @param channel channel name
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public PublishChain channel(String channel) {
        this.channel = channel;
        return this;
    }
}
