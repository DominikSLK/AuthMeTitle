package ru.ilyahiguti.authmetitle.nms;

import org.bukkit.entity.Player;

public class BukkitImpl implements NMS {
    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
