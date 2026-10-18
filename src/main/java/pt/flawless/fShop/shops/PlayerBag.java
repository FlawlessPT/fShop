package pt.flawless.fShop.shops;

import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Inventário principal (36 espaços, sem armadura) de um jogador; o que não couber cai aos pés dele. */
public final class PlayerBag extends InventoryBag {

    private static final int STORAGE_SLOTS = 36;

    private final Player player;

    public PlayerBag(Player player) {
        super(player.getInventory(), STORAGE_SLOTS);
        this.player = player;
    }

    @Override
    protected void leftovers(Collection<ItemStack> items) {
        items.forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
    }

    @Override
    protected void changed() {
        player.updateInventory();
    }
}
