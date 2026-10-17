package pt.flawless.fShop.shops;

import org.bukkit.inventory.ItemStack;

/** Decide se um item do jogador conta como o item de uma loja. */
public final class ItemMatcher {

    private ItemMatcher() {
    }

    /**
     * Mesmo tipo, data value e metadados (encantamentos, nome, lore). A quantidade nunca conta e, em itens com
     * durabilidade (ferramentas, armas, armaduras), a durabilidade também não: uma espada gasta vende-se a uma loja
     * de espada nova.
     */
    public static boolean matches(ItemStack template, ItemStack item) {
        if (item == null || item.getType() != template.getType()) {
            return false;
        }
        ItemStack probe = item.clone();
        if (template.getType().getMaxDurability() > 0) {
            probe.setDurability(template.getDurability());
        }
        return template.isSimilar(probe);
    }
}
