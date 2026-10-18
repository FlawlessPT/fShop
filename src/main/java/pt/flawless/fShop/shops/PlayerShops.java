package pt.flawless.fShop.shops;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * O dono de cada loja de jogador. Os cliques (thread principal) leem só da cache em memória; as escritas na base de
 * dados são assíncronas e, se falharem, são registadas e a cache mantém-se (a loja funciona até ao reinício).
 */
public final class PlayerShops {

    private final Map<ShopKey, ShopOwner> cache = new ConcurrentHashMap<>();
    private final PlayerShopRepository repository;
    private final Consumer<Throwable> onError;

    public PlayerShops(PlayerShopRepository repository, Consumer<Throwable> onError) {
        this.repository = repository;
        this.onError = onError;
    }

    /** Carrega tudo da base de dados. Bloqueia: chamar só no {@code onEnable}. */
    public void load() throws Exception {
        cache.putAll(repository.loadAll().get(30, TimeUnit.SECONDS));
    }

    public Optional<ShopOwner> get(ShopKey key) {
        return Optional.ofNullable(cache.get(key));
    }

    public void put(ShopKey key, ShopOwner owner) {
        cache.put(key, owner);
        repository.save(key, owner).exceptionally(this::logged);
    }

    /** Só toca na base de dados se a loja tinha registo. */
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
