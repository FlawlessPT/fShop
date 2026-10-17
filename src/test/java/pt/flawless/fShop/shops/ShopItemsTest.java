package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShopItemsTest {

    private static final ShopKey KEY = new ShopKey("world", 1, 64, 2);

    static class FakeRepository implements ShopItemRepository {
        final Map<ShopKey, ItemStack> stored = new HashMap<>();
        final List<String> calls = new ArrayList<>();
        CompletableFuture<Void> saveResult = CompletableFuture.completedFuture(null);

        @Override
        public CompletableFuture<Map<ShopKey, ItemStack>> loadAll() {
            return CompletableFuture.completedFuture(new HashMap<>(stored));
        }

        @Override
        public CompletableFuture<Void> save(ShopKey key, ItemStack item) {
            calls.add("save");
            stored.put(key, item);
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
    private ShopItems items;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        errors = new ArrayList<>();
        items = new ShopItems(repository, errors::add);
    }

    @Test
    void load_fillsTheCache() throws Exception {
        repository.stored.put(KEY, new ItemStack(Material.DIAMOND_SWORD));
        items.load();
        assertEquals(Material.DIAMOND_SWORD, items.get(KEY).orElseThrow().getType());
    }

    @Test
    void get_unknownShopIsEmpty() {
        assertTrue(items.get(KEY).isEmpty());
    }

    @Test
    void put_storesAnAmountOfOne_andPersists() {
        items.put(KEY, new ItemStack(Material.DIAMOND, 64));
        assertEquals(1, items.get(KEY).orElseThrow().getAmount());
        assertEquals(List.of("save"), repository.calls);
        assertEquals(1, repository.stored.get(KEY).getAmount());
    }

    @Test
    void put_copiesTheInput() {
        ItemStack original = new ItemStack(Material.DIAMOND);
        items.put(KEY, original);
        original.setType(Material.DIRT);
        assertEquals(Material.DIAMOND, items.get(KEY).orElseThrow().getType());
    }

    @Test
    void get_returnsACopy() {
        items.put(KEY, new ItemStack(Material.DIAMOND));
        items.get(KEY).orElseThrow().setAmount(40);
        assertEquals(1, items.get(KEY).orElseThrow().getAmount());
    }

    @Test
    void put_whenSaveFails_isLoggedAndShopStillWorks() {
        repository.saveResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        items.put(KEY, new ItemStack(Material.DIAMOND));
        assertEquals(1, errors.size());
        assertTrue(items.get(KEY).isPresent());
    }

    @Test
    void remove_deletesWhenThereWasARecord() {
        items.put(KEY, new ItemStack(Material.DIAMOND));
        items.remove(KEY);
        assertTrue(items.get(KEY).isEmpty());
        assertEquals(List.of("save", "delete"), repository.calls);
    }

    @Test
    void remove_whenThereWasNoRecord_doesNotTouchTheDatabase() {
        items.remove(KEY);
        assertFalse(repository.calls.contains("delete"));
    }
}
