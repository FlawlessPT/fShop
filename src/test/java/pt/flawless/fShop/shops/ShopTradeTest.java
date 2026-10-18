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
import static pt.flawless.fShop.shops.ShopTrade.Result.NO_STOCK;
import static pt.flawless.fShop.shops.ShopTrade.Result.OWNER_NO_FUNDS;
import static pt.flawless.fShop.shops.ShopTrade.Result.OWN_SHOP;
import static pt.flawless.fShop.shops.ShopTrade.Result.SHOP_FULL;
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
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID PLAYER2 = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final ShopOffer OFFER = new ShopOffer(16, Material.DIAMOND, 1000, 500);
    private static final ItemStack ITEM = new ItemStack(Material.DIAMOND);

    static class FakeEconomy implements EconomyApi {
        final List<String> calls = new ArrayList<>();
        CompletableFuture<Long> withdrawResult = CompletableFuture.completedFuture(0L);
        CompletableFuture<Long> depositResult = CompletableFuture.completedFuture(0L);
        CompletableFuture<Long> transferResult = CompletableFuture.completedFuture(0L);
        CompletableFuture<Long> balanceResult;
        long balance = 1_000_000;

        @Override
        public CompletableFuture<Long> getBalance(UUID uuid) {
            return balanceResult != null ? balanceResult : CompletableFuture.completedFuture(balance);
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
            calls.add("transfer " + label(from) + ">" + label(to) + " " + cents);
            return transferResult;
        }

        private static String label(UUID id) {
            return id.equals(PLAYER) ? "P" : id.equals(OWNER) ? "O" : "Q";
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
        boolean failGive;
        boolean online = true;
        ItemStack lastItem;
        private int canHoldCalls;

        @Override
        public boolean canHold(ItemStack item, int quantity) {
            lastItem = item;
            boolean ok = canHoldCalls++ == 0 ? canHoldFirst : canHoldLater;
            return online && ok;
        }

        @Override
        public boolean has(ItemStack item, int quantity) {
            lastItem = item;
            return online && items >= quantity;
        }

        @Override
        public void give(ItemStack item, int quantity) {
            if (failGive) {
                throw new IllegalStateException("sem espaço");
            }
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
    private FakeBag chest;
    private ShopSide side;
    private ShopSide chestSide;
    private List<Throwable> errors;
    private ShopTrade trade;

    @BeforeEach
    void setUp() {
        economy = new FakeEconomy();
        bag = new FakeBag();
        chest = new FakeBag();
        side = new ServerSide(economy);
        chestSide = new ChestSide(economy, OWNER, "Dono", chest);
        errors = new ArrayList<>();
        trade = new ShopTrade(economy, Runnable::run, errors::add);
    }

    @Test
    void buy_withdrawsThenGivesItems() {
        assertEquals(BOUGHT, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(List.of("withdraw 1000"), economy.calls);
        assertEquals(16, bag.items);
    }

    @Test
    void buy_whenShopDoesNotSell_isNotAvailable() {
        ShopOffer sellOnly = new ShopOffer(16, Material.DIAMOND, 0, 500);
        assertEquals(NOT_AVAILABLE, trade.buy(PLAYER, sellOnly, ITEM, bag, side).join());
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void buy_withoutSpace_doesNotTouchTheMoney() {
        bag.canHoldFirst = false;
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertTrue(economy.calls.isEmpty());
        assertEquals(0, bag.items);
    }

    @Test
    void buy_whenSpaceDisappearsAfterWithdraw_refunds() {
        bag.canHoldLater = false;
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(List.of("withdraw 1000", "deposit 1000"), economy.calls);
        assertEquals(0, bag.items);
    }

    @Test
    void buy_withInsufficientFunds_givesNothing() {
        economy.withdrawResult = CompletableFuture.failedFuture(new InsufficientFundsException(PLAYER, 0, 1000));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(0, bag.items);
        assertTrue(errors.isEmpty());
    }

    @Test
    void buy_unwrapsCompletionException() {
        economy.withdrawResult = CompletableFuture.failedFuture(
                new CompletionException(new InsufficientFundsException(PLAYER, 0, 1000)));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());

        economy.withdrawResult = CompletableFuture.failedFuture(
                new CompletionException(new AccountNotFoundException(PLAYER)));
        assertEquals(NO_ACCOUNT, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
    }

    @Test
    void buy_withUnexpectedFailure_isErrorAndLogged() {
        economy.withdrawResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(ERROR, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(1, errors.size());
        assertEquals(0, bag.items);
    }

    @Test
    void secondClickWhileFirstIsPending_isBusy_andGuardIsReleasedAfterwards() {
        economy.withdrawResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> first = trade.buy(PLAYER, OFFER, ITEM, bag, side);
        assertFalse(first.isDone());

        assertEquals(BUSY, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(List.of("withdraw 1000"), economy.calls);

        economy.withdrawResult.complete(0L);
        assertEquals(BOUGHT, first.join());

        economy.withdrawResult = CompletableFuture.completedFuture(0L);
        assertEquals(BOUGHT, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(32, bag.items);
    }

    @Test
    void sell_takesItemsThenDeposits() {
        bag.items = 20;
        assertEquals(SOLD, trade.sell(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(List.of("deposit 500"), economy.calls);
        assertEquals(4, bag.items);
    }

    @Test
    void sell_whenShopDoesNotBuy_isNotAvailable() {
        ShopOffer buyOnly = new ShopOffer(16, Material.DIAMOND, 1000, 0);
        bag.items = 20;
        assertEquals(NOT_AVAILABLE, trade.sell(PLAYER, buyOnly, ITEM, bag, side).join());
        assertEquals(20, bag.items);
    }

    @Test
    void sell_withoutEnoughItems_doesNotTouchTheMoney() {
        bag.items = 3;
        assertEquals(NOT_ENOUGH_ITEMS, trade.sell(PLAYER, OFFER, ITEM, bag, side).join());
        assertTrue(economy.calls.isEmpty());
        assertEquals(3, bag.items);
    }

    @Test
    void sell_whenDepositFails_returnsTheItems() {
        bag.items = 20;
        economy.depositResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(ERROR, trade.sell(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(20, bag.items);
        assertEquals(1, errors.size());
    }

    @Test
    void failedRefund_isLoggedWithPlayerAndAmount() {
        bag.canHoldLater = false;
        economy.depositResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag, side).join());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains(PLAYER.toString()));
        assertTrue(errors.get(0).getMessage().contains("1000"));
    }

    @Test
    void buy_givesTheShopItem() {
        trade.buy(PLAYER, OFFER, ITEM, bag, side).join();
        assertSame(ITEM, bag.lastItem);
    }

    @Test
    void sell_takesTheShopItem() {
        bag.items = 20;
        trade.sell(PLAYER, OFFER, ITEM, bag, side).join();
        assertSame(ITEM, bag.lastItem);
    }

    @Test
    void chestBuy_takesFromTheChestAndTransfersToTheOwner() {
        chest.items = 20;
        assertEquals(BOUGHT, trade.buy(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(4, chest.items);
        assertEquals(16, bag.items);
        assertEquals(List.of("transfer P>O 1000"), economy.calls);
    }

    @Test
    void chestBuy_withoutStock_isNoStockAndTouchesNoMoney() {
        chest.items = 5;
        assertEquals(NO_STOCK, trade.buy(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(5, chest.items);
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void chestBuy_whenPaymentFails_putsTheItemsBack() {
        chest.items = 20;
        economy.transferResult = CompletableFuture.failedFuture(new InsufficientFundsException(PLAYER, 0, 1000));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(20, chest.items);
        assertEquals(0, bag.items);
    }

    @Test
    void chestBuy_whenSpaceDisappears_refundsTheBuyerAndRestocks() {
        chest.items = 20;
        bag.canHoldLater = false;
        assertEquals(NO_SPACE, trade.buy(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(20, chest.items);
        assertEquals(List.of("transfer P>O 1000", "transfer O>P 1000"), economy.calls);
    }

    @Test
    void twoBuyersCannotTakeTheSameLastStock() {
        chest.items = 16;
        economy.transferResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> first = trade.buy(PLAYER, OFFER, ITEM, bag, chestSide);
        FakeBag other = new FakeBag();
        assertEquals(NO_STOCK, trade.buy(PLAYER2, OFFER, ITEM, other, chestSide).join());
        assertFalse(first.isDone());

        economy.transferResult.complete(0L);
        assertEquals(BOUGHT, first.join());
        assertEquals(0, chest.items);
        assertEquals(16, bag.items);
        assertEquals(0, other.items);
    }

    @Test
    void chestSell_putsTheItemsInTheChestThenTheOwnerPays() {
        bag.items = 20;
        assertEquals(SOLD, trade.sell(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(4, bag.items);
        assertEquals(16, chest.items);
        assertEquals(List.of("transfer O>P 500"), economy.calls);
    }

    @Test
    void chestSell_whenTheChestIsFull_takesNothing() {
        bag.items = 20;
        chest.canHoldFirst = false;
        assertEquals(SHOP_FULL, trade.sell(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void chestSell_whenTheOwnerHasNoMoney_returnsTheItems() {
        bag.items = 20;
        economy.transferResult = CompletableFuture.failedFuture(new InsufficientFundsException(OWNER, 0, 500));
        assertEquals(OWNER_NO_FUNDS, trade.sell(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
    }

    @Test
    void ownShop_isRefusedBothWays() {
        ShopSide mine = new ChestSide(economy, PLAYER, "Eu", chest);
        chest.items = 20;
        bag.items = 20;
        assertEquals(OWN_SHOP, trade.buy(PLAYER, OFFER, ITEM, bag, mine).join());
        assertEquals(OWN_SHOP, trade.sell(PLAYER, OFFER, ITEM, bag, mine).join());
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void chestRestockFailure_isLoggedAndTheResultIsStillReported() {
        chest.items = 20;
        chest.failGive = true;
        economy.transferResult = CompletableFuture.failedFuture(new InsufficientFundsException(PLAYER, 0, 1000));
        assertEquals(INSUFFICIENT_FUNDS, trade.buy(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(1, errors.size());
    }

    @Test
    void chestSell_whenTheOwnerCannotAfford_failsBeforeMovingAnything() {
        bag.items = 20;
        economy.balance = 100;
        assertEquals(OWNER_NO_FUNDS, trade.sell(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
        assertTrue(economy.calls.stream().noneMatch(c -> c.startsWith("transfer")));
    }

    @Test
    void chestSell_whenTheBalanceCheckFails_isReportedAndNothingMoves() {
        bag.items = 20;
        economy.balanceResult = CompletableFuture.failedFuture(new IllegalStateException("db em baixo"));
        assertEquals(ERROR, trade.sell(PLAYER, OFFER, ITEM, bag, chestSide).join());
        assertEquals(1, errors.size());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
    }

    @Test
    void chestSell_whenPaymentFails_returnsTheItemsAndLeavesTheChestUntouched() {
        bag.items = 20;
        chest.items = 3;
        economy.transferResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> sale = trade.sell(PLAYER, OFFER, ITEM, bag, chestSide);
        assertFalse(sale.isDone());
        assertEquals(4, bag.items);
        assertEquals(3, chest.items);
        chest.items = 0;
        economy.transferResult.completeExceptionally(new InsufficientFundsException(OWNER, 0, 500));
        assertEquals(OWNER_NO_FUNDS, sale.join());
        assertTrue(errors.isEmpty());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
    }

    @Test
    void chestSell_whenTheChestFillsDuringPayment_logsAndStillSells() {
        bag.items = 20;
        economy.transferResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> sale = trade.sell(PLAYER, OFFER, ITEM, bag, chestSide);
        chest.failGive = true;
        economy.transferResult.complete(0L);
        assertEquals(SOLD, sale.join());
        assertEquals(1, errors.size());
        assertEquals(4, bag.items);
    }

    @Test
    void sell_whenThePlayerGoesOfflineBeforeTheBalanceCheck_takesNothing() {
        bag.items = 20;
        economy.balanceResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> sale = trade.sell(PLAYER, OFFER, ITEM, bag, chestSide);
        assertFalse(sale.isDone());
        bag.online = false;
        economy.balanceResult.complete(1_000_000L);
        assertEquals(NOT_ENOUGH_ITEMS, sale.join());
        assertEquals(20, bag.items);
        assertEquals(0, chest.items);
        assertTrue(economy.calls.isEmpty());
    }

    @Test
    void buy_whenThePlayerGoesOfflineDuringPayment_refundsAndRestocks() {
        chest.items = 20;
        economy.transferResult = new CompletableFuture<>();
        CompletableFuture<ShopTrade.Result> purchase = trade.buy(PLAYER, OFFER, ITEM, bag, chestSide);
        assertFalse(purchase.isDone());
        bag.online = false;
        economy.transferResult.complete(0L);
        assertEquals(NO_SPACE, purchase.join());
        assertEquals(20, chest.items);
        assertEquals(0, bag.items);
        assertEquals(List.of("transfer P>O 1000", "transfer O>P 1000"), economy.calls);
    }
}
