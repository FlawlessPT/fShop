package pt.flawless.fShop.shops;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Inventário principal (36 espaços, sem armadura) de um jogador. O item da loja é um modelo: dar entrega cópias
 * dele e contar/retirar usa {@link ItemMatcher} (encantamentos, nome e data value contam; durabilidade e
 * quantidade não).
 */
public final class PlayerBag implements ShopTrade.Bag {

    private static final int STORAGE_SLOTS = 36;

    private final Player player;

    public PlayerBag(Player player) {
        this.player = player;
    }

    /** O empilhamento usa {@code isSimilar} estrito, como o {@code addItem}: aqui não se pode ser tolerante. */
    @Override
    public boolean canHold(ItemStack template, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int max = template.getMaxStackSize();
        int free = 0;
        for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                free += max;
            } else if (template.isSimilar(item)) {
                free += Math.max(0, max - item.getAmount());
            }
        }
        return free >= quantity;
    }

    @Override
    public boolean has(ItemStack template, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int found = 0;
        for (int slot = 0; slot < STORAGE_SLOTS; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (ItemMatcher.matches(template, item)) {
                found += item.getAmount();
            }
        }
        return found >= quantity;
    }

    @Override
    public void give(ItemStack template, int quantity) {
        ItemStack stack = template.clone();
        stack.setAmount(quantity);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.updateInventory();
    }

    @Override
    public void take(ItemStack template, int quantity) {
        PlayerInventory inventory = player.getInventory();
        int remaining = quantity;
        for (int slot = 0; slot < STORAGE_SLOTS && remaining > 0; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!ItemMatcher.matches(template, item)) {
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
