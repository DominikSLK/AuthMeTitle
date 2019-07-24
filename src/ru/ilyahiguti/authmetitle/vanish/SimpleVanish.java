package ru.ilyahiguti.authmetitle.vanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SimpleVanish implements IVanish {
    @Override
    public void markAsUnlogged(Player player) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(otherPlayer);
            otherPlayer.hidePlayer(player);
        }
    }

    @Override
    public void markAsLoggedIn(Player player) {

    }

    @Override
    public void onQuit(Player player) {

    }
}
