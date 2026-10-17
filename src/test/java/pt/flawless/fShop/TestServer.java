package pt.flawless.fShop;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import sun.misc.Unsafe;

/** Servidor falso mínimo: só serve o ItemFactory, o suficiente para ItemMeta/isSimilar nos testes. */
public final class TestServer {

    private TestServer() {
    }

    public static synchronized void install() {
        if (Bukkit.getServer() != null) {
            return;
        }
        try {
            // Um Proxy de Server não funciona: o Server do Spigot 1.8 tem dois getOnlinePlayers() com retornos
            // incompatíveis. Alocamos um CraftServer sem correr o construtor; getItemFactory() não usa estado.
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Server server = (Server) ((Unsafe) unsafeField.get(null)).allocateInstance(CraftServer.class);
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(null, server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
