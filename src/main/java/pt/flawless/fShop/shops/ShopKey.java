package pt.flawless.fShop.shops;

import org.bukkit.Location;
import org.bukkit.block.Block;

/** Posição de uma placa de loja: nome do mundo e coordenadas do bloco. */
public record ShopKey(String world, int x, int y, int z) {

    public static ShopKey of(Location location) {
        return new ShopKey(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static ShopKey of(Block block) {
        return new ShopKey(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
}
