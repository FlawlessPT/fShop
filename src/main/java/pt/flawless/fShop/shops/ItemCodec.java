package pt.flawless.fShop.shops;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/** Converte um {@link ItemStack} em texto (Base64 da serialização do Bukkit) para guardar na base de dados. */
public final class ItemCodec {

    private ItemCodec() {
    }

    public static String encode(ItemStack item) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
            out.writeObject(item);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    /** Vazio se o texto não for Base64 válido ou não contiver um {@link ItemStack}. */
    public static Optional<ItemStack> decode(String text) {
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(text)))) {
            return in.readObject() instanceof ItemStack item ? Optional.of(item) : Optional.empty();
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
