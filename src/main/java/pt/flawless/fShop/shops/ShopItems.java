package pt.flawless.fShop.shops;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;

/**
 * Item-modelo de cada loja do servidor. Os cliques (thread principal) leem só da cache em memória; as escritas na
 * base de dados são assíncronas e, se falharem, são registadas e a cache mantém-se (a loja funciona até ao reinício).
 * O que sai e entra é sempre uma cópia, com quantidade 1.
 */
public final class ShopItems {

    private final Map<ShopKey, ItemStack> cache = new ConcurrentHashMap<>();
    private final ShopItemRepository repository;
    private final Consumer<Throwable> onError;

    public ShopItems(ShopItemRepository repository, Consumer<Throwable> onError) {
        this.repository = repository;
        this.onError = onError;
    }

    /** Carrega tudo da base de dados. Bloqueia: chamar só no {@code onEnable}. */
    public void load() throws Exception {
        cache.putAll(repository.loadAll().get(30, TimeUnit.SECONDS));
    }

    public Optional<ItemStack> get(ShopKey key) {
        ItemStack item = cache.get(key);
        return item == null ? Optional.empty() : Optional.of(item.clone());
    }

    public void put(ShopKey key, ItemStack item) {
        ItemStack copy = item.clone();
        copy.setAmount(1);
        cache.put(key, copy);
        repository.save(key, copy).exceptionally(this::logged);
    }

    /** Só toca na base de dados se a loja tinha item guardado. */
    public void remove(ShopKey key) {
        if (cache.remove(key) != null) {
            repository.delete(key).exceptionally(this::logged);
        }
    }

    private Void logged(Throwable error) {
        onError.accept(error);
        return null;
    }
}
