package pt.flawless.fShop.shops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ShopMenuTest {

    private static final ShopOffer OFFER = new ShopOffer(16, Material.DIAMOND, 1000, 500);

    @Test
    void buyAndSellSlotsMapToTheirActions() {
        assertEquals(ShopMenu.Action.BUY, ShopMenu.actionFor(ShopMenu.BUY_SLOT));
        assertEquals(ShopMenu.Action.SELL, ShopMenu.actionFor(ShopMenu.SELL_SLOT));
    }

    @Test
    void everyOtherSlotIsNone() {
        for (int slot = 0; slot < ShopMenu.SIZE; slot++) {
            if (slot == ShopMenu.BUY_SLOT || slot == ShopMenu.SELL_SLOT) {
                continue;
            }
            assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(slot), "slot " + slot);
        }
    }

    @Test
    void itemSlotIsNotAButton() {
        assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(ShopMenu.ITEM_SLOT));
    }

    @Test
    void slotsOfThePlayersInventoryAndInvalidSlotsAreNone() {
        assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(ShopMenu.SIZE));
        assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(40));
        assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(-1));
        assertEquals(ShopMenu.Action.NONE, ShopMenu.actionFor(-999));
    }

    @Test
    void buyLoreShowsTheRealQuantityAndThePrice() {
        assertEquals(List.of("§716x " + OFFER.itemName(), "§7Preço: §f10,00€"), ShopMenu.buyLore(OFFER, "10,00€"));
    }

    @Test
    void sellLoreShowsTheRealQuantityAndWhatThePlayerReceives() {
        assertEquals(List.of("§716x " + OFFER.itemName(), "§7Recebes: §f5,00€"), ShopMenu.sellLore(OFFER, "5,00€"));
    }
}
