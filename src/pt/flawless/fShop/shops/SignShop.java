package pt.flawless.fShop.shops;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import pt.flawless.fShop.managers.MessagesManager;
import pt.flawless.fapi.items.FItemStack;
import pt.flawless.fapi.sounds.FSound;


public class SignShop {
    private SignChangeEvent sign;
    private Player player;

    public SignShop(SignChangeEvent sign, Player player) {
        this.sign = sign;
        this.player = player;
    }

    private void setTitle(ShopType shopType) {
        if (shopType == ShopType.PlayerShop) {
            this.sign.setLine(0, "§9" + player.getName() + "");
        } else {
            this.sign.setLine(0, "§1§lLoja");
        }
    }

    private void setQuantity(String quantity) {
        this.sign.setLine(1, "§l" + quantity);
    }

    // TODO: Create new exception
    private void setSignShopType(String signShopType, String price) throws Exception {
        String signText;

        if (signShopType.equalsIgnoreCase("C")) {
            signText = "§2C " + price;
        } else if (signShopType.equalsIgnoreCase("V")) {
            signText = "§cV " + price;
        } else if (signShopType.equalsIgnoreCase("CV")) {
            int space = price.indexOf(" ");
            String buyPrice = price.substring(0, space);
            String sellPrice = price.substring(space + 1);
            player.sendMessage(space + "-" + buyPrice + "-" + sellPrice + "-" + price.substring(space + 1));

            // Max 16 chars
            signText = "§6C " + buyPrice + " §0/ §cV " + sellPrice;
        } else {
            throw new Exception(price);
            //player.sendMessage(MessagesManager.shopError);
        }

        this.sign.setLine(2, signText);
    }

    private void verifyErrors(String shopType) {
        if (this.sign.getLine(1).equals("")) {
            this.sign.getBlock().breakNaturally();
            player.sendMessage(MessagesManager.quantiyMissing);
            FSound.fail(player);
        }
        if (this.sign.getLine(2).equals("")) {
            this.sign.getBlock().breakNaturally();
            player.sendMessage(MessagesManager.priceMissing);
            FSound.fail(player);
        }
        if (shopType.equals("")) {
            player.getWorld().dropItemNaturally(player.getLocation(), new FItemStack(Material.SIGN, 1).createItem());
            player.sendMessage(MessagesManager.shopTypeMissing);
            FSound.fail(player);
        }
    }

    public void createPlayerShop() {
        String quantity = this.sign.getLine(1);
        String signShopType = this.sign.getLine(2);
        String price = this.sign.getLine(3);

        if (!(this.sign.getLine(1).equals("")) && !(this.sign.getLine(2).equals("")) && !(signShopType.equals(""))) {
            try {
                setTitle(ShopType.PlayerShop);
                setQuantity(quantity);
                setSignShopType(signShopType, price);
                this.sign.setLine(3, "---");

                this.sign.getBlock().getState().update();

                FSound.success(player);
                player.sendMessage(MessagesManager.shopCreated);
            } catch (Exception e1) {
                this.sign.getBlock().breakNaturally();
                player.sendMessage("$cErro ao criar loja. " + "§o(Valor inserido: " + e1.getLocalizedMessage().substring(18) + ")");
                //player.sendMessage(MessagesManager.invalidArgument + " §o(Valor inserido: " + e1.getLocalizedMessage().substring(18) + ")");
                FSound.fail(player);
            }
        } else {
            player.sendMessage("$cErro ao criar loja.");

            //verifyErrors(signShopType);
        }
    }

    public void createServerShop() {
        try {
            String serverShop = this.sign.getLine(0);
            String quantity = this.sign.getLine(1);
            String signShopType = this.sign.getLine(2);
            String price = this.sign.getLine(3);

            //if (quantity.length() > 0 && signShopType.length() > 0 && signShopType.length() > 0) {
            try {
                setTitle(ShopType.ServerShop);
                setQuantity(quantity);
                setSignShopType(signShopType, price);
                this.sign.setLine(3, "---");

                this.sign.getBlock().getState().update();

                FSound.success(player);
                //player.sendMessage(MessagesManager.shopCreated);
                player.sendMessage("§eLoja criada com sucesso.");
            } catch (Exception e1) {
                this.sign.getBlock().breakNaturally();
                player.sendMessage("§cErro ao criar loja.");
                //player.sendMessage("$cErro ao criar loja. " + "§o(Valor inserido: " + e1.getLocalizedMessage().substring(18) + ")");
                //player.sendMessage(MessagesManager.invalidArgument + " §o(Valor inserido: " + e1.getLocalizedMessage().substring(18) + ")");
                FSound.fail(player);
            }
//            } else {
//                sign.getBlock().breakNaturally();
//                FSound.fail(player);
//                player.sendMessage("§cErro ao criar loja.");
//            }
        } catch (Exception e) {
            player.sendMessage("$cErro ao criar loja." + e.getMessage());
            sign.getBlock().breakNaturally();
        }
    }

    public void insertItemName(Material itemInHand) {
        //        sign.setLine(3, ChatColor.DARK_PURPLE + player.getItemInHand().getType().toString());
        //        sign.setLine(3, player.getItemInHand().getType().toString());
        //        sign.update();

        if (!hasItem()) {
            //SignsManager.updateLine(this.sign, 3, itemInHand.toString());
        }
    }

    public boolean hasItem() {
        if (this.sign.getLine(3).equalsIgnoreCase("---")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasEnchantments() {
        if (player.getItemInHand().getEnchantments().toString().equalsIgnoreCase("")) {
            return false;
        } else {
            return true;
        }
    }

    public SignChangeEvent getSign() {
        return this.sign;
    }

    public void setSign(SignChangeEvent sign) {
        this.sign = sign;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private enum ShopType {
        ServerShop, PlayerShop
    }
}
