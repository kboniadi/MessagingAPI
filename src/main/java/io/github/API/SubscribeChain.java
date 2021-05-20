package io.github.API;

import io.github.API.utils.IOWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class SubscribeChain implements IChannels, IExecute {
    private final String CHANNEL_KEY = "channels";
    private final String type;
    private String[] channels;
    private final IOWrapper buffer;

    /**
     * Constructor
     * @param type json type
     * @param buffer shadow BufferWrapper instance
     * @author Kord Boniadi
     */
    SubscribeChain(String type, IOWrapper buffer) {
        this.type = type;
        this.channels = null;
        this.buffer = buffer;
    }

    /**
     * @param channels array of channels
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public SubscribeChain channels(String... channels) {
        this.channels = channels;
        return this;
    }

    /**
     * writes the built data to the server
     * @author Kord Boniadi
     */
    @Override
    public void execute() {
        validate();
        JSONObject json = new JSONObject();
        json.put(CHANNEL_KEY, channels);
        json.put("type", this.type);
        buffer.writeLine(json.toString());
    }

    /**
     * Checks that the channels() method was called
     * @author Kord Boniadi
     */
    private void validate() {
        if (this.channels == null)
            throw new IllegalArgumentException("channels were not set");
    }

}
