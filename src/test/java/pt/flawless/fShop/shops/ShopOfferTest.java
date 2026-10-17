package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ShopOfferTest {

    @Test
    void buyOnly_readsPriceInEuros() {
        ShopOffer offer = ShopOffer.fromSign("§l16", "§2C 10", "Diamond").orElseThrow();
        assertEquals(16, offer.quantity());
        assertEquals(Material.DIAMOND, offer.material());
        assertEquals(1000, offer.buyCents());
        assertEquals(0, offer.sellCents());
        assertTrue(offer.canBuy());
        assertFalse(offer.canSell());
    }

    @Test
    void sellOnly() {
        ShopOffer offer = ShopOffer.fromSign("1", "§cV 7", "Stone").orElseThrow();
        assertEquals(0, offer.buyCents());
        assertEquals(700, offer.sellCents());
        assertFalse(offer.canBuy());
        assertTrue(offer.canSell());
    }

    @Test
    void buyAndSell() {
        ShopOffer offer = ShopOffer.fromSign("§l64", "§6C 10 §0/ §cV 5", "Cobblestone").orElseThrow();
        assertEquals(1000, offer.buyCents());
        assertEquals(500, offer.sellCents());
    }

    @Test
    void pricesWithSuffixes() {
        ShopOffer offer = ShopOffer.fromSign("1", "C 1k / V 1,5k", "Diamond").orElseThrow();
        assertEquals(100000, offer.buyCents());
        assertEquals(150000, offer.sellCents());
        assertEquals(200_000_000, ShopOffer.fromSign("1", "C 2M", "Diamond").orElseThrow().buyCents());
        assertEquals(Optional.empty(), ShopOffer.fromSign("1", "C 1,5", "Diamond"));
    }

    @Test
    void itemNameWithSpacesMatchesMaterial() {
        ShopOffer offer = ShopOffer.fromSign("1", "C 50", "Diamond Sword").orElseThrow();
        assertEquals(Material.DIAMOND_SWORD, offer.material());
        assertEquals("diamond sword", offer.itemName());
    }

    @Test
    void itemNotSetYet_isEmpty() {
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "C 10", "---"));
    }

    @Test
    void unknownItem_isEmpty() {
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "C 10", "Banana Split"));
    }

    @Test
    void badQuantity_isEmpty() {
        assertEquals(Optional.empty(), ShopOffer.fromSign("abc", "C 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("", "C 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("0", "C 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("-3", "C 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("2305", "C 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("99999999999", "C 10", "Diamond"));
    }

    @Test
    void maxQuantityIsAccepted() {
        assertEquals(2304, ShopOffer.fromSign("2304", "C 10", "Diamond").orElseThrow().quantity());
    }

    @Test
    void badPrice_isEmpty() {
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "C 0", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "C 10.5", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "X 10", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", null, "Diamond"));
    }

    @Test
    void priceThatOverflowsCents_isEmpty() {
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "C 99999999999999999999", "Diamond"));
        assertEquals(Optional.empty(), ShopOffer.fromSign("16", "V 92233720368547759", "Diamond"));
    }
}
