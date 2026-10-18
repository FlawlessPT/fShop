package pt.flawless.fShop.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pt.flawless.fShop.managers.SignShopManager;
import pt.flawless.fapi.sounds.FSound;

/** Só o dono parte a sua loja de jogador; os admins ({@code fshop.admin}) partem tudo; lojas do servidor só os admins. */
public class ShopProtectListener implements Listener {

    /** Regra de quem pode partir uma loja. {@code owner} só conta para lojas de jogador. */
    static boolean canBreak(boolean admin, boolean serverShop, boolean owner) {
        return admin || (!serverShop && owner);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();
        if (type != Material.SIGN_POST && type != Material.WALL_SIGN) return;
        if (!(block.getState() instanceof Sign sign)) return;

        Player player = e.getPlayer();
        SignShopManager shop = new SignShopManager(sign, player);
        if (!shop.isShop()) return;

        if (canBreak(player.hasPermission("fshop.admin"), shop.isServerShop(), shop.canEdit())) return;

        e.setCancelled(true);
        FSound.fail(player);
        player.sendMessage("§cNão podes partir esta loja.");
    }
}
