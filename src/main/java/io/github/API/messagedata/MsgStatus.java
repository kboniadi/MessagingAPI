package io.github.API.messagedata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MsgStatus {
    private final MsgStatusCategory category;
    private final MsgStatusOperation operation;
}
