package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.flawless.fShop.TestServer;

class ItemMatcherTest {

    @BeforeAll
    static void server() {
        TestServer.install();
    }

    private static ItemStack enchanted(Material material) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack named(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    @Test
    void sameTypeMatches() {
        assertTrue(ItemMatcher.matches(new ItemStack(Material.DIAMOND), new ItemStack(Material.DIAMOND)));
    }

    @Test
    void differentTypeDoesNotMatch() {
        assertFalse(ItemMatcher.matches(new ItemStack(Material.DIAMOND), new ItemStack(Material.EMERALD)));
    }

    @Test
    void nullItemDoesNotMatch() {
        assertFalse(ItemMatcher.matches(new ItemStack(Material.DIAMOND), null));
    }

    @Test
    void amountIsIgnored() {
        assertTrue(ItemMatcher.matches(new ItemStack(Material.DIAMOND, 1), new ItemStack(Material.DIAMOND, 64)));
    }

    @Test
    void differentDataValueDoesNotMatch() {
        assertFalse(ItemMatcher.matches(new ItemStack(Material.WOOL, 1, (short) 1), new ItemStack(Material.WOOL, 1, (short) 2)));
        assertTrue(ItemMatcher.matches(new ItemStack(Material.WOOL, 1, (short) 1), new ItemStack(Material.WOOL, 1, (short) 1)));
    }

    @Test
    void toolDurabilityIsIgnored() {
        ItemStack worn = new ItemStack(Material.DIAMOND_SWORD);
        worn.setDurability((short) 120);
        assertTrue(ItemMatcher.matches(new ItemStack(Material.DIAMOND_SWORD), worn));
    }

    @Test
    void enchantedItemDoesNotMatchPlainTemplate() {
        assertFalse(ItemMatcher.matches(new ItemStack(Material.DIAMOND_SWORD), enchanted(Material.DIAMOND_SWORD)));
    }

    @Test
    void plainItemDoesNotMatchEnchantedTemplate() {
        assertFalse(ItemMatcher.matches(enchanted(Material.DIAMOND_SWORD), new ItemStack(Material.DIAMOND_SWORD)));
    }

    @Test
    void sameEnchantmentsMatch() {
        assertTrue(ItemMatcher.matches(enchanted(Material.DIAMOND_SWORD), enchanted(Material.DIAMOND_SWORD)));
    }

    @Test
    void renamedItemDoesNotMatchPlainTemplate() {
        assertFalse(ItemMatcher.matches(new ItemStack(Material.DIAMOND_SWORD), named(Material.DIAMOND_SWORD, "Excalibur")));
        assertFalse(ItemMatcher.matches(named(Material.DIAMOND_SWORD, "Excalibur"), new ItemStack(Material.DIAMOND_SWORD)));
    }
}
