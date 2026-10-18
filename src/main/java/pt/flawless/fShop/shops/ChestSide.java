package pt.flawless.fShop.shops;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.economy.EconomyApi;

/** Loja de jogador: o stock é o baú e o dinheiro passa entre as contas do jogador e do dono por {@code transfer}. */
public final class ChestSide implements ShopSide {
    private final EconomyApi economy;
    private final UUID owner;
    private final String ownerName;
    private final ShopTrade.Bag chest;

    public ChestSide(EconomyApi economy, UUID owner, String ownerName, ShopTrade.Bag chest) {
        this.economy = economy;
        this.owner = owner;
        this.ownerName = ownerName;
        this.chest = chest;
    }

    @Override
    public Optional<UUID> ownerId() {
        return Optional.of(owner);
    }

    @Override
    public String title() {
        return "Loja de " + ownerName;
    }

    @Override
    public boolean takeStock(ItemStack item, int quantity) {
        if (!chest.has(item, quantity)) {
            return false;
        }
        chest.take(item, quantity);
        return true;
    }

    @Override
    public boolean canStore(ItemStack item, int quantity) {
        return chest.canHold(item, quantity);
    }

    @Override
    public void putStock(ItemStack item, int quantity) {
        chest.give(item, quantity);
    }

    @Override
    public CompletableFuture<?> collect(UUID buyer, long cents) {
        return economy.transfer(buyer, owner, cents);
    }

    @Override
    public CompletableFuture<Boolean> canPay(long cents) {
        return economy.getBalance(owner).thenApply(balance -> balance >= cents);
    }

    @Override
    public CompletableFuture<?> pay(UUID player, long cents) {
        return economy.transfer(owner, player, cents);
    }
}
