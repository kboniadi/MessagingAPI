package io.github.API.proj;

import io.github.API.proj.utils.BufferWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagingAPI extends AbstractEventManager implements AutoCloseable, IChannel, IChannels, IMessage, IExecute {
    private final String serverAddress = "localhost";     // IP For remote Server
    private final ExecutorService server;                   // Thread pool for DB calls
    private final Socket mainSocket;                        // Persistent Server connection for api
    private final BufferWrapper buffer;                     // read/write wrapper for tcp throughput
    private JSONObject jsonBuffer;                          // json buffer for sending messages


    public static void main(String[] args) {
        try {
            MessagingAPI api = new MessagingAPI(ThreadCount.FOUR);
            MessagingAPI api2 = new MessagingAPI(ThreadCount.SYS_DEP);

            // TESTING - GRANT
            api.updateFirstName("james", "new james").thenAccept((json) -> {
                System.out.println("updated first name of james: " + json);
            });

            api.subscribe().channels("channel1", "channel2").execute();     // register api to listen to a channel
            api2.subscribe().channels("channel2").execute();                // register api to listen to a channel

            api.addEventListener((apiRef, json) -> {            // callback event listener
                System.out.println("IT WORKED!!! Message received from \"channel1\" and \"channel2\" on api");
                System.out.println(json);
            }, "channel1", "channel2");

            api.addEventListener((apiRef, json) -> {            // callback event listener
                System.out.println("IT WORKED!!! Message received from channel2 on api");
                System.out.println(json);
            }, "channel2");

            api2.addEventListener((apiRef, json) -> {           // callback event listener
                System.out.println("IT WORKED!!! Message received from channel2 on api2");
                System.out.println(json);
            }, "channel2");

            api2.addEventListener((apiRef, json) -> {           // callback event listener
                System.out.println("This should not work!!! Message received from channel3 on api2");
                System.out.println(json);
            }, "channel3");

            // sending message to a channel
            api.publish().channel("channel1").message(api.addJsonType("{}", "Message")
                    .put("name", "tim")
                    .put("haircolor", "brown").put("utsav", new JSONArray(Arrays.stream(new String[]{"one", "two", "three", "four"}).toArray())).toString()).execute();

            // sending message to a channel
            api.publish().channel("channel2").message(api.addJsonType("{}", "Message")
                    .put("name", "johnny")
                    .put("haircolor", "blond").put("utsav", new JSONArray(Arrays.stream(new String[]{"one222222222222222222222", "two", "three", "four"}).toArray())).toString()).execute();

            // sending message to a channel
            api2.publish().channel("channel3").message(api.addJsonType("{}", "Message")
                    .put("name", "kevin")
                    .put("haircolor", "black").put("utsav", new JSONArray(Arrays.stream(new String[]{"one333333333333333333333", "two", "three", "four"}).toArray())).toString()).execute();

            System.out.println("this statement was actually invoked last!!");
        } catch (Exception e) {
            System.out.println("end");
        }
    }

    /**
     * No arg Constructor
     * @throws IOException from socket connection
     * @author Kord Boniadi
     */
    public MessagingAPI() throws IOException {
        this(ThreadCount.FOUR);
    }

    /**
     * Constructor
     * @param count number of threads to use in pool
     * @throws IOException from socket connection
     * @author Kord Boniadi
     */
    public MessagingAPI(ThreadCount count) throws IOException {
        server = Executors.newFixedThreadPool(count.toInt());
        mainSocket = new Socket(serverAddress, 9000);
        buffer = new BufferWrapper.Builder()
                .withWriter(new BufferedWriter(new OutputStreamWriter(mainSocket.getOutputStream(), StandardCharsets.UTF_8)))
                .withReader(new BufferedReader(new InputStreamReader(mainSocket.getInputStream(), StandardCharsets.UTF_8)))
                .build();

        new Thread(() -> {  // api listener thread
            try {
                String value;
                while (true) {
                    if ((value = buffer.readLine()) != null) {
                        JSONObject newJson = new JSONObject(value);
                        publish(this, newJson.remove("channels").toString(), newJson.toString());
                    } else {
                        System.out.println("Server returned null, something is wrong");
                        throw new IOException("Server may have crashed");
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            
        }).start();
    }

    /*===============================START OF HELPER METHODS====================================================*/

    /**
     * @param json object containing data needed for DB query
     * @return Future event that can be handled at a later time
     * @throws IOException from socket connection
     * @author Kord Boniadi
     */
    private CompletableFuture<String> getStringCompletableFuture(JSONObject json) throws IOException {
        Socket socket = new Socket(serverAddress, 9000);
        var buffer = new BufferWrapper.Builder()
                .withWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)))
                .withReader(new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
                .build();

        return CompletableFuture.supplyAsync(() -> {
            buffer.writeLine(json.toString());

            try {
                String value;
                if ((value = buffer.readLine()) != null)
                    return value;
                throw new Exception("Server returned null");
            } catch (IOException e) {
                System.out.println("There may have been a lose of connection");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    buffer.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }, server);
    }

    /**
     * @param json unmodified json String
     * @param typeValue value of "type" key in json
     * @return modded JSONObject
     * @author Kord Boniadi
     */
    public JSONObject addJsonType(String json, Object typeValue) {
        return new JSONObject(json).put("type", typeValue);
    }

    /*===============================END OF HELPER METHODS====================================================*/

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public IChannels subscribe() {
        jsonBuffer = addJsonType("{}", "Subscribe");
        return this;
    }

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public IChannels unsubscribe() {
        jsonBuffer = addJsonType("{}", "Unsubscribe");
        return this;
    }

    /**
     * @return instance of api class
     * @author Kord Boniadi
     */
    public IChannel publish() {
        jsonBuffer = addJsonType("{}", "Message");
        return this;
    }

    /**
     * @param userName userName of player to look up
     * @return Future event that resolving to a json string
     * @throws IOException from socket connection
     * @author Kord Boniadi
     */
    public CompletableFuture<String> getPlayerInfo(String userName) throws IOException {
        return getStringCompletableFuture(
                addJsonType("{}", "PlayerInfo")
                .put("username", userName)      // KEYS NEED TO BE ALL LOWERCASE
        );
    }

    /**
     * @param firstName first name
     * @param lastName last name
     * @param userName userName
     * @param password password
     * @return Future event resolving to a json String
     * @throws IOException from socket connection
     * @author Kord Boniadi
     */
    public CompletableFuture<String> createAccount(String userName, String password, String firstName, String lastName) throws IOException { // returns json containing { "isSuccess: "true | false" }
        return getStringCompletableFuture(
                addJsonType("{}", "CreateAccount")
                .put("username", userName)              // KEYS NEED TO BE ALL LOWERCASE
                .put("password", password)              // KEYS NEED TO BE ALL LOWERCASE
                .put("firstname", firstName)            // KEYS NEED TO BE ALL LOWERCASE
                .put("lastname", lastName)              // KEYS NEED TO BE ALL LOWERCASE
        );
    }


//    public CompletableFuture<String> getGameHistoryInfo(String userName) {
//
//    }
//
//

    /**
     * Creates a JSON future that encapsulates a delete account message. The
     * message contains the username of the account to be marked as deleted.
     * <p>
     * Account deletion is soft; i.e. the account persists in
     * the database and its {@code isDeleted} flag is set to {@code true}
     * </p>
     * @param userName user name of the account to flag as deleted
     * @return Future event involving a JSON string (NULL for now)
     * @throws IOException from socket connection
     * @author Grant Goldsworth
     */
    public CompletableFuture<String> deleteAccount(String userName) throws IOException {
        return getStringCompletableFuture(
                addJsonType("{}", "DeleteAccount")
                .put("username",userName)
        );
    }
//
//    public CompletableFuture<String> verifyPassword(String userName, String password) { // returns json containing { "isSuccess: "true | false" }
//        // utsav
//    }
//

    /**
     * Called from the api, this method update's the user's username - the username must be unique.
     * @param oldUserName The user's original username which is being updated.
     * @param newUserName The user's new username - has to be unique
     * @return A completable future as a string (will contain either "true" or "false")
     * @throws IOException from socket connection
     * @author Joey Campbell
     */
    public CompletableFuture<String> updateUserName(String oldUserName, String newUserName) throws IOException { // returns json containing { "isSuccess: "true | false" }
        return getStringCompletableFuture(addJsonType("{}", "UpdateUserName")
                .put("oldusername", oldUserName)
                .put("newusername", newUserName)
        );
    }
//
//    public CompletableFuture<String> updatePassword(String userName, String password) { // returns json containing { "isSuccess: "true | false" }
//        // utsav
//    }
//

    /**
     * Creates a JSOn future object that encapsulates a first name update message.
     * @param userName user account name
     * @param firstName new name to use
     * @return a future event JSON string
     * @throws IOException from socket connection
     * @author Grant Goldsworth
     */
    public CompletableFuture<String> updateFirstName(String userName, String firstName) throws IOException { // returns json containing { "isSuccess: "true | false" }
        return getStringCompletableFuture(
                addJsonType("{}", "UpdateFirstName")
                .put("username", userName)
                .put("firstname", firstName)
        );
    }
//

    /**
     * Called from the api, this method updates the user's last name.
     * @param userName The username of the user who is switching their last name
     * @param lastName The new last name for the user
     * @return A completable future as a string (will contain either "true" or "false")
     * @throws IOException from socket connection
     * @author Joey Campbell
     */
    public CompletableFuture<String> updateLastName(String userName, String lastName) throws IOException { // returns json containing { "isSuccess: "true | false" }
        // joey
        return getStringCompletableFuture(
                addJsonType("{}", "UpdateLastName")
                .put("username", userName)
                .put("lastname", lastName)
        );
    }

    /**
     * Free's up api allocated resources
     * @author Kord Boniadi
     */
    public void freeResources() {
        try {
            server.shutdown();
            mainSocket.close();
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
            server.shutdown();
            mainSocket.close();
        } catch (SecurityException | IOException e) {
            throw new Exception("Something went wrong in -> { io.github.API.proj.TTTDataBaseAPI.class }");
        }
    }

    /**
     * @param channels array of channels
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public IExecute channels(String... channels) {
        jsonBuffer.put("channels", new JSONArray(Arrays.stream(
                channels)
                .toArray()));
        return this;
    }

    /**
     * @param channel channel name
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public IMessage channel(String channel) {
        jsonBuffer.put("channels", channel);
        return this;
    }

    /**
     * @param json message in json form
     * @return instance of api class
     * @author Kord Boniadi
     */
    @Override
    public IExecute message(String json) {
        JSONObject temp = new JSONObject(json);
        var iterator = this.jsonBuffer.keys();
        iterator.forEachRemaining(key -> {
            temp.put(key, this.jsonBuffer.get(key));
        });

        this.jsonBuffer = temp;
        return this;
    }

    /**
     * writes the built data to the server
     * @author Kord Boniadi
     */
    @Override
    public void execute() {
        buffer.writeLine(this.jsonBuffer.toString());
    }
}
