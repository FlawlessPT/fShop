package pt.flawless.fShop.managers;

import org.bukkit.block.Sign;

public class SignsManager
{
    public static void updateLine(Sign sign, int line, String text)
    {
        sign.setLine(line, text);
        sign.update();
    }
}
