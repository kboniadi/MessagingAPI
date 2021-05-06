package io.github.API.messagedata;

public enum MsgStatusCategory {
    MsgConnectedCategory("Connected"),
    MsgDisconnectedCategory("Disconnected"),
    MsgClosedCategory("Closed"),
    MsgErrorCategory("Error");

    private final String value;

    MsgStatusCategory(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
