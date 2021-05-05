package io.github.API;

import io.github.API.messagedata.MsgResultAPI;
import io.github.API.messagedata.MsgStatus;

public interface ISubscribeCallback {
    void status(MessagingAPI api, MsgStatus status);
    void resolved(MessagingAPI api, MsgResultAPI result);
    void rejected(Exception e);
}
