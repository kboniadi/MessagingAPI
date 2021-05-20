package io.github.API;

import io.github.API.utils.IOWrapper;
import io.github.API.utils.GsonWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PublishChain implements IMessage, IChannel, IExecute {
    private final String CHANNEL_KEY = "channels";
    private String publisherUuid;
    private final String type;
    private Object message;
    private String channel;
    private final IOWrapper buffer;

    /**
     * Constructor
     * @param type json type
     * @param buffer shadow BufferWrapper instance
     * @author Kord Boniadi
     */
    PublishChain(String uuid, String type, IOWrapper buffer) {
        this.publisherUuid = uuid;
        this.type = type;
        this.buffer = buffer;
        this.message = null;
        this.channel = null;
    }

    /**
     * @param message message Object
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public PublishChain message(Object message) {
        this.message = message;
        return this;
    }

    /**
     * writes the built data to the server
     * @author Kord Boniadi
     */
    @Override
    public void execute() {
        validate();
        JSONObject temp = null;
        if (!(this.message instanceof  Object[]) &&
                !(this.message instanceof Collection) &&
                !(this.message instanceof Map)) {
            temp = new JSONObject(this.message);
        }
        JSONObject json = new JSONObject();
        json.put(CHANNEL_KEY, this.channel);
        json.put("uuid", this.publisherUuid);
        json.put("type", this.type);
        json.put("message", (temp == null) ? this.message : temp);
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
