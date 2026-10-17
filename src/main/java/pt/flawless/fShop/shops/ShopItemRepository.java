package pt.flawless.fShop.shops;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;

/** Persistência assíncrona do item-modelo de cada loja. Os futuros completam fora da thread principal. */
public interface ShopItemRepository {

    CompletableFuture<Map<ShopKey, ItemStack>> loadAll();

    CompletableFuture<Void> save(ShopKey key, ItemStack item);

    CompletableFuture<Void> delete(ShopKey key);
}
