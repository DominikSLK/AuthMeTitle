package ru.ilyahiguti.authmetitle.vanish;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.ilyahiguti.authmetitle.AuthMeTitle;

import java.util.HashSet;
import java.util.Set;

public class TempVanish implements IVanish {
    private final AuthMeTitle plugin;
    private final long delayBeforeShow;
    private Set<Player> unlogged = new HashSet<>();

    public TempVanish(AuthMeTitle plugin, long delayBeforeShow) {
        this.plugin = plugin;
        this.delayBeforeShow = delayBeforeShow;
    }

    @Override
    public void markAsUnlogged(Player player) {
        unlogged.add(player);
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(otherPlayer);
            otherPlayer.hidePlayer(player);
        }
    }

    @Override
    public void markAsLoggedIn(Player player) {
        unlogged.remove(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (unlogged.contains(otherPlayer)) continue;
                player.showPlayer(otherPlayer);
                otherPlayer.showPlayer(player);
            }

        }, delayBeforeShow);
    }

    @Override
    public void onQuit(Player player) {
        unlogged.remove(player);
    }
}
