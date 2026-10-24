package pt.flawless.fShop.shops;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.inventory.ItemStack;
import pt.flawless.fapi.database.DatabaseService;

/** Tabela {@code fshop_items}. Linhas cujo item não se consegue descodificar são ignoradas no carregamento. */
public final class SqlShopItemRepository implements ShopItemRepository {

    private static final String LOAD = "SELECT world, x, y, z, item FROM fshop_items";
    private static final String SAVE =
            "INSERT INTO fshop_items (world, x, y, z, item) VALUES (?, ?, ?, ?, ?) AS new "
                    + "ON DUPLICATE KEY UPDATE item = new.item";
    private static final String DELETE = "DELETE FROM fshop_items WHERE world = ? AND x = ? AND y = ? AND z = ?";

    private final DatabaseService database;

    public SqlShopItemRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<Map<ShopKey, ItemStack>> loadAll() {
        return database.supplyAsync(connection -> {
            Map<ShopKey, ItemStack> items = new HashMap<>();
            try (PreparedStatement ps = connection.prepareStatement(LOAD); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShopKey key = new ShopKey(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                    ItemCodec.decode(rs.getString(5)).ifPresent(item -> items.put(key, item));
                }
            }
            return items;
        });
    }

    @Override
    public CompletableFuture<Void> save(ShopKey key, ItemStack item) {
        String encoded = ItemCodec.encode(item);
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SAVE)) {
                ps.setString(1, key.world());
                ps.setInt(2, key.x());
                ps.setInt(3, key.y());
                ps.setInt(4, key.z());
                ps.setString(5, encoded);
                ps.executeUpdate();
            }
        });
    }

    @Override
    public CompletableFuture<Void> delete(ShopKey key) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(DELETE)) {
                ps.setString(1, key.world());
                ps.setInt(2, key.x());
                ps.setInt(3, key.y());
                ps.setInt(4, key.z());
                ps.executeUpdate();
            }
        });
    }
}
