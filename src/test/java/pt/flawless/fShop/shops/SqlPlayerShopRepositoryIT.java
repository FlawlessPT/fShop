package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fShop.TestDatabaseService;
import pt.flawless.fShop.TestDb;

class SqlPlayerShopRepositoryIT {

    private static final ShopKey KEY = new ShopKey("world", 10, 64, -3);
    private static final ShopOwner OWNER = new ShopOwner(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Dono");

    private TestDatabaseService db;
    private SqlPlayerShopRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDb.service();
        TestDb.resetSchema(db);
        repository = new SqlPlayerShopRepository(db);
    }

    @AfterEach
    void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    private static <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(30, TimeUnit.SECONDS);
    }

    @Test
    void saveThenLoadAll_roundTrips() throws Exception {
        await(repository.save(KEY, OWNER));
        Map<ShopKey, ShopOwner> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertEquals(OWNER, loaded.get(KEY));
    }

    @Test
    void save_sameKeyTwice_overwritesTheOwner() throws Exception {
        await(repository.save(KEY, OWNER));
        ShopOwner other = new ShopOwner(UUID.fromString("00000000-0000-0000-0000-000000000009"), "Outro");
        await(repository.save(KEY, other));
        Map<ShopKey, ShopOwner> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertEquals(other, loaded.get(KEY));
    }

    @Test
    void delete_removesTheRow() throws Exception {
        await(repository.save(KEY, OWNER));
        await(repository.delete(KEY));
        assertTrue(await(repository.loadAll()).isEmpty());
    }

    @Test
    void loadAll_skipsRowsWithAnInvalidUuid() throws Exception {
        await(repository.save(KEY, OWNER));
        await(db.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO fshop_shops (world, x, y, z, owner_uuid, owner_name) VALUES ('world', 1, 2, 3, 'lixo', 'X')")) {
                ps.executeUpdate();
            }
        }));
        Map<ShopKey, ShopOwner> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertTrue(loaded.containsKey(KEY));
    }
}
