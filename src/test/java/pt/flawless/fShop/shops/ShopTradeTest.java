package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.flawless.fShop.shops.ShopTrade.Result.BOUGHT;
import static pt.flawless.fShop.shops.ShopTrade.Result.BUSY;
import static pt.flawless.fShop.shops.ShopTrade.Result.ERROR;
import static pt.flawless.fShop.shops.ShopTrade.Result.INSUFFICIENT_FUNDS;
import static pt.flawless.fShop.shops.ShopTrade.Result.NOT_AVAILABLE;
import static pt.flawless.fShop.shops.ShopTrade.Result.NOT_ENOUGH_ITEMS;
import static pt.flawless.fShop.shops.ShopTrade.Result.NO_ACCOUNT;
import static pt.flawless.fShop.shops.ShopTrade.Result.NO_SPACE;
import static pt.flawless.fShop.shops.ShopTrade.Result.SOLD;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fapi.economy.AccountNotFoundException;
import pt.flawless.fapi.economy.EconomyApi;
import pt.flawless.fapi.economy.InsufficientFundsException;

class ShopTradeTest {

    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final ShopOffer OFFER = new ShopOffer(16, Material.DIAMOND, 1000, 500);
    private static final ItemStack ITEM = new ItemStack(Material.DIAMOND);

    static class FakeEconomy implements EconomyApi {
        final List<String> calls = new ArrayList<>();
        CompletableFuture<Long> withdrawResult = CompletableFuture.completedFuture(0L);
        CompletableFuture<Long> depositResult = CompletableFuture.completedFuture(0L);

        @Override
        public CompletableFuture<Long> getBalance(UUID uuid) {
            return CompletableFuture.completedFuture(0L);
        }

        @Override
        public CompletableFuture<Long> deposit(UUID uuid, long cents) {
            calls.add("deposit " + cents);
            return depositResult;
        }

        @Override
        public CompletableFuture<Long> withdraw(UUID uuid, long cents) {
            calls.add("withdraw " + cents);
            return withdrawResult;
        }

        @Override
        public CompletableFuture<Long> transfer(UUID from, UUID to, long cents) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String format(long cents) {
            return cents + "c";
        }
    }

    static class FakeBag implements ShopTrade.Bag {
        int items;
        boolean canHoldFirst = true;
        boolean canHoldLater = true;
        ItemStack lastItem;
        private int canHoldCalls;

        @Override
        public boolean canHold(ItemStack item, int quantity) {
            lastItem = item;
            return canHoldCalls++ == 0 ? canHoldFirst : canHoldLater;
        }

        @Override
        public boolean has(ItemStack item, int quantity) {
            lastItem = item;
            return items >= quantity;
        }

        @Override
        public void give(ItemStack item, int quantity) {
            lastItem = item;
            items += quantity;
        }

        @Override
        public void take(ItemStack item, int quantity) {
            lastItem = item;
            items -= quantity;
        }
    }

    private FakeEconomy economy;
    private FakeBag bag;
    private List<Throwable> errors;
    private ShopTrade trade;

    @BeforeEach
    void setUp() {
        economy = new FakeEconomy();
        bag = new FakeBag();
        errors = new ArrayList<>();
        trade = new ShopTrade(economy, Runnable::run, errors::add);
    }

    @Test
    void buy_withdrawsThenGivesItems() {
        assertEquals(BOUGHT, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(List.of("withdraw 1000"), economy.calls);
        assertEquals(16, bag.items);
    }

    @Test
    void buy_whenShopDoesNotSell_isNotAvailable() {
        ShopOffer sellOnly = new ShopOffer(16, Material.DIAMOND, 0, 500);
        assertEquals(NOT_AVAILABLE, trade.buy(PLAYER, sellOnly, ITEM, bag).join());
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void buy_withoutSpace_doesNotTouchTheMoney() {
        bag.canHoldFirst = false;
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertTrue(economy.calls.isEmpty());
        assertEquals(0, bag.items);
    }

    @Test
    void buy_whenSpaceDisappearsAfterWithdraw_refunds() {
        bag.canHoldLater = false;
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(List.of("withdraw 1000", "deposit 1000"), economy.calls);
        assertEquals(0, bag.items);
    }

    @Test
    void buy_withInsufficientFunds_givesNothing() {
        economy.withdrawResult = CompletableFuture.failedFuture(new InsufficientFundsException(PLAYER, 0, 1000));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(0, bag.items);
        assertTrue(errors.isEmpty());
    }

    @Test
    void buy_unwrapsCompletionException() {
        economy.withdrawResult = CompletableFuture.failedFuture(
                new CompletionException(new InsufficientFundsException(PLAYER, 0, 1000)));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag).join());

        economy.withdrawResult = CompletableFuture.failedFuture(
                new CompletionException(new AccountNotFoundException(PLAYER)));
        assertEquals(NO_ACCOUNT, trade.buy(PLAYER, OFFER, ITEM, bag).join());
    }

    @Test
    void buy_withUnexpectedFailure_isErrorAndLogged() {
        economy.withdrawResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(ERROR, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(1, errors.size());
        assertEquals(0, bag.items);
    }

    @Test
    void secondClickWhileFirstIsPending_isBusy_andGuardIsReleasedAfterwards() {
        economy.withdrawResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> first = trade.buy(PLAYER, OFFER, ITEM, bag);
        assertFalse(first.isDone());

        assertEquals(BUSY, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(List.of("withdraw 1000"), economy.calls);

        economy.withdrawResult.complete(0L);
        assertEquals(BOUGHT, first.join());

        economy.withdrawResult = CompletableFuture.completedFuture(0L);
        assertEquals(BOUGHT, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(32, bag.items);
    }

    @Test
    void sell_takesItemsThenDeposits() {
        bag.items = 20;
        assertEquals(SOLD, trade.sell(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(List.of("deposit 500"), economy.calls);
        assertEquals(4, bag.items);
    }

    @Test
    void sell_whenShopDoesNotBuy_isNotAvailable() {
        ShopOffer buyOnly = new ShopOffer(16, Material.DIAMOND, 1000, 0);
        bag.items = 20;
        assertEquals(NOT_AVAILABLE, trade.sell(PLAYER, buyOnly, ITEM, bag).join());
        assertEquals(20, bag.items);
    }

    @Test
    void sell_withoutEnoughItems_doesNotTouchTheMoney() {
        bag.items = 3;
        assertEquals(NOT_ENOUGH_ITEMS, trade.sell(PLAYER, OFFER, ITEM, bag).join());
        assertTrue(economy.calls.isEmpty());
        assertEquals(3, bag.items);
    }

    @Test
    void sell_whenDepositFails_returnsTheItems() {
        bag.items = 20;
        economy.depositResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(ERROR, trade.sell(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(20, bag.items);
        assertEquals(1, errors.size());
    }

    @Test
    void failedRefund_isLoggedWithPlayerAndAmount() {
        bag.canHoldLater = false;
        economy.depositResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag).join());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains(PLAYER.toString()));
        assertTrue(errors.get(0).getMessage().contains("1000"));
    }

    @Test
    void buy_givesTheShopItem() {
        trade.buy(PLAYER, OFFER, ITEM, bag).join();
        assertSame(ITEM, bag.lastItem);
    }

    @Test
    void sell_takesTheShopItem() {
        bag.items = 20;
        trade.sell(PLAYER, OFFER, ITEM, bag).join();
        assertSame(ITEM, bag.lastItem);
    }
}
