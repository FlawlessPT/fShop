package pt.flawless.fShop.shops;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Material;
import pt.flawless.fapi.economy.Amounts;

/** Oferta lida de uma placa de loja. Preços em cêntimos; 0 significa que a loja não faz essa operação. */
public record ShopOffer(int quantity, Material material, long buyCents, long sellCents) {

    // 36 espaços de inventário x 64 itens
    static final int MAX_QUANTITY = 2304;

    // preço: inteiro em euros, ou com sufixo k/M/B (decimais permitidos: 1,5k)
    private static final String PRICE = "([0-9]+(?:[.,][0-9]+)?[kKmMbB]|[0-9]+)";
    private static final Pattern BUY = Pattern.compile("^C\\s+" + PRICE + "$");
    private static final Pattern SELL = Pattern.compile("^V\\s+" + PRICE + "$");
    private static final Pattern BOTH = Pattern.compile("^C\\s+" + PRICE + "\\s*/\\s*V\\s+" + PRICE + "$");

    public boolean canBuy() {
        return buyCents > 0;
    }

    public boolean canSell() {
        return sellCents > 0;
    }

    public String itemName() {
        return material.name().toLowerCase().replace('_', ' ');
    }

    /** Lê as linhas 2, 3 e 4 da placa; vazio se a placa não der uma oferta válida. */
    public static Optional<ShopOffer> fromSign(String quantityLine, String priceLine, String itemLine) {
        String prices = strip(priceLine);
        String item = strip(itemLine);

        int quantity;
        try {
            quantity = Integer.parseInt(strip(quantityLine));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (quantity <= 0 || quantity > MAX_QUANTITY) {
            return Optional.empty();
        }

        Material material = item.equals("---") ? null : Material.matchMaterial(item);
        if (material == null || material == Material.AIR) {
            return Optional.empty();
        }

        long buy = 0;
        long sell = 0;
        try {
            Matcher both = BOTH.matcher(prices);
            Matcher buyOnly = BUY.matcher(prices);
            Matcher sellOnly = SELL.matcher(prices);
            if (both.matches()) {
                buy = cents(both.group(1));
                sell = cents(both.group(2));
            } else if (buyOnly.matches()) {
                buy = cents(buyOnly.group(1));
            } else if (sellOnly.matches()) {
                sell = cents(sellOnly.group(1));
            } else {
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (buy <= 0 && sell <= 0) {
            return Optional.empty();
        }
        return Optional.of(new ShopOffer(quantity, material, buy, sell));
    }

    private static long cents(String price) {
        return Amounts.parse(price).orElseThrow(NumberFormatException::new);
    }

    private static String strip(String line) {
        return line == null ? "" : line.replaceAll("§.", "").trim();
    }
}
