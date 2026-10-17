package pt.flawless.fShop.shops;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Material;
import pt.flawless.fapi.economy.AccountNotFoundException;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.economy.InsufficientFundsException;

/**
 * Compra e venda numa loja do servidor (stock infinito). {@code buy} e {@code sell} são chamados na thread
 * principal; o futuro devolvido completa na thread principal (via {@code mainThread}).
 */
public final class ShopTrade {

    public enum Result {
        BOUGHT, SOLD, NOT_AVAILABLE, NO_SPACE, NOT_ENOUGH_ITEMS, INSUFFICIENT_FUNDS, NO_ACCOUNT, BUSY, ERROR
    }

    /** O inventário do jogador, tal como a transação o vê. */
    public interface Bag {
        boolean canHold(Material material, int quantity);

        boolean has(Material material, int quantity);

        void give(Material material, int quantity);

        void take(Material material, int quantity);
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

    public String format(long cents) {
        return economy.format(cents);
    }

    /** Saldo atual, entregue na thread principal; falha se a economia falhar. */
    public CompletableFuture<Long> balance(UUID player) {
        return economy.getBalance(player).whenCompleteAsync((balance, error) -> {
        }, mainThread);
    }

    public CompletableFuture<Result> buy(UUID player, ShopOffer offer, Bag bag) {
        if (!offer.canBuy()) {
            return done(Result.NOT_AVAILABLE);
        }
        return guarded(player, () -> {
            if (!bag.canHold(offer.material(), offer.quantity())) {
                return done(Result.NO_SPACE);
            }
            return economy.withdraw(player, offer.buyCents()).handleAsync((balance, error) -> {
                if (error != null) {
                    return failure(error);
                }
                if (!bag.canHold(offer.material(), offer.quantity())) {
                    refund(player, offer.buyCents());
                    return Result.NO_SPACE;
                }
                bag.give(offer.material(), offer.quantity());
                return Result.BOUGHT;
            }, mainThread);
        });
    }

    public CompletableFuture<Result> sell(UUID player, ShopOffer offer, Bag bag) {
        if (!offer.canSell()) {
            return done(Result.NOT_AVAILABLE);
        }
        return guarded(player, () -> {
            if (!bag.has(offer.material(), offer.quantity())) {
                return done(Result.NOT_ENOUGH_ITEMS);
            }
            bag.take(offer.material(), offer.quantity());
            return economy.deposit(player, offer.sellCents()).handleAsync((balance, error) -> {
                if (error == null) {
                    return Result.SOLD;
                }
                bag.give(offer.material(), offer.quantity());
                return failure(error);
            }, mainThread);
        });
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

    private Result failure(Throwable error) {
        Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
        if (cause instanceof InsufficientFundsException) {
            return Result.INSUFFICIENT_FUNDS;
        }
        if (cause instanceof AccountNotFoundException) {
            return Result.NO_ACCOUNT;
        }
        onError.accept(cause);
        return Result.ERROR;
    }

    private void refund(UUID player, long cents) {
        economy.deposit(player, cents).exceptionally(error -> {
            onError.accept(new IllegalStateException("reembolso falhou: jogador " + player + ", " + cents
                    + " cêntimos por devolver", error));
            return null;
        });
    }

    private static CompletableFuture<Result> done(Result result) {
        return CompletableFuture.completedFuture(result);
    }
}
