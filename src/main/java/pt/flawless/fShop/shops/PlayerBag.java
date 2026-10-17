package pt.flawless.fShop.shops;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Inventário principal (36 espaços, sem armadura) de um jogador. Conta só o tipo de item, não o data value. */
public final class PlayerBag implements ShopTrade.Bag {

    private static final int STORAGE_SLOTS = 36;

    private final Player player;

    public PlayerBag(Player player) {
        this.player = player;
    }

    @Override
    public boolean canHold(Material material, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int max = material.getMaxStackSize();
        int free = 0;
        for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                free += max;
            } else if (item.getType() == material) {
                free += Math.max(0, max - item.getAmount());
            }
        }
        return free >= quantity;
    }

    @Override
    public boolean has(Material material, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int found = 0;
        for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() == material) {
                found += item.getAmount();
            }
        }
        return found >= quantity;
    }

    @Override
    public void give(Material material, int quantity) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(new ItemStack(material, quantity));
        leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.updateInventory();
    }

    @Override
    public void take(Material material, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int remaining = quantity;
        for (int slot = 0; slot < STORAGE_SLOTS && remaining > 0; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() != material) {
                continue;
            }
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                inventory.setItem(slot, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
        player.updateInventory();
    }
}
