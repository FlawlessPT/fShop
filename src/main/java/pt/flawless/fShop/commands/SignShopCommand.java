package pt.flawless.fShop.commands;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fShop.managers.SignShopManager;
import pt.flawless.fShop.shops.PlayerShops;
import pt.flawless.fShop.shops.ShopItems;
import pt.flawless.fShop.shops.ShopKey;
import pt.flawless.fapi.messages.Messages;
import pt.flawless.fapi.sounds.FSound;

/** {@code /signshop resetitem}: limpa o item da placa que o jogador está a olhar, sem a partir. */
public class SignShopCommand implements CommandExecutor {
    private final ShopItems items;
    private final PlayerShops shops;

    public SignShopCommand(ShopItems items, PlayerShops shops) {
        this.items = items;
        this.shops = shops;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUso: /signshop help");
            return true;
        }
        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§e/signshop resetitem §7- Limpa o item da loja que estás a olhar");
            sender.sendMessage("§e/signshop help §7- Mostra esta lista");
            return true;
        }
        if (!args[0].equalsIgnoreCase("resetitem")) {
            sender.sendMessage("§cSubcomando desconhecido. Usa /signshop help");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cApenas jogadores podem usar este comando.");
            return true;
        }

        Block block = player.getTargetBlock((java.util.Set<org.bukkit.Material>) null, 5);
        if (block == null || !(block.getState() instanceof Sign sign)) {
            fail(player, "§cOlha para a placa da loja (a menos de 5 blocos).");
            return true;
        }

        SignShopManager manager = new SignShopManager(sign, player);
        if (!manager.isShop()) {
            fail(player, "§cEsta placa não é uma loja.");
            return true;
        }

        ShopKey key = ShopKey.of(block);
        boolean mayEdit = manager.isServerShop()
                ? player.hasPermission("fshop.admin")
                : shops.get(key).map(o -> o.uuid().equals(player.getUniqueId())).orElse(false);
        if (!mayEdit) {
            FSound.fail(player);
            Messages.noPermission(player);
            return true;
        }
        if (!manager.withItemInShop()) {
            fail(player, "§cEsta loja ainda não tem item.");
            return true;
        }

        manager.setItemName("---");
        items.remove(key);
        FSound.success(player);
        player.sendMessage("§eItem da loja removido. Clica com o botão direito com um item na mão para definir outro.");
        return true;
    }

    private static void fail(Player player, String message) {
        FSound.fail(player);
        player.sendMessage(message);
    }
}
