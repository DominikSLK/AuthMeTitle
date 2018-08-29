package ru.ilyahiguti.authmetitle;

import org.bukkit.entity.Player;

class PlayerExp {
    private final Player player;
    private final int level;
    private final float exp;

    PlayerExp(Player player) {
        this.player = player;
        level = player.getLevel();
        exp = player.getExp();
    }

    void getBack() {
        player.setLevel(level);
        player.setExp(exp);
    }
}
