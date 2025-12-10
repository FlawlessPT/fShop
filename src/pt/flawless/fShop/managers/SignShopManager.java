package pt.flawless.fShop.managers;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.enums.ShopType;
import pt.flawless.fapi.logs.FConsoleLogger;

public class SignShopManager {
    private final Player player;
    private SignChangeEvent creatableSign;
    private Sign sign;

    public SignShopManager(SignChangeEvent creatableSign, Player player) {
        this.creatableSign = creatableSign;
        this.player = player;
    }

    public SignShopManager(Sign sign, Player player) {
        this.sign = sign;
        this.player = player;
    }

    public void setTitle(ShopType shopType) {
        if (shopType == ShopType.PlayerShop) {
            this.creatableSign.setLine(0, "§9" + this.player.getName());
        } else {
            this.creatableSign.setLine(0, "§1§lLoja");
        }
    }

    public void setQuantity(String quantity) {
        this.creatableSign.setLine(1, "§l" + quantity);
    }

    // TODO: Create new exception
    public void setSignShopType(String signShopType, String price) throws Exception {
        String signText;

        if (signShopType.equalsIgnoreCase("C")) {
            signText = "§2C " + price;
        } else if (signShopType.equalsIgnoreCase("V")) {
            signText = "§cV " + price;
        } else if (signShopType.equalsIgnoreCase("CV")) {
            int space = price.indexOf(" ");
            String buyPrice = price.substring(0, space);
            String sellPrice = price.substring(space + 1);

            if (buyPrice.isEmpty() || sellPrice.isEmpty()) throw new Exception();

            // Max 16 chars
            signText = "§6C " + buyPrice + " §0/ §cV " + sellPrice;
        } else {
            throw new Exception();
        }

        this.creatableSign.setLine(2, signText);
    }

    public void setItemName(String itemName) {
        if (this.creatableSign != null) {
            this.creatableSign.setLine(3, itemName);
            this.creatableSign.getBlock().getState().update();
            return;
        }

        this.sign.setLine(3, itemName);
        this.sign.update();
    }

    public boolean withItemInShop() {
        if (this.sign != null) return !this.sign.getLine(3).equalsIgnoreCase("---");

        return !this.creatableSign.getLine(3).equalsIgnoreCase("---");
    }

    public boolean isShop() {
        if (this.sign != null) {
            String title = removeColorCodes(this.sign.getLine(0));
            String shopTypeAndPrice = removeColorCodes(this.sign.getLine(2));

            System.out.println(title + shopTypeAndPrice);
            FConsoleLogger.sendConsoleMessage(title + shopTypeAndPrice);

            return title.equalsIgnoreCase("loja") || matchShopTypePattern(shopTypeAndPrice);
        }

        String title = this.creatableSign.getLine(0);
        String shopTypeAndPrice = this.creatableSign.getLine(2);

        System.out.println(title + shopTypeAndPrice);
        FConsoleLogger.sendConsoleMessage(title + shopTypeAndPrice);

        return title.equalsIgnoreCase("loja") || matchShopTypePattern(shopTypeAndPrice);
    }

    private boolean matchShopTypePattern(String text) {
        // String regex = "^(C|V)\\s+\\d+$|^C\\s+\\d+\\s*/\\s*V\\s+\\d+$";
        String regex = "^([CV])\\s+\\d+$|^C\\s+\\d+\\s*/\\s*V\\s+\\d+$";
        return text.matches(regex);
    }

    // TODO: Move to fAPI
    private String removeColorCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("§.", "");
    }
}
