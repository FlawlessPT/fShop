package pt.flawless.fShop.shops;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import pt.flawless.fapi.database.DatabaseService;

/** Tabela {@code fshop_shops}. Linhas com um UUID inválido são ignoradas no carregamento. */
public final class SqlPlayerShopRepository implements PlayerShopRepository {

    private static final String LOAD = "SELECT world, x, y, z, owner_uuid, owner_name FROM fshop_shops";
    private static final String SAVE =
            "INSERT INTO fshop_shops (world, x, y, z, owner_uuid, owner_name) VALUES (?, ?, ?, ?, ?, ?) AS new "
                    + "ON DUPLICATE KEY UPDATE owner_uuid = new.owner_uuid, owner_name = new.owner_name";
    private static final String DELETE = "DELETE FROM fshop_shops WHERE world = ? AND x = ? AND y = ? AND z = ?";

    private final DatabaseService database;

    public SqlPlayerShopRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<Map<ShopKey, ShopOwner>> loadAll() {
        return database.supplyAsync(connection -> {
            Map<ShopKey, ShopOwner> shops = new HashMap<>();
            try (PreparedStatement ps = connection.prepareStatement(LOAD); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ShopKey key = new ShopKey(rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                    try {
                        shops.put(key, new ShopOwner(UUID.fromString(rs.getString(5)), rs.getString(6)));
                    } catch (IllegalArgumentException invalidUuid) {
                        // linha corrompida: ignora-se, a loja fica como "antiga"
                    }
                }
            }
            return shops;
        });
    }

    @Override
    public CompletableFuture<Void> save(ShopKey key, ShopOwner owner) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SAVE)) {
                ps.setString(1, key.world());
                ps.setInt(2, key.x());
                ps.setInt(3, key.y());
                ps.setInt(4, key.z());
                ps.setString(5, owner.uuid().toString());
                ps.setString(6, owner.name());
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
