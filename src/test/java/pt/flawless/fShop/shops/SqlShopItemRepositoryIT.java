package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fShop.TestDatabaseService;
import pt.flawless.fShop.TestDb;
import pt.flawless.fShop.TestServer;

class SqlShopItemRepositoryIT {

    private static final ShopKey KEY = new ShopKey("world", 10, 64, -3);

    private TestDatabaseService db;
    private SqlShopItemRepository repository;

    @BeforeAll
    static void server() {
        TestServer.install();
    }

    @BeforeEach
    void setUp() throws Exception {
        db = TestDb.service();
        TestDb.resetSchema(db);
        repository = new SqlShopItemRepository(db);
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

    private static ItemStack named(String name) {
        ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    @Test
    void saveThenLoadAll_roundTrips() throws Exception {
        await(repository.save(KEY, named("Excalibur")));

        Map<ShopKey, ItemStack> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertEquals("Excalibur", loaded.get(KEY).getItemMeta().getDisplayName());
    }

    @Test
    void save_sameKeyTwice_overwrites() throws Exception {
        await(repository.save(KEY, named("Primeira")));
        await(repository.save(KEY, named("Segunda")));

        Map<ShopKey, ItemStack> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertEquals("Segunda", loaded.get(KEY).getItemMeta().getDisplayName());
    }

    @Test
    void delete_removesTheRow() throws Exception {
        await(repository.save(KEY, named("Excalibur")));
        await(repository.delete(KEY));
        assertTrue(await(repository.loadAll()).isEmpty());
    }

    @Test
    void loadAll_skipsRowsThatCannotBeDecoded() throws Exception {
        await(repository.save(KEY, named("Excalibur")));
        await(db.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO fshop_items (world, x, y, z, item) VALUES ('world', 1, 2, 3, 'lixo')")) {
                ps.executeUpdate();
            }
        }));

        Map<ShopKey, ItemStack> loaded = await(repository.loadAll());
        assertEquals(1, loaded.size());
        assertTrue(loaded.containsKey(KEY));
    }
}
