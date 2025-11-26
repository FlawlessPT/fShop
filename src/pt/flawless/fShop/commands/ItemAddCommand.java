package pt.flawless.fShop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ItemAddCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
//        if (sender instanceof Player)
//        {
//            Player p = (Player) sender;
//            BGroups g = BGroups.GROUP_ADMIN;
//            if (BGroup.hasPermission(g, p))
//            {
//                if (args.length == 0)
//                {
//                    p.sendMessage(MessagesManager.usageItemAdd);
//                    VariablesManager.fail.send(p);
//                    return false;
//                }
//                if (args.length == 1)
//                {
//                    int preco = Integer.parseInt(args[0]);
//                    Bukkit.broadcastMessage("4º " + p.getItemInHand().getTypeId() + ":" + p.getItemInHand().getDurability());
//                    p.sendMessage(MessagesManager.itemAdded
//                            .replace(VariablesManager.item, p.getItemInHand().getData().toString())
//                            .replace(VariablesManager.price, preco + ""));
//                    VariablesManager.sucess.send(p);
//                    BarberiansShop.plugin.getConfig().addDefault("Precos." + p.getItemInHand().getType().toString(), preco);
//                    BarberiansShop.plugin.getConfig().options().copyDefaults(true);
//                    BarberiansShop.plugin.saveConfig();
//
//                    return true;
//                }
//            }
//        }
//        else
//        {
//            sender.sendMessage(MessagesManager.playersOnly);
//            return false;
//        }
        return false;
    }
}
