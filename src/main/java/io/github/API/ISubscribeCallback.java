package io.github.API;

public interface ISubscribeCallback {
    void resolved(MessagingAPI api, MessageResultAPI result);
    default void rejected(Exception e) throws Exception {
        // default impl
        throw e;
    }
}
