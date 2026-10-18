package pt.flawless.fShop.shops;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Resolve uma placa numa loja pronta a trocar. */
public final class ShopLookup {

    public record Ready(ShopOffer offer, ItemStack item, ShopSide side) {
    }

    /** {@code ready} nulo e {@code error} nulo: não é uma loja pronta (sem aviso). {@code error}: mostrar ao jogador. */
    public record Resolution(Ready ready, String error) {
        static Resolution notReady() {
            return new Resolution(null, null);
        }
    }

    private ShopLookup() {
    }

    /** O item guardado, se for do mesmo tipo que a placa diz; senão o item simples (placas antigas). */
    public static ItemStack itemFor(ShopItems items, ShopKey key, ShopOffer offer) {
        return items.get(key)
                .filter(stored -> stored.getType() == offer.material())
                .orElseGet(() -> new ItemStack(offer.material()));
    }

    /** Loja completa (item definido, oferta válida e contraparte resolvida) neste bloco. */
    public static Resolution ready(Block block, Player player, ShopItems items, ShopSides sides) {
        if (block == null || !(block.getState() instanceof Sign sign)) {
            return Resolution.notReady();
        }
        SignShopImpl shop = new SignShopImpl(new SignShop(sign, player));
        if (!shop.isSignShop() || !shop.hasItem()) {
            return Resolution.notReady();
        }
        return shop.offer().map(offer -> {
            ShopSides.Resolution side = sides.resolve(block, shop.isServerShop());
            if (side.side() == null) {
                return new Resolution(null, side.error());
            }
            return new Resolution(
                    new Ready(offer, itemFor(items, ShopKey.of(block), offer), side.side()), null);
        }).orElseGet(Resolution::notReady);
    }
}
