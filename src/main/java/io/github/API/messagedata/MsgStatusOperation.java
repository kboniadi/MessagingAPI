package io.github.API.messagedata;

public enum MsgStatusOperation {
    MsgSubscribeOperation("Subscribed"),
    MsgUnsubscribeOperation("Unsubscribed"),
    MsgClosingOperation("Closed");

    private final String value;

    MsgStatusOperation(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
