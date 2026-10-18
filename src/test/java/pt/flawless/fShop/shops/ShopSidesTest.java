package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ShopSidesTest {

    @Test
    void serverShopResolvesToTheServerSideWithoutTouchingTheBlock() {
        ShopSides sides = new ShopSides(null, new PlayerShops(null, error -> { }));
        ShopSides.Resolution resolution = sides.resolve(null, true);
        assertNotNull(resolution.side());
        assertNull(resolution.error());
        assertEquals("Loja", resolution.side().title());
    }
}
