package pt.flawless.fShop.listeners;

import java.util.Optional;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fShop.shops.PlayerShops;
import pt.flawless.fShop.shops.ShopActions;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;
import pt.flawless.fShop.shops.ShopLookup;
import pt.flawless.fShop.shops.ShopMenu;
import pt.flawless.fShop.shops.ShopOffer;
import pt.flawless.fShop.shops.ShopSides;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

public class SignClickListener implements Listener {
    private final ShopActions actions;

    private final ShopItems items;

    private final PlayerShops shops;

    private final ShopSides sides;

    public SignClickListener(ShopActions actions, ShopItems items, PlayerShops shops, ShopSides sides) {
        this.actions = actions;
        this.items = items;
        this.shops = shops;
        this.sides = sides;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClickEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (!Tag.SIGNS.isTagged(type)) return;

        // Agachado: botão direito abre o menu da loja; botão esquerdo deixa partir a placa
        if (player.isSneaking()) {
            if (action == Action.RIGHT_CLICK_BLOCK) {
                ShopLookup.Resolution resolution = ShopLookup.ready(block, player, items, sides);
                if (resolution.ready() != null) {
                    ShopLookup.Ready ready = resolution.ready();
                    e.setCancelled(true);
                    player.openInventory(ShopMenu.create(ShopKey.of(block), ready.offer(), ready.item(),
                            actions::format, ready.side().title()));
                } else if (resolution.error() != null) {
                    e.setCancelled(true);
                    FSound.fail(player);
                    player.sendMessage(resolution.error());
                }
            }
            return;
        }

        if (!(block.getState() instanceof Sign sign)) return;

        SignShopImpl signShop = new SignShopImpl(new SignShop(sign, player));

        try {
            if (!signShop.isSignShop()) return;
            e.setCancelled(true);
            if (action == Action.LEFT_CLICK_BLOCK) ShopProtectListener.resync(block, player);

            boolean serverShop = signShop.isServerShop();
            if (!serverShop && shops.get(ShopKey.of(block)).isEmpty()) {
                FSound.fail(player);
                player.sendMessage("§cEsta loja é antiga. Parte-a e cria-a de novo.");
                return;
            }

            if (!signShop.hasItem()) {
                boolean mayEdit = serverShop
                        ? signShop.canEdit()
                        : shops.get(ShopKey.of(block)).map(o -> o.uuid().equals(player.getUniqueId())).orElse(false);
                if (action == Action.RIGHT_CLICK_BLOCK && mayEdit) {
                    ItemStack inHand = player.getInventory().getItemInMainHand();
                    if (signShop.insertItemInHand(inHand)) {
                        items.put(ShopKey.of(block), inHand);
                    }
                }
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
            ShopSides.Resolution side = sides.resolve(block, serverShop);
            if (side.side() == null) {
                FSound.fail(player);
                player.sendMessage(side.error());
                return;
            }
            ItemStack item = ShopLookup.itemFor(items, ShopKey.of(block), shopOffer);
            actions.execute(player, shopOffer, item, side.side(), buying);
        } catch (Exception exception) {
            FConsoleLogger.sendConsoleMessage(String.valueOf(exception), EMessageTypeEnum.ERROR);
            e.setCancelled(true);
        }
    }

    /** Placas de loja nunca abrem o editor (clique com tinta, corante ou placa na mão). */
    @EventHandler(ignoreCancelled = true)
    public void onOpenSign(PlayerOpenSignEvent e) {
        if (new SignShopImpl(new SignShop(e.getSign(), e.getPlayer())).isSignShop()) {
            e.setCancelled(true);
        }
    }
}
