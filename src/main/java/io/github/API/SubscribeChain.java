package io.github.API;

import io.github.API.utils.BufferWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class SubscribeChain implements IChannels, IExecute {
    private final JSONObject jsonBuffer;          // json buffer for sending messages
    private final BufferWrapper buffer;

    /**
     * Constructor
     * @param json shadow json object
     * @param buffer shadow BufferWrapper instance
     * @author Kord Boniadi
     */
    SubscribeChain(JSONObject json, BufferWrapper buffer) {
        this.jsonBuffer = json;
        this.buffer = buffer;
    }

    /**
     * @param channels array of channels
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public SubscribeChain channels(String... channels) {
        jsonBuffer.put("channels", new JSONArray(Arrays.stream(
                channels)
                .toArray()));
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
     * Checks that the channels() method was called
     * @author Kord Boniadi
     */
    private void validate() {
        if (jsonBuffer.isNull("channels"))
            throw new IllegalArgumentException("channels were not set");
    }

}
