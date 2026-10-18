package pt.flawless.fShop.shops;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;

/**
 * A outra parte de uma troca: a loja do servidor (stock infinito, dinheiro do nada) ou uma loja de jogador (stock
 * num baú, dinheiro do dono). As operações de stock são síncronas, na thread principal; as de dinheiro são
 * assíncronas.
 */
public interface ShopSide {

    /** O dono da loja; vazio na loja do servidor. */
    Optional<UUID> ownerId();

    /** Título do menu da loja. */
    String title();

    /** Retira {@code quantity} do stock; {@code false} (sem alterar nada) se já não há. */
    boolean takeStock(ItemStack item, int quantity);

    boolean canStore(ItemStack item, int quantity);

    /** Guarda {@code quantity} no stock; só se chama depois de {@link #canStore}. */
    void putStock(ItemStack item, int quantity);

    /** O comprador paga à loja. */
    CompletableFuture<?> collect(UUID buyer, long cents);

    /** A loja paga a um jogador (venda ou reembolso). */
    CompletableFuture<?> pay(UUID player, long cents);
}
