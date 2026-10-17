package pt.flawless.fShop.managers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SignShopManagerTest {

    @Test
    void serverTitleIgnoresColoursCaseAndSpaces() {
        assertTrue(SignShopManager.isServerTitle("Loja"));
        assertTrue(SignShopManager.isServerTitle("loja"));
        assertTrue(SignShopManager.isServerTitle("§1Loja"));
        assertTrue(SignShopManager.isServerTitle("§1§lLoja"));
        assertTrue(SignShopManager.isServerTitle(" Loja "));
    }

    @Test
    void otherTitlesAreNotServerShops() {
        assertFalse(SignShopManager.isServerTitle("Lojas"));
        assertFalse(SignShopManager.isServerTitle("Steve"));
        assertFalse(SignShopManager.isServerTitle(""));
        assertFalse(SignShopManager.isServerTitle(null));
    }
}
