package pt.flawless.fShop.shops;

import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;
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

    public boolean createPlayerShop() {
        return createShop(ShopType.PlayerShop);
    }

    public boolean createServerShop() {
        return createShop(ShopType.ServerShop);
    }

    private boolean createShop(ShopType shopType) {
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

            return false;
        }

        try {
            signShopManager.setTitle(shopType);
            signShopManager.setQuantity(quantity);
            signShopManager.setSignShopType(signShopType, price);
            sign.setLine(3, "---");

            sign.getBlock().getState().update();

            FSound.success(player);
            player.sendMessage("§eLoja criada com sucesso.");
            return true;
        } catch (Exception e1) {
            sign.getBlock().breakNaturally();
            FSound.fail(player);
            FConsoleLogger.sendConsoleMessage(e1.toString(), EMessageTypeEnum.ERROR);
            player.sendMessage("§cErro ao criar loja.");
            return false;
        }
    }

    /** Escreve o nome do item na linha 4. Devolve {@code true} só se escreveu (loja ainda sem item e item válido). */
    public boolean insertItemInHand(ItemStack itemInHand) {
        if (itemInHand == null || itemInHand.getType() == Material.AIR) return false;

        SignShopManager signShopManager = new SignShopManager(signShop.getSign(), signShop.getPlayer());
        if (signShopManager.withItemInShop()) return false;

        String itemName = itemInHand.getType().name().toLowerCase();
        String formattedItemName = Arrays.stream(itemName.split("_"))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
        signShopManager.setItemName(formattedItemName);
        return true;
    }

    public boolean isServerShop() {
        return new SignShopManager(signShop.getSign(), signShop.getPlayer()).isServerShop();
    }

    public boolean canEdit() {
        return new SignShopManager(signShop.getSign(), signShop.getPlayer()).canEdit();
    }

    public boolean hasItem() {
        return new SignShopManager(signShop.getSign(), signShop.getPlayer()).withItemInShop();
    }

    public Optional<ShopOffer> offer() {
        return new SignShopManager(signShop.getSign(), signShop.getPlayer()).offer();
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
