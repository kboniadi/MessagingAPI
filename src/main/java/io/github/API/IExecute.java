package io.github.API;

interface IExecute {
    void execute();
}
interface IChannel {
    IExecute channel(String channel);
}
interface IChannels {
    IExecute channels(String... channels);
}
interface IMessage {
    IChannel message(Object message);
}
