package io.github.API.messagedata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class MsgResultAPI {
    private final String channel;
    private final String message;
    private final String uuid;

    public MsgResultAPI(String channel, String message, String uuid) {
        this.channel = channel;
        this.message = message;
        this.uuid = uuid;
    }
}
