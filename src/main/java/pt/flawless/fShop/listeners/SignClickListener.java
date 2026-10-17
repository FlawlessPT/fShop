package pt.flawless.fShop.listeners;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fShop.shops.PlayerBag;
import pt.flawless.fShop.shops.ShopOffer;
import pt.flawless.fShop.shops.ShopTrade;
import pt.flawless.fShop.shops.SignShop;
import pt.flawless.fShop.shops.SignShopImpl;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

public class SignClickListener implements Listener {
    private final ShopTrade trade;

    public SignClickListener(ShopTrade trade) {
        this.trade = trade;
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
                    signShop.insertItemInHand(player.getItemInHand());
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
            PlayerBag bag = new PlayerBag(player);
            CompletableFuture<ShopTrade.Result> result = buying
                    ? trade.buy(player.getUniqueId(), shopOffer, new ItemStack(shopOffer.material()), bag)
                    : trade.sell(player.getUniqueId(), shopOffer, new ItemStack(shopOffer.material()), bag);
            result.thenAccept(r -> report(player, shopOffer, r, buying)).exceptionally(error -> {
                FConsoleLogger.sendConsoleMessage(String.valueOf(error), EMessageTypeEnum.ERROR);
                return null;
            });
        } catch (Exception exception) {
            FConsoleLogger.sendConsoleMessage(String.valueOf(exception), EMessageTypeEnum.ERROR);
            e.setCancelled(true);
        }
    }

    private void report(Player player, ShopOffer offer, ShopTrade.Result result, boolean buying) {
        String what = offer.quantity() + "x " + offer.itemName();
        switch (result) {
            case BOUGHT -> {
                FSound.success(player);
                withBalance(player, "§aCompraste §7" + what + " §apor §7" + trade.format(offer.buyCents()) + "§a.");
            }
            case SOLD -> {
                FSound.success(player);
                withBalance(player, "§aVendeste §7" + what + " §apor §7" + trade.format(offer.sellCents()) + "§a.");
            }
            case NOT_AVAILABLE -> fail(player, buying
                    ? "§cEsta loja não vende este item."
                    : "§cEsta loja não compra este item.");
            case NO_SPACE -> fail(player, "§cNão tens espaço no inventário.");
            case NOT_ENOUGH_ITEMS -> fail(player, "§cNão tens itens suficientes para vender.");
            case INSUFFICIENT_FUNDS -> fail(player, "§cNão tens dinheiro suficiente.");
            case NO_ACCOUNT -> fail(player, "§cA tua conta de economia não existe. Volta a entrar no servidor.");
            case BUSY -> player.sendMessage("§cAguarda que a transação anterior termine.");
            case ERROR -> fail(player, "§cErro ao processar a transação. Tenta mais tarde.");
        }
    }

    /** Envia a mensagem com o saldo atual no fim; sem saldo (erro), envia só a mensagem. */
    private void withBalance(Player player, String message) {
        trade.balance(player.getUniqueId()).whenComplete((balance, error) -> player.sendMessage(
                error == null ? message + " Novo saldo: §7" + trade.format(balance) : message));
    }

    private void fail(Player player, String message) {
        FSound.fail(player);
        player.sendMessage(message);
    }
}
