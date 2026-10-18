CREATE TABLE IF NOT EXISTS fshop_shops (
    world VARCHAR(64) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    owner_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    owner_name VARCHAR(16) NOT NULL,
    PRIMARY KEY (world, x, y, z)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
