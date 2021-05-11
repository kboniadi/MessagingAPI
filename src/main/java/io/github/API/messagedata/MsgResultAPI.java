package io.github.API.messagedata;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MsgResultAPI {
    private final String channel;
    private final String message;
    private final String publisherUuid;
}
