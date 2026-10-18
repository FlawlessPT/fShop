package pt.flawless.fShop.shops;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

/** Executa uma compra ou venda e diz o resultado ao jogador. Partilhado pela placa e pelo menu da loja. */
public final class ShopActions {
    private final ShopTrade trade;

    public ShopActions(ShopTrade trade) {
        this.trade = trade;
    }

    public String format(long cents) {
        return trade.format(cents);
    }

    public void execute(Player player, ShopOffer offer, ItemStack item, ShopSide side, boolean buying) {
        PlayerBag bag = new PlayerBag(player);
        CompletableFuture<ShopTrade.Result> result = buying
                ? trade.buy(player.getUniqueId(), offer, item, bag, side)
                : trade.sell(player.getUniqueId(), offer, item, bag, side);
        result.thenAccept(r -> report(player, offer, side, r, buying)).exceptionally(error -> {
            FConsoleLogger.sendConsoleMessage(String.valueOf(error), EMessageTypeEnum.ERROR);
            return null;
        });
    }

    private void report(Player player, ShopOffer offer, ShopSide side, ShopTrade.Result result, boolean buying) {
        String what = offer.quantity() + "x " + offer.itemName();
        switch (result) {
            case BOUGHT -> {
                FSound.success(player);
                withBalance(player, "§aCompraste §7" + what + " §apor §7" + trade.format(offer.buyCents()) + "§a.");
                notifyOwner(player, offer, side, true);
            }
            case SOLD -> {
                FSound.success(player);
                withBalance(player, "§aVendeste §7" + what + " §apor §7" + trade.format(offer.sellCents()) + "§a.");
                notifyOwner(player, offer, side, false);
            }
            case NOT_AVAILABLE -> fail(player, buying
                    ? "§cEsta loja não vende este item."
                    : "§cEsta loja não compra este item.");
            case NO_SPACE -> fail(player, "§cNão tens espaço no inventário.");
            case NOT_ENOUGH_ITEMS -> fail(player, "§cNão tens itens suficientes para vender.");
            case INSUFFICIENT_FUNDS -> fail(player, "§cNão tens dinheiro suficiente.");
            case NO_ACCOUNT -> fail(player, "§cA tua conta de economia não existe. Volta a entrar no servidor.");
            case NO_STOCK -> fail(player, "§cEsta loja está sem stock.");
            case SHOP_FULL -> fail(player, "§cO baú da loja está cheio.");
            case OWNER_NO_FUNDS -> fail(player, "§cO dono da loja não tem dinheiro suficiente.");
            case OWN_SHOP -> fail(player, "§cNão podes trocar com a tua própria loja.");
            case BUSY -> player.sendMessage("§cAguarda que a transação anterior termine.");
            case ERROR -> fail(player, "§cErro ao processar a transação. Tenta mais tarde.");
        }
    }

    /** Numa loja de jogador, avisa o dono se estiver online; a loja do servidor não avisa ninguém. */
    private void notifyOwner(Player trader, ShopOffer offer, ShopSide side, boolean traderBought) {
        side.ownerId().map(Bukkit::getPlayer).ifPresent(owner -> {
            String what = offer.quantity() + "x " + offer.itemName();
            owner.sendMessage(traderBought
                    ? "§e" + trader.getName() + " §acomprou §7" + what + " §apor §7" + trade.format(offer.buyCents())
                            + " §ana tua loja."
                    : "§e" + trader.getName() + " §avendeu-te §7" + what + " §apor §7"
                            + trade.format(offer.sellCents()) + "§a.");
        });
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
