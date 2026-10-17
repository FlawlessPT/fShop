package pt.flawless.fShop;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assumptions;
import pt.flawless.fapi.database.DatabaseService;

/** Ajudante dos testes de integração: só corre contra a base literalmente chamada flawless_test. */
public final class TestDb {

    private static final String[] VARS = {"FLAWLESS_DB_HOST", "FLAWLESS_DB_PORT", "FLAWLESS_DB_NAME",
            "FLAWLESS_DB_USER", "FLAWLESS_DB_PASSWORD"};

    private TestDb() {
    }

    /** Ignora o teste se faltarem variáveis ou se a base não for flawless_test (os testes fazem DROP). */
    public static TestDatabaseService service() {
        for (String v : VARS) {
            String value = System.getenv(v);
            Assumptions.assumeTrue(value != null && !value.isEmpty(), v + " não definida");
        }
        Assumptions.assumeTrue("flawless_test".equals(System.getenv("FLAWLESS_DB_NAME")),
                "FLAWLESS_DB_NAME não é flawless_test");
        return new TestDatabaseService(System.getenv("FLAWLESS_DB_HOST"), System.getenv("FLAWLESS_DB_PORT"),
                System.getenv("FLAWLESS_DB_NAME"), System.getenv("FLAWLESS_DB_USER"),
                System.getenv("FLAWLESS_DB_PASSWORD"));
    }

    /** Apaga só fshop_items (nunca schema_version) e recria-a com o ficheiro V001. */
    public static void resetSchema(DatabaseService database) throws Exception {
        String ddl = readMigration().trim();
        if (ddl.endsWith(";")) {
            ddl = ddl.substring(0, ddl.length() - 1);
        }
        String create = ddl;
        database.runAsync(connection -> {
            try (Statement st = connection.createStatement()) {
                st.execute("DROP TABLE IF EXISTS fshop_items");
                st.execute(create);
            }
        }).get(30, TimeUnit.SECONDS);
    }

    private static String readMigration() {
        try (InputStream in = TestDb.class.getResourceAsStream("/db/fshop/V001__create_shop_items.sql")) {
            if (in == null) {
                throw new IllegalStateException("V001 não encontrado no classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
