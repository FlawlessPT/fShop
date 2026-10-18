package pt.flawless.fShop.listeners;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShopProtectListenerTest {

    @Test
    void adminBreaksAnyShop() {
        assertTrue(ShopProtectListener.canBreak(true, true, false));
        assertTrue(ShopProtectListener.canBreak(true, false, false));
    }

    @Test
    void ownerBreaksOwnPlayerShop() {
        assertTrue(ShopProtectListener.canBreak(false, false, true));
    }

    @Test
    void ownerFlagNeverOpensAServerShop() {
        assertFalse(ShopProtectListener.canBreak(false, true, true));
    }

    @Test
    void otherPlayersBreakNothing() {
        assertFalse(ShopProtectListener.canBreak(false, true, false));
        assertFalse(ShopProtectListener.canBreak(false, false, false));
    }
}
