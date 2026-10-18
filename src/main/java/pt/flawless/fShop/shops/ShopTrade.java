package pt.flawless.fShop.shops;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.economy.AccountNotFoundException;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.economy.InsufficientFundsException;

/**
 * Compra e venda numa loja (do servidor ou de jogador), através de uma {@link ShopSide}.
 * {@code buy} e {@code sell} são chamados na thread
 * principal; o futuro devolvido completa na thread principal (via {@code mainThread}).
 */
public final class ShopTrade {

    public enum Result {
        BOUGHT, SOLD, NOT_AVAILABLE, NO_SPACE, NOT_ENOUGH_ITEMS, INSUFFICIENT_FUNDS, NO_ACCOUNT, BUSY, ERROR,
        NO_STOCK, SHOP_FULL, OWNER_NO_FUNDS, OWN_SHOP
    }

    /** O inventário do jogador, tal como a transação o vê. {@code item} é o modelo da loja: nunca se altera. */
    public interface Bag {
        boolean canHold(ItemStack item, int quantity);

        boolean has(ItemStack item, int quantity);

        void give(ItemStack item, int quantity);

        void take(ItemStack item, int quantity);
    }

    private final EconomyApi economy;
    private final Executor mainThread;
    private final Consumer<Throwable> onError;
    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    public ShopTrade(EconomyApi economy, Executor mainThread, Consumer<Throwable> onError) {
        this.economy = economy;
        this.mainThread = mainThread;
        this.onError = onError;
    }

    /** A contraparte da loja do servidor. */
    public ShopSide serverSide() {
        return new ServerSide(economy);
    }

    public String format(long cents) {
        return economy.format(cents);
    }

    /** Saldo atual, entregue na thread principal; falha se a economia falhar. */
    public CompletableFuture<Long> balance(UUID player) {
        return economy.getBalance(player).whenCompleteAsync((balance, error) -> {
        }, mainThread);
    }

    public CompletableFuture<Result> buy(UUID player, ShopOffer offer, ItemStack item, Bag bag, ShopSide side) {
        if (!offer.canBuy()) {
            return done(Result.NOT_AVAILABLE);
        }
        if (isOwner(side, player)) {
            return done(Result.OWN_SHOP);
        }
        return guarded(player, () -> {
            if (!bag.canHold(item, offer.quantity())) {
                return done(Result.NO_SPACE);
            }
            if (!side.takeStock(item, offer.quantity())) {
                return done(Result.NO_STOCK);
            }
            return side.collect(player, offer.buyCents()).handleAsync((paid, error) -> {
                if (error != null) {
                    restock(side, item, offer.quantity());
                    return failure(error, Result.INSUFFICIENT_FUNDS);
                }
                if (!bag.canHold(item, offer.quantity())) {
                    restock(side, item, offer.quantity());
                    refund(side, player, offer.buyCents());
                    return Result.NO_SPACE;
                }
                bag.give(item, offer.quantity());
                return Result.BOUGHT;
            }, mainThread);
        });
    }

    public CompletableFuture<Result> sell(UUID player, ShopOffer offer, ItemStack item, Bag bag, ShopSide side) {
        if (!offer.canSell()) {
            return done(Result.NOT_AVAILABLE);
        }
        if (isOwner(side, player)) {
            return done(Result.OWN_SHOP);
        }
        return guarded(player, () -> {
            if (!bag.has(item, offer.quantity())) {
                return done(Result.NOT_ENOUGH_ITEMS);
            }
            if (!side.canStore(item, offer.quantity())) {
                return done(Result.SHOP_FULL);
            }
            bag.take(item, offer.quantity());
            side.putStock(item, offer.quantity());
            return side.pay(player, offer.sellCents()).handleAsync((paid, error) -> {
                if (error == null) {
                    return Result.SOLD;
                }
                if (!side.takeStock(item, offer.quantity())) {
                    onError.accept(new IllegalStateException("o stock da loja mudou ao reverter uma venda: jogador "
                            + player + ", " + offer.quantity() + " itens"));
                }
                bag.give(item, offer.quantity());
                return failure(error, Result.OWNER_NO_FUNDS);
            }, mainThread);
        });
    }

    private static boolean isOwner(ShopSide side, UUID player) {
        return side.ownerId().filter(player::equals).isPresent();
    }

    /** Repõe o stock retirado; se o baú mudou entretanto e já não cabe, regista o erro em vez de o propagar. */
    private void restock(ShopSide side, ItemStack item, int quantity) {
        try {
            side.putStock(item, quantity);
        } catch (RuntimeException e) {
            onError.accept(new IllegalStateException("não foi possível repor o stock da loja (" + quantity
                    + " itens)", e));
        }
    }

    private CompletableFuture<Result> guarded(UUID player, Supplier<CompletableFuture<Result>> trade) {
        if (!inFlight.add(player)) {
            return done(Result.BUSY);
        }
        CompletableFuture<Result> future;
        try {
            future = trade.get();
        } catch (RuntimeException e) {
            inFlight.remove(player);
            throw e;
        }
        return future.whenComplete((result, error) -> inFlight.remove(player));
    }

    /** {@code insufficient}: o que significa "sem fundos" nesta troca (o comprador na compra, o dono na venda). */
    private Result failure(Throwable error, Result insufficient) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (cause instanceof InsufficientFundsException) {
            return insufficient;
        }
        if (cause instanceof AccountNotFoundException) {
            return Result.NO_ACCOUNT;
        }
        onError.accept(cause);
        return Result.ERROR;
    }

    private void refund(ShopSide side, UUID player, long cents) {
        side.pay(player, cents).exceptionally(error -> {
            onError.accept(new IllegalStateException("reembolso falhou: jogador " + player + ", " + cents
                    + " cêntimos por devolver", error));
            return null;
        });
    }

    private static CompletableFuture<Result> done(Result result) {
        return CompletableFuture.completedFuture(result);
    }
}
