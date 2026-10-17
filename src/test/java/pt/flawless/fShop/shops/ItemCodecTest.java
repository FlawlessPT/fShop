package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.flawless.fShop.TestServer;

class ItemCodecTest {

    @BeforeAll
    static void server() {
        TestServer.install();
    }

    @Test
    void plainItemRoundTrips() throws Exception {
        ItemStack original = new ItemStack(Material.WOOL, 1, (short) 5);
        ItemStack decoded = ItemCodec.decode(ItemCodec.encode(original)).orElseThrow();
        assertEquals(Material.WOOL, decoded.getType());
        assertEquals((short) 5, decoded.getDurability());
    }

    @Test
    void renamedItemKeepsItsName() throws Exception {
        ItemStack original = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = original.getItemMeta();
        meta.setDisplayName("Excalibur");
        original.setItemMeta(meta);

        ItemStack decoded = ItemCodec.decode(ItemCodec.encode(original)).orElseThrow();
        assertEquals("Excalibur", decoded.getItemMeta().getDisplayName());
    }

    @Test
    void invalidTextDecodesToEmpty() {
        assertEquals(Optional.empty(), ItemCodec.decode("isto não é base64!"));
        assertEquals(Optional.empty(), ItemCodec.decode(""));
        assertEquals(Optional.empty(), ItemCodec.decode("AAAA"));
    }

    @Test
    void validBase64ThatIsNotAnItemDecodesToEmpty() throws Exception {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (org.bukkit.util.io.BukkitObjectOutputStream out = new org.bukkit.util.io.BukkitObjectOutputStream(bytes)) {
            out.writeObject("uma string");
        }
        String text = java.util.Base64.getEncoder().encodeToString(bytes.toByteArray());
        assertTrue(ItemCodec.decode(text).isEmpty());
    }

    @Test
    void nullDecodesToEmpty() {
        assertTrue(ItemCodec.decode(null).isEmpty());
    }


    @Test
    void payloadThatFailsInsideBukkitDecodesToEmpty() throws Exception {
        byte[] raw = java.util.Base64.getDecoder().decode(ItemCodec.encode(new ItemStack(Material.DIAMOND_SWORD)));
        raw[142] ^= 0x20; // stream Java válido, mas o Bukkit lança NullPointerException ao reconstruir
        assertTrue(ItemCodec.decode(java.util.Base64.getEncoder().encodeToString(raw)).isEmpty());
    }
}
