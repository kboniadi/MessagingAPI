import org.json.JSONObject;

import utils.BufferWrapper;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TTTDataBaseAPI implements AutoCloseable {
    private static final String serverAddress = "45.57.226.7";
    private ExecutorService server;
    private Socket socket;
    public BufferWrapper buffer;

    public static void main(String[] args) {
        try {
            TTTDataBaseAPI api = new TTTDataBaseAPI();

            api.getPlayerInfo("kord", "nothing").thenAccept((event) -> {
                System.out.println(event);
            });

            System.out.println("this statement was actually invoked after the db call!!");
        } catch (Exception e) {
            System.out.println("end");
        }
        
        
    }

    public TTTDataBaseAPI() throws Exception {
        this(ThreadCount.FOUR);
    }

    public TTTDataBaseAPI(ThreadCount count) throws Exception {
        server = Executors.newFixedThreadPool(count.toInt());
        socket = new Socket(serverAddress, 9000);
        buffer = new BufferWrapper.Builder()
                .withWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)))
                .withReader(new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)))
                .build();
    }

    public CompletableFuture<String> getPlayerInfo(String name, String typeOf) {
        JSONObject json = new JSONObject();
        json.put("type", "PlayerInfo");
        json.put("name", name);
        json.put("typeOf", typeOf);
        return CompletableFuture.supplyAsync(() -> {
            buffer.writeLine(json.toString());

            String value;

            try {
                while ((value = buffer.readLine()) != null) {
                    return value;
                }
                throw new IOException("Server returned null");
            } catch (IOException e) {
                System.out.println("There may have been a lose of connection");
                e.printStackTrace();
            }
            return null;
        }, server);
    }
    
    public CompletableFuture<Boolean> createAccount(String firstName, String lastName, String userName, String password) {
        JSONObject json = new JSONObject();
        json.put("type", "CreateAccount");
        json.put("firstname", firstName);
        json.put("lastname", lastName);
        json.put("username", userName);
        json.put("password", password);
        return CompletableFuture.supplyAsync(() -> {
            buffer.writeLine(json.toString());

            String value;

            try {
                while ((value = buffer.readLine()) != null) {
                    return value;
                }
                throw new IOException("Server returned null");
            } catch (IOException e) {
                System.out.println("There may have been a lose of connection");
                e.printStackTrace();
            }
            return null;
        }, server);
    }
//    public CompletableFuture<String> getGameInfo(String gameName, String... typeOf) {
//
//    }
//
//    public void playerSignUp(String userName, String password, String firstName, String lastName) {
//
//    }
//
//    public void playerSignUp(String json) {
//
//    }
//
//    public CompletableFuture<Boolean> playerSignIn(String userName, String password) {
//
//    }
//
//    public CompletableFuture<Boolean> deleteAccount(String userName, String password) {
//
//    }

    public void freeThreadPool() {
        try {
            server.shutdown();
            socket.close();
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
            socket.close();
        } catch (SecurityException | IOException e) {
            throw new Exception("Something went wrong in -> { TTTDataBaseAPI.class }");
        }
    }

//    public void createTable(String tableName, String json) {
//
//    }
//
//    public CompletableFuture<String> get(String tableName, String column, String... options) {
//
//    }
//
//    public boolean put(String tableName, String json, String column) {
//
//    }
//
//    public void post(String tableName, String json, String column) {
//
//    }
//
//    public void delete(String tableName, String json, String... options) {
//
//    }
}
