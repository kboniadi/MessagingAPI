package io.github.API.proj;

public interface ISubscribeCallback {
    void resolved(MessagingAPI api, String json);
    default void rejected(Exception e) throws Exception {
        // default impl
        throw e;
    }
}
