package pt.flawless.fShop.managers;

import java.util.Optional;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.enums.ShopType;
import pt.flawless.fShop.shops.ShopOffer;

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
            signText = "§2C " + suffix(price);
        } else if (signShopType.equalsIgnoreCase("V")) {
            signText = "§cV " + suffix(price);
        } else if (signShopType.equalsIgnoreCase("CV")) {
            String[] parts = price.trim().split("\\s*/\\s*|\\s+");
            if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) throw new Exception();
            String buyPrice = parts[0];
            String sellPrice = parts[1];

            // Linha de placa: 15 chars no máximo (cores incluídas); mais é cortado e a loja deixa de ser reconhecida
            signText = "§2C " + suffix(buyPrice) + "/§cV " + suffix(sellPrice);
        } else {
            throw new Exception();
        }

        this.creatableSign.setLine(2, signText);
    }

    /** Sufixo do preço normalizado: k minúsculo, M e B maiúsculos. */
    private static String suffix(String price) {
        if (price.isEmpty()) return price;
        char last = price.charAt(price.length() - 1);
        String head = price.substring(0, price.length() - 1);
        return switch (last) {
            case 'K', 'k' -> head + 'k';
            case 'm', 'M' -> head + 'M';
            case 'b', 'B' -> head + 'B';
            default -> price;
        };
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

    /** Título de loja do servidor, ignorando cores e espaços; usado também na criação (permissão). */
    public static boolean isServerTitle(String line) {
        return line != null && removeColorCodes(line).trim().equalsIgnoreCase("loja");
    }

    public boolean isServerShop() {
        return isServerTitle(this.sign.getLine(0));
    }

    /** Loja do servidor: só quem tem {@code fshop.admin}. Loja de jogador: só o dono (nome na linha 1). */
    public boolean canEdit() {
        if (isServerShop()) {
            return this.player.hasPermission("fshop.admin");
        }
        return removeColorCodes(this.sign.getLine(0)).equalsIgnoreCase(this.player.getName());
    }

    public Optional<ShopOffer> offer() {
        return ShopOffer.fromSign(this.sign.getLine(1), this.sign.getLine(2), this.sign.getLine(3));
    }

    public boolean isShop() {
        if (this.sign != null) {
            String title = removeColorCodes(this.sign.getLine(0));
            String shopTypeAndPrice = removeColorCodes(this.sign.getLine(2));

            return title.equalsIgnoreCase("loja") || matchShopTypePattern(shopTypeAndPrice);
        }

        String title = this.creatableSign.getLine(0);
        String shopTypeAndPrice = this.creatableSign.getLine(2);

        return title.equalsIgnoreCase("loja") || matchShopTypePattern(shopTypeAndPrice);
    }

    private boolean matchShopTypePattern(String text) {
        // String regex = "^(C|V)\\s+\\d+$|^C\\s+\\d+\\s*/\\s*V\\s+\\d+$";
        String p = "([0-9]+(?:[.,][0-9]+)?[kKmMbB]?)";
        String regex = "^[CV]\\s+" + p + "$|^C\\s+" + p + "\\s*/\\s*V\\s+" + p + "$";
        return text.matches(regex);
    }

    // TODO: Move to fAPI
    private static String removeColorCodes(String input) {
        if (input == null) return null;
        return input.replaceAll("§.", "");
    }
}
