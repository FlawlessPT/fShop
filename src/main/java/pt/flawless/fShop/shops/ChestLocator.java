package pt.flawless.fShop.shops;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;

/**
 * Liga uma placa ao seu baú. Só conta uma placa de parede ({@code WALL_SIGNS}) presa à face de um baú; o bloco de
 * suporte lê-se da própria placa a cada chamada, para a loja nunca mexer num baú que não é o dela.
 */
public final class ChestLocator {

    private ChestLocator() {
    }

    /** O inventário completo (também de baú duplo) do baú a que a placa está presa; vazio se não houver. */
    public static Optional<Inventory> chestFor(Block signBlock) {
        if (signBlock == null || !Tag.WALL_SIGNS.isTagged(signBlock.getType())) {
            return Optional.empty();
        }
        if (!(signBlock.getBlockData() instanceof WallSign ws)) {
            return Optional.empty();
        }
        Block support = signBlock.getRelative(ws.getFacing().getOppositeFace());
        if (support.getType() != Material.CHEST) {
            return Optional.empty();
        }
        return support.getState() instanceof Chest chest ? Optional.of(chest.getInventory()) : Optional.empty();
    }
}
