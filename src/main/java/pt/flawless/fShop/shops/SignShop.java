package pt.flawless.fShop.shops;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignShop {
    private SignChangeEvent creatableSign;
    private Sign sign;
    private Player player;

    public SignShop(SignChangeEvent sign, Player player) {
        this.creatableSign = sign;
        this.player = player;
    }

    public SignShop(Sign sign, Player player) {
        this.sign = sign;
        this.player = player;
    }

    public SignChangeEvent getCreatableSign() {
        return this.creatableSign;
    }

    public void setCreatableSign(SignChangeEvent creatableSign) {
        this.creatableSign = creatableSign;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }
}

