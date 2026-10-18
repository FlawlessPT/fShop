package pt.flawless.fShop.shops;

import java.util.Collection;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Os primeiros {@code slots} espaços de um {@link Inventory}. O item da loja é um modelo: dar entrega cópias dele e
 * contar/retirar usa {@link ItemMatcher} (encantamentos, nome e data value contam; durabilidade e quantidade não).
 */
public class InventoryBag implements ShopTrade.Bag {

    private final Inventory inventory;
    private final int slots;

    public InventoryBag(Inventory inventory, int slots) {
        this.inventory = inventory;
        this.slots = slots;
    }

    /** O empilhamento usa {@code isSimilar} estrito, como o {@code addItem}: aqui não se pode ser tolerante. */
    @Override
    public boolean canHold(ItemStack template, int quantity) {
        int max = template.getMaxStackSize();
        int free = 0;
        for (int slot = 0; slot < slots; slot++) {
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
        int found = 0;
        for (int slot = 0; slot < slots; slot++) {
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
        Map<Integer, ItemStack> leftovers = inventory.addItem(stack);
        if (!leftovers.isEmpty()) {
            leftovers(leftovers.values());
        }
        changed();
    }

    @Override
    public void take(ItemStack template, int quantity) {
        int remaining = quantity;
        for (int slot = 0; slot < slots && remaining > 0; slot++) {
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
        changed();
    }

    /** O que não coube ao dar. Por omissão é um erro (num baú nunca devia acontecer: {@code canHold} corre antes). */
    protected void leftovers(Collection<ItemStack> items) {
        throw new IllegalStateException("sem espaço para guardar " + items.size() + " pilha(s)");
    }

    /** Chamado depois de dar ou retirar. */
    protected void changed() {
    }
}
