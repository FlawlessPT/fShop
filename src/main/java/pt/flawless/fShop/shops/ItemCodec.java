package pt.flawless.fShop.shops;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.server.v1_8_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

/**
 * Converte um {@link ItemStack} em texto para guardar na base de dados. Usa o NBT do item (prefixo {@code nbt:}), que
 * mantém as marcas de outros plugins (ex.: itens do fCustomItems); a serialização do Bukkit do 1.8.0 perdia-as.
 * Lê também o formato antigo (Base64 da serialização do Bukkit, sem prefixo).
 */
public final class ItemCodec {

    private static final String NBT_PREFIX = "nbt:";

    private ItemCodec() {
    }

    public static String encode(ItemStack item) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        NBTCompressedStreamTools.a(CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()), bytes);
        return NBT_PREFIX + Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    /** Vazio se o texto for nulo, não for Base64 válido, não se reconstruir ou não contiver um {@link ItemStack}. */
    public static Optional<ItemStack> decode(String text) {
        try {
            if (text.startsWith(NBT_PREFIX)) {
                NBTTagCompound tag = NBTCompressedStreamTools.a(
                        new ByteArrayInputStream(Base64.getDecoder().decode(text.substring(NBT_PREFIX.length()))));
                net.minecraft.server.v1_8_R1.ItemStack nms = net.minecraft.server.v1_8_R1.ItemStack.createStack(tag);
                return nms == null ? Optional.empty() : Optional.of(CraftItemStack.asCraftMirror(nms));
            }
            try (BukkitObjectInputStream in = new BukkitObjectInputStream(
                    new ByteArrayInputStream(Base64.getDecoder().decode(text)))) {
                return in.readObject() instanceof ItemStack item ? Optional.of(item) : Optional.empty();
            }
        } catch (Exception e) { // também RuntimeException do Bukkit (material removido, linha editada à mão)
            return Optional.empty();
        }
    }
}
