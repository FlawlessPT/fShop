package pt.flawless.fShop.shops;

import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import pt.flawless.fapi.economy.EconomyApi;

/** Descobre a contraparte de uma placa: a loja do servidor, ou o dono e o baú de uma loja de jogador. */
public final class ShopSides {

    public record Resolution(ShopSide side, String error) {
        static Resolution of(ShopSide side) {
            return new Resolution(side, null);
        }

        static Resolution error(String message) {
            return new Resolution(null, message);
        }
    }

    private final EconomyApi economy;
    private final PlayerShops shops;

    public ShopSides(EconomyApi economy, PlayerShops shops) {
        this.economy = economy;
        this.shops = shops;
    }

    /** Lojas de jogador: o dono vem de {@code fshop_shops} e o baú lê-se do bloco de suporte da placa, a cada vez. */
    public Resolution resolve(Block signBlock, boolean serverShop) {
        if (serverShop) {
            return Resolution.of(new ServerSide(economy));
        }
        Optional<ShopOwner> owner = shops.get(ShopKey.of(signBlock));
        if (owner.isEmpty()) {
            return Resolution.error("§cEsta loja é antiga. Parte-a e cria-a de novo.");
        }
        Optional<Inventory> chest = ChestLocator.chestFor(signBlock);
        if (chest.isEmpty()) {
            return Resolution.error("§cEsta loja já não está presa a um baú.");
        }
        Inventory inventory = chest.get();
        return Resolution.of(new ChestSide(economy, owner.get().uuid(), owner.get().name(),
                new InventoryBag(inventory, inventory.getSize())));
    }
}
