package io.github.API;

import io.github.API.utils.BufferWrapper;
import org.json.JSONObject;

public class PublishChain implements IMessage, IChannel, IExecute {
    private JSONObject jsonBuffer;          // json buffer for sending messages
    private final BufferWrapper buffer;
    private boolean isMessageCreated = false;

    /**
     * Constructor
     * @param json shadow json object
     * @param buffer shadow BufferWrapper instance
     * @author Kord Boniadi
     */
    PublishChain(JSONObject json, BufferWrapper buffer) {
        this.jsonBuffer = json;
        this.buffer = buffer;
    }

    /**
     * @param json message in json form
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public PublishChain message(String json) {
        JSONObject temp = new JSONObject(json);
        var iterator = this.jsonBuffer.keys();
        iterator.forEachRemaining(key -> {
            temp.put(key, this.jsonBuffer.get(key));
        });
        this.jsonBuffer = temp;
        isMessageCreated = true;
        return this;
    }

    /**
     * writes the built data to the server
     * @author Kord Boniadi
     */
    @Override
    public void execute() {
        validate();
        buffer.writeLine(jsonBuffer.toString());
    }

    /**
     * Checks that both channel() and message() methods were called
     * @author Kord Boniadi
     */
    private void validate() {
        if (jsonBuffer.isNull("channels"))
            throw new IllegalArgumentException("channel was not set");
        else if (!isMessageCreated)
            throw new IllegalArgumentException("message was never created");
    }

    /**
     * @param channel channel name
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public PublishChain channel(String channel) {
        jsonBuffer.put("channels", channel);
        return this;
    }
}
