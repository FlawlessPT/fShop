package pt.flawless.fShop.listeners;

import java.util.Optional;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fShop.shops.ShopActions;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;
import pt.flawless.fShop.shops.ShopLookup;
import pt.flawless.fShop.shops.ShopOffer;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

public class SignClickListener implements Listener {
    private final ShopActions actions;

    private final ShopItems items;

    public SignClickListener(ShopActions actions, ShopItems items) {
        this.actions = actions;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClickEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        Player player = e.getPlayer();

        // A SignShop can only be destroyed while sneaking
        if (player.isSneaking()) return;

        Block block = e.getClickedBlock();

        if (block == null || !(block.getState() instanceof Sign sign)) return;

        SignShopImpl signShop = new SignShopImpl(new SignShop(sign, player));

        try {
            if (!signShop.isSignShop()) return;
            e.setCancelled(true);

            if (!signShop.hasItem()) {
                if (action == Action.RIGHT_CLICK_BLOCK && signShop.canEdit()) {
                    ItemStack inHand = player.getItemInHand();
                    if (signShop.insertItemInHand(inHand)) {
                        items.put(ShopKey.of(block), inHand);
                    }
                }
                return;
            }

            if (!signShop.isServerShop()) {
                player.sendMessage("§cLojas de jogador ainda não estão disponíveis.");
                return;
            }

            Optional<ShopOffer> offer = signShop.offer();
            if (offer.isEmpty()) {
                FSound.fail(player);
                player.sendMessage("§cEsta loja está mal configurada.");
                return;
            }

            boolean buying = action == Action.RIGHT_CLICK_BLOCK;
            ShopOffer shopOffer = offer.get();
            ItemStack item = ShopLookup.itemFor(items, ShopKey.of(block), shopOffer);
            actions.execute(player, shopOffer, item, buying);
        } catch (Exception exception) {
            FConsoleLogger.sendConsoleMessage(String.valueOf(exception), EMessageTypeEnum.ERROR);
            e.setCancelled(true);
        }
    }
}
