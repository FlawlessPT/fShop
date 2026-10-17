package pt.flawless.fShop;

import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.SqlConsumer;
import pt.flawless.fapi.database.SqlFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** DatabaseService de teste: uma ligação por chamada, 4 threads, falhas com a excecao original. */
public final class TestDatabaseService implements DatabaseService, AutoCloseable {

    private final String url;
    private final String user;
    private final String password;
    private final ExecutorService executor;

    public TestDatabaseService(String host, String port, String name, String user, String password) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?sslMode=PREFERRED";
        this.user = user;
        this.password = password;
        AtomicInteger counter = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(4, runnable -> {
            Thread thread = new Thread(runnable, "fshop-test-db-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(SqlFunction<Connection, T> work) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.execute(() -> {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                future.complete(work.apply(connection));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Void> runAsync(SqlConsumer<Connection> work) {
        return supplyAsync(connection -> {
            work.accept(connection);
            return null;
        });
    }

    @Override
    public void migrate(Plugin owner, List<String> resources) throws SQLException {
        throw new UnsupportedOperationException("migrate nao e usado nos testes");
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
