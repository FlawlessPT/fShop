package pt.flawless.fShop.shops;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.economy.EconomyApi;

/** Loja do servidor: stock infinito; o dinheiro sai e entra na economia por {@code withdraw}/{@code deposit}. */
public final class ServerSide implements ShopSide {
    private final EconomyApi economy;

    public ServerSide(EconomyApi economy) {
        this.economy = economy;
    }

    @Override
    public Optional<UUID> ownerId() {
        return Optional.empty();
    }

    @Override
    public String title() {
        return "Loja";
    }

    @Override
    public boolean takeStock(ItemStack item, int quantity) {
        return true;
    }

    @Override
    public boolean canStore(ItemStack item, int quantity) {
        return true;
    }

    @Override
    public void putStock(ItemStack item, int quantity) {
    }

    @Override
    public CompletableFuture<?> collect(UUID buyer, long cents) {
        return economy.withdraw(buyer, cents);
    }

    @Override
    public CompletableFuture<?> pay(UUID player, long cents) {
        return economy.deposit(player, cents);
    }
}
