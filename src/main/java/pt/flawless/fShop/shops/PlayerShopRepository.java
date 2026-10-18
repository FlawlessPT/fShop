package pt.flawless.fShop.shops;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Persistência assíncrona do dono de cada loja de jogador. Os futuros completam fora da thread principal. */
public interface PlayerShopRepository {

    CompletableFuture<Map<ShopKey, ShopOwner>> loadAll();

    CompletableFuture<Void> save(ShopKey key, ShopOwner owner);

    CompletableFuture<Void> delete(ShopKey key);
}
