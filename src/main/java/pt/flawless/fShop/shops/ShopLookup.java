package pt.flawless.fShop.shops;

import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Resolve uma placa numa loja pronta a trocar. */
public final class ShopLookup {

    public record Ready(ShopOffer offer, ItemStack item) {
    }

    private ShopLookup() {
    }

    /** O item guardado, se for do mesmo tipo que a placa diz; senão o item simples (placas antigas). */
    public static ItemStack itemFor(ShopItems items, ShopKey key, ShopOffer offer) {
        return items.get(key)
                .filter(stored -> stored.getType() == offer.material())
                .orElseGet(() -> new ItemStack(offer.material()));
    }

    /** Loja do servidor completa (item definido e oferta válida) neste bloco; vazio caso contrário. */
    public static Optional<Ready> ready(Block block, Player player, ShopItems items) {
        if (block == null || !(block.getState() instanceof Sign sign)) {
            return Optional.empty();
        }
        SignShopImpl shop = new SignShopImpl(new SignShop(sign, player));
        if (!shop.isSignShop() || !shop.isServerShop() || !shop.hasItem()) {
            return Optional.empty();
        }
        return shop.offer().map(offer -> new Ready(offer, itemFor(items, ShopKey.of(block), offer)));
    }
}
