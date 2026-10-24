package pt.flawless.fShop.shops;

import java.util.Base64;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/**
 * Converte um {@link ItemStack} em texto para guardar na base de dados: {@code paper:} + Base64 de
 * {@link ItemStack#serializeAsBytes()}, que mantém tudo (encantamentos, nome e o PersistentDataContainer com as
 * marcas de outros plugins, ex.: itens do fCustomItems). Os formatos antigos do 1.8 ({@code nbt:} e o Base64 da
 * serialização do Bukkit) já não se leem: {@link #decode} devolve vazio e a loja fica como "item em falta".
 */
public final class ItemCodec {

    private static final String PREFIX = "paper:";

    private ItemCodec() {
    }

    public static String encode(ItemStack item) {
        return PREFIX + Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    /** Vazio se o texto for nulo, não tiver o prefixo {@code paper:}, não for Base64 válido ou não se reconstruir. */
    public static Optional<ItemStack> decode(String text) {
        try {
            if (!text.startsWith(PREFIX)) {
                return Optional.empty();
            }
            return Optional.of(ItemStack.deserializeBytes(Base64.getDecoder().decode(text.substring(PREFIX.length()))));
        } catch (Exception e) { // também RuntimeException do Paper (item inválido, linha editada à mão)
            return Optional.empty();
        }
    }
}
