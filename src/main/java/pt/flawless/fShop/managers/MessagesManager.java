package pt.flawless.fShop.managers;

import pt.flawless.fShop.FShop;

public class MessagesManager
{
    //GENERAL STRINGS

    public static String playerOff = (String) FShop.plugin.getConfig().get("Mensagens.playeroff");
    public static String playersOnly = (String) FShop.plugin.getConfig().get("Mensagens.playersOnly");

    //SHOP STRINGS
    public static String invalidArgument = (String) FShop.plugin.getConfig().get("Mensagens.invalidArgument");
    public static String quantiyMissing = (String) FShop.plugin.getConfig().get("Mensagens.quantityMissing");
    public static String priceMissing = (String) FShop.plugin.getConfig().get("Mensagens.priceMissing");
    public static String shopTypeMissing = (String) FShop.plugin.getConfig().get("Mensagens.shopTypeMissing");
    public static String shopError = (String) FShop.plugin.getConfig().get("Mensagens.shopError");
    public static String shopCreated = (String) FShop.plugin.getConfig().get("Mensagens.shopCreated");

    //ITEMADD STRING

    public static String usageItemAdd = "§cArgumentos inválidos: /itemadd (preco)";
    public static String itemAdded = "§eItem §f(" + VariablesManager.item + "§f) §eadicionado por §f" + VariablesManager.price;
}
