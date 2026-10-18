package pt.flawless.fShop.shops;

import java.util.List;
import java.util.function.LongFunction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.enums.FInventorySize;
import pt.flawless.fapi.inventory.FInventory;
import pt.flawless.fapi.items.FItemStack;

/**
 * Menu da loja: baú de 27 espaços com o item exato ao centro, o botão de compra à esquerda e o de venda à direita.
 * O holder identifica o menu (não se usa o título) e guarda a posição da placa.
 */
public final class ShopMenu implements InventoryHolder {

    public static final int SIZE = 27;
    public static final int BUY_SLOT = 11;
    public static final int ITEM_SLOT = 13;
    public static final int SELL_SLOT = 15;

    public enum Action { BUY, SELL, NONE }

    // data values da lã (WOOL)
    private static final short GREEN = 5;
    private static final short GRAY = 7;
    private static final short RED = 14;

    private final ShopKey key;
    private Inventory inventory;

    private ShopMenu(ShopKey key) {
        this.key = key;
    }

    public ShopKey key() {
        return key;
    }

    /** O bloco da placa; {@code null} se o mundo já não existir. */
    public Block block() {
        World world = Bukkit.getWorld(key.world());
        return world == null ? null : world.getBlockAt(key.x(), key.y(), key.z());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /** Só os dois botões fazem alguma coisa; slots do inventário de baixo (>= SIZE) e negativos são {@link Action#NONE}. */
    public static Action actionFor(int rawSlot) {
        return switch (rawSlot) {
            case BUY_SLOT -> Action.BUY;
            case SELL_SLOT -> Action.SELL;
            default -> Action.NONE;
        };
    }

    static List<String> buyLore(ShopOffer offer, String price) {
        return List.of("§7" + offer.quantity() + "x " + offer.itemName(), "§7Preço: §f" + price);
    }

    static List<String> sellLore(ShopOffer offer, String price) {
        return List.of("§7" + offer.quantity() + "x " + offer.itemName(), "§7Recebes: §f" + price);
    }

    public static Inventory create(ShopKey key, ShopOffer offer, ItemStack item, LongFunction<String> format) {
        ShopMenu holder = new ShopMenu(key);
        FInventory menu = new FInventory(holder, FInventorySize.SIZE_27, "Loja");
        Inventory inventory = menu.getInventory();
        holder.inventory = inventory;

        ItemStack shown = item.clone();
        shown.setAmount(Math.min(offer.quantity(), shown.getMaxStackSize()));
        inventory.setItem(ITEM_SLOT, shown);

        menu.setItem(BUY_SLOT, offer.canBuy()
                ? button(GREEN, "§aComprar", buyLore(offer, format.apply(offer.buyCents())))
                : button(GRAY, "§7Indisponível", List.of("§7Esta loja não permite a compra deste item.")));
        menu.setItem(SELL_SLOT, offer.canSell()
                ? button(RED, "§cVender", sellLore(offer, format.apply(offer.sellCents())))
                : button(GRAY, "§7Indisponível", List.of("§7Esta loja não permite a venda deste item.")));
        return inventory;
    }

    private static FItemStack button(short color, String name, List<String> lore) {
        return new FItemStack(Material.WOOL, 1, name).setDurability(color).setLore(lore);
    }
}
