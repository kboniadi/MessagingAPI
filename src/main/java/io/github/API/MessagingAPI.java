package io.github.API;

import io.github.API.messagedata.ThreadCount;
import io.github.API.utils.IOWrapper;
import lombok.Getter;
import lombok.NonNull;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagingAPI implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(MessagingAPI.class);
    private final String serverAddress = "localhost";       // IP For remote Server
    private final ExecutorService server;                   // Thread pool for DB calls
    private Socket mainSocket;                              // Persistent Server connection for api
    private IOWrapper buffer;                               // read/write wrapper for tcp throughput
    @Getter
    private final String uuid = UUID.randomUUID().toString();
    private volatile boolean exit = false;
    private CloseCallback cc;

    static {
        PropertyConfigurator.configure(MessagingAPI.class.getResource("configs/log4j.properties"));
//        Logger.getRootLogger().setLevel(Level.OFF);
    }

//    public static void main(String[] args) {
//        MessagingAPI api = new MessagingAPI();
//
//        api.subscribe().channels("PlayerInfo").execute();
//
//        api.publish().channel("PlayerInfo").message(new PlayerInfo()).execute();
//
//        MessagingAPI api = null;
//        MessagingAPI api2 = null;
//        try {
//            api = new MessagingAPI(ThreadCount.FOUR);
//            api2 = new MessagingAPI(ThreadCount.SYS_DEP);
//            api.subscribe().channels("channel1", "channel2").execute();     // register api to listen to a channel
//            api2.subscribe().channels("channel3").execute();                // register api to listen to a channel
//
//            api.addEventListener(new ISubscribeCallback() {
//                @Override
//                public void status(MessagingAPI api, MsgStatus status) {
//
//                }
//
//                @Override
//                public void resolved(MessagingAPI api, MsgResultAPI result) {
//                    System.out.println("IT WORKED!!! Message received from \"channel1\" and \"channel2\" on api");
//                    System.out.println(result.getMessage());
//                }
//
//                @Override
//                public void rejected(Exception e) {
//
//                }
//            }, "channel1", "channel2");
//
//            api.addEventListener(new ISubscribeCallback() {
//                @Override
//                public void status(MessagingAPI api, MsgStatus status) {
//
//                }
//
//                @Override
//                public void resolved(MessagingAPI api, MsgResultAPI result) {
//                    System.out.println("IT WORKED!!! Message received from channel2 on api");
//                    System.out.println(result.getMessage());
//                }
//
//                @Override
//                public void rejected(Exception e) {
//
//                }
//            }, "channel2");
//
//            api2.addEventListener(new ISubscribeCallback() {
//                @Override
//                public void status(MessagingAPI api, MsgStatus status) {
//
//                }
//
//                @Override
//                public void resolved(MessagingAPI api, MsgResultAPI result) {
//                    System.out.println("IT WORKED!!! Message received from channel3 on api2");
//                    System.out.println(result.getMessage());
//                }
//
//                @Override
//                public void rejected(Exception e) {
//
//                }
//            }, "channel3");
//
//            // sending message to a channel
//            api.publish().message(new TestClass("tim", "brown", new String[]{"one", "two", "three", "four"})).channel("channel1").execute();
//
//            // sending message to a channel
//            api.publish().message(new TestClass("johnny", "blond", new String[]{"one222222222222222222222", "two", "three", "four"})).channel("channel2").execute();
//
//            // sending message to a channel
//            api2.publish().message(new TestClass("kevin", "black", new String[]{"one333333333333333333333", "two", "three", "four"})).channel("channel3").execute();
//
//            System.out.println("this statement was actually invoked last!!");
//            Thread.sleep(5000);
//            api.free();
//            api2.free();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("end");
//            if (api != null)
//                api.free();
//            if (api2 != null)
//                api2.free();
//        }
//    }

    /**
     * No arg Constructor
     * @author Kord Boniadi
     */
    public MessagingAPI() {
        this(ThreadCount.FOUR);
    }

    /**
     * Constructor
     * @param count number of threads to use in pool
     * @author Kord Boniadi
     */
    public MessagingAPI(ThreadCount count) {
        server = Executors.newFixedThreadPool(count.toInt());
        try {
            mainSocket = new Socket(serverAddress, 9000);
            buffer = new IOWrapper.Builder()
                    .withWriter(new DataOutputStream(mainSocket.getOutputStream()))
                    .withReader(new DataInputStream(mainSocket.getInputStream()))
                    .build();
        } catch (Exception e) {
            LOGGER.info("Connection error.");
            LOGGER.info("freeing up resources...");
            LOGGER.info("Closed.");
            free();
        }

        new Thread(() -> {  // api listener thread
            try {
                String value;
                while (!exit) {
                    if ((value = buffer.readLine()) != null) {
                        JSONObject newJson = new JSONObject(value);
                        EventManager.getInstance().publish(this, newJson.remove("uuid").toString(), newJson.remove("channels").toString(), newJson.toString());
                    } else {
                        System.out.println("Server returned null, something is wrong");
                        throw new IOException("Server may have crashed");
                    }
                }
            } catch (IOException e) {
                LOGGER.info(Objects.requireNonNullElse(e.getMessage(), "Server may have crashed"));
            } finally {
                LOGGER.info("freeing up resources...");
                LOGGER.info("Closed.");
                free();
            }

        }).start();
    }

    /*===============================START OF HELPER METHODS====================================================*/

