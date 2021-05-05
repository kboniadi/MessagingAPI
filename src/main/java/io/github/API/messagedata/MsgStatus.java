package io.github.API.messagedata;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class MsgStatus {
    private final MsgStatusCategory category;
    private final MsgStatusOperation operation;
}
