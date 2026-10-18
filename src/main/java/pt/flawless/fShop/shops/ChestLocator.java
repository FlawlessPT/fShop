package pt.flawless.fShop.shops;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

/**
 * Liga uma placa ao seu baú. Só conta uma placa de parede ({@code WALL_SIGN}) presa à face de um baú; o bloco de
 * suporte lê-se da própria placa a cada chamada, para a loja nunca mexer num baú que não é o dela.
 */
public final class ChestLocator {

    private ChestLocator() {
    }

    /** O inventário completo (também de baú duplo) do baú a que a placa está presa; vazio se não houver. */
    public static Optional<Inventory> chestFor(Block signBlock) {
        if (signBlock == null || signBlock.getType() != Material.WALL_SIGN) {
            return Optional.empty();
        }
        if (!(signBlock.getState().getData() instanceof org.bukkit.material.Sign data)) {
            return Optional.empty();
        }
        Block support = signBlock.getRelative(data.getAttachedFace());
        if (support.getType() != Material.CHEST) {
            return Optional.empty();
        }
        return support.getState() instanceof Chest chest ? Optional.of(chest.getInventory()) : Optional.empty();
    }
}