//    /**
//     * @param json object containing data needed for DB query
//     * @return Future event that can be handled at a later time
//     * @throws IOException from socket connection
//     * @author Kord Boniadi
//     */
//    private CompletableFuture<String> getStringCompletableFuture(JSONObject json) throws IOException {
//        Socket socket = new Socket(serverAddress, 9000);
//        var buffer = new BufferWrapper.Builder()
//                .withWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)))
//                .withReader(new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
//                .build();
//
//        return CompletableFuture.supplyAsync(() -> {
//            buffer.writeLine(json.toString());
//
//            try {
//                String value;
//                if ((value = buffer.readLine()) != null)
//                    return value;
//                throw new Exception("Server returned null");
//            } catch (IOException e) {
//                System.out.println("There may have been a lose of connection");
//                e.printStackTrace();
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            } finally {
//                try {
//                    buffer.close();
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
//        }, server);
//    }

    /*===============================END OF HELPER METHODS====================================================*/

    /**
     * Wrapper around EventManager method
     * @param callback  callback that receives data
     * @param channels  channels broadcast on
     * @author Kord Boniadi
     */
    public void addEventListener(@NonNull ISubscribeCallback callback, @NonNull String... channels) {
        EventManager.getInstance().addEventListener(this, callback, channels);
    }

    /**
     * Wrapper around EventManager method
     * @param callback  callback that receives data
     * @param channels  channels broadcast on
     * @author Kord Boniadi
     */
    public void removeEventListener(@NonNull ISubscribeCallback callback, @NonNull String... channels) {
        try {
            EventManager.getInstance().removeEventListener(this, callback, channels);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public SubscribeChain subscribe() {
        return new SubscribeChain("Subscribe", this.buffer);
    }

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public SubscribeChain unsubscribe() {
        return new SubscribeChain("Unsubscribe", this.buffer);
    }

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public PublishChain publish() {
        return new PublishChain(getUuid(), "Message", this.buffer);
    }

    public void onclose(CloseCallback cc) {
        this.cc = cc;
    }

    /**
     * Free's up api allocated resources
     * @author Kord Boniadi
     */
    public void free() {
        try {
            exit = true;
            server.shutdown();
            EventManager.getInstance().cleanup();
            buffer.close();
            mainSocket.close();
            this.cc.action();
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        try {
            exit = true;
            server.shutdown();
            EventManager.getInstance().cleanup();
            buffer.close();
            mainSocket.close();
            this.cc.action();
        } catch (SecurityException | IOException e) {
            throw new Exception("Something went wrong in -> { io.github.API.proj.MessageAPI.class }");
        }
    }
}
