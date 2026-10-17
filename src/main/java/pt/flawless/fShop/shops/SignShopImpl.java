package pt.flawless.fShop.shops;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fShop.enums.ShopType;
import pt.flawless.fShop.managers.SignShopManager;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.sounds.FSound;

public class SignShopImpl {
    private final SignShop signShop;

    public SignShopImpl(SignShop signShop) {
        this.signShop = signShop;
    }

    public void createPlayerShop() {
        createShop(ShopType.PlayerShop);
    }

    public void createServerShop() {
        createShop(ShopType.ServerShop);
    }

    private void createShop(ShopType shopType) {
        SignChangeEvent sign = signShop.getCreatableSign();
        Player player = signShop.getPlayer();

        SignShopManager signShopManager = new SignShopManager(sign, player);

        String quantity = sign.getLine(1);
        String signShopType = sign.getLine(2);
        String price = sign.getLine(3);

        if (quantity.isEmpty() || signShopType.isEmpty() || price.isEmpty()) {
            sign.getBlock().breakNaturally();
            FSound.fail(player);
            player.sendMessage("§cErro ao criar loja. Argumentos em falta.");

            return;
        }

        try {
            signShopManager.setTitle(shopType);
            signShopManager.setQuantity(quantity);
            signShopManager.setSignShopType(signShopType, price);
            sign.setLine(3, "---");

            sign.getBlock().getState().update();

            FSound.success(player);
            player.sendMessage("§eLoja criada com sucesso.");
        } catch (Exception e1) {
            sign.getBlock().breakNaturally();
            FSound.fail(player);
            FConsoleLogger.sendConsoleMessage(e1.toString(), EMessageTypeEnum.ERROR);
            player.sendMessage("§cErro ao criar loja.");
        }
    }

    public void insertItemInHand(ItemStack itemInHand) {
        SignShopManager signShopManager = new SignShopManager(signShop.getSign(), signShop.getPlayer());
        String itemName = itemInHand.getType().name().toLowerCase();

        if (signShopManager.withItemInShop()) return;

        if (itemName.equalsIgnoreCase(Material.AIR.name())) return;

        String formattedItemName = WordUtils.capitalizeFully(itemName.replace("_", " "));
        signShopManager.setItemName(formattedItemName);
    }

    public boolean isSignShop() {
        SignShopManager signShopManager = null;

        if (signShop.getSign() != null) {
            signShopManager = new SignShopManager(signShop.getSign(), signShop.getPlayer());
        }

        if (signShop.getCreatableSign() != null) {
            signShopManager = new SignShopManager(signShop.getCreatableSign(), signShop.getPlayer());

        }
        return signShopManager != null && signShopManager.isShop();

    }
}
