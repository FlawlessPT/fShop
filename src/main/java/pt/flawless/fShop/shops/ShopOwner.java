package pt.flawless.fShop.shops;

import java.util.UUID;

/** O dono de uma loja de jogador, tal como ficou guardado ao criá-la. */
public record ShopOwner(UUID uuid, String name) {
}
