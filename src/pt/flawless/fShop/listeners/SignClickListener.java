package pt.flawless.fShop.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pt.flawless.fShop.shops.SignShop;

public class SignClickListener implements Listener {
    @EventHandler
    public void onSignClickEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block block = e.getClickedBlock();

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
//            SignShop signShop = new SignShop(e, p);
//            p.sendMessage(p.getItemInHand().getEnchantments().toString());
//            signShop.insertItemName(p.getItemInHand().getType());
        }
    }
}

