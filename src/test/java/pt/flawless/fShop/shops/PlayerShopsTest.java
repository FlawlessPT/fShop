package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerShopsTest {

    private static final ShopKey KEY = new ShopKey("world", 1, 64, 2);
    private static final ShopOwner OWNER = new ShopOwner(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Dono");

    static class FakeRepository implements PlayerShopRepository {
        final Map<ShopKey, ShopOwner> stored = new HashMap<>();
        final List<String> calls = new ArrayList<>();
        CompletableFuture<Void> saveResult = CompletableFuture.completedFuture(null);

        @Override
        public CompletableFuture<Map<ShopKey, ShopOwner>> loadAll() {
            return CompletableFuture.completedFuture(new HashMap<>(stored));
        }

        @Override
        public CompletableFuture<Void> save(ShopKey key, ShopOwner owner) {
            calls.add("save");
            stored.put(key, owner);
            return saveResult;
        }

        @Override
        public CompletableFuture<Void> delete(ShopKey key) {
            calls.add("delete");
            stored.remove(key);
            return CompletableFuture.completedFuture(null);
        }
    }

    private FakeRepository repository;
    private List<Throwable> errors;
    private PlayerShops shops;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        errors = new ArrayList<>();
        shops = new PlayerShops(repository, errors::add);
    }

    @Test
    void load_fillsTheCache() throws Exception {
        repository.stored.put(KEY, OWNER);
        shops.load();
        assertEquals(OWNER, shops.get(KEY).orElseThrow());
    }

    @Test
    void get_unknownShopIsEmpty() {
        assertTrue(shops.get(KEY).isEmpty());
    }

    @Test
    void put_storesAndPersists() {
        shops.put(KEY, OWNER);
        assertEquals(OWNER, shops.get(KEY).orElseThrow());
        assertEquals(List.of("save"), repository.calls);
    }

    @Test
    void put_whenSaveFails_isLoggedAndTheShopStillWorks() {
        repository.saveResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        shops.put(KEY, OWNER);
        assertEquals(1, errors.size());
        assertTrue(shops.get(KEY).isPresent());
    }

    @Test
    void remove_deletesWhenThereWasARecord() {
        shops.put(KEY, OWNER);
        shops.remove(KEY);
        assertTrue(shops.get(KEY).isEmpty());
        assertEquals(List.of("save", "delete"), repository.calls);
    }

    @Test
    void remove_whenThereWasNoRecord_doesNotTouchTheDatabase() {
        shops.remove(KEY);
        assertFalse(repository.calls.contains("delete"));
    }
}
