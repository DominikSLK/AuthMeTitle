package ru.ilyahiguti.authmetitle.listener;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.ilyahiguti.authmetitle.AuthMeTitle;

import java.util.HashMap;
import java.util.Map;

public class ExperienceListener implements Listener {
    private final AuthMeTitle plugin;
    private final int timeout;
    private final boolean getBackExp;
    private final float[] expCache;
    private Map<Player, ExpTask> expMap;

    public ExperienceListener(AuthMeTitle plugin, int timeout, boolean getBackExp) {
        this.plugin = plugin;
        this.getBackExp = getBackExp;
        this.expMap = new HashMap<>();
        this.timeout = timeout;

        float fTimeout = (float) timeout;
        this.expCache = new float[timeout];
        for (int i = 0; i < expCache.length; i++) {
            expCache[i] = (fTimeout - (fTimeout - i)) / fTimeout;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.expMap.put(event.getPlayer(), new ExpTask(event.getPlayer()));
    }

    @EventHandler
    public void onAuth(LoginEvent event) {
        restoreExp(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restoreExp(event.getPlayer());
    }

    private void restoreExp(Player player) {
        ExpTask expData = expMap.remove(player);
        if (expData != null) expData.restore();
    }

    private class ExpTask extends BukkitRunnable {
        private final Player player;
        private final int level;
        private final float exp;
        private int remainingTime = ExperienceListener.this.timeout;

        ExpTask(Player player) {
            if (getBackExp) {
                level = player.getLevel();
                exp = player.getExp();
            } else {
                level = 0;
                exp = 0;
            }
            this.player = player;
            this.runTaskTimer(plugin, 0, 20);
        }

        @Override
        public void run() {
            if (remainingTime < 1) {
                player.setLevel(0);
                player.setExp(0);
                cancel();
                return;
            }
            player.setLevel(remainingTime--);
            player.setExp(expCache[remainingTime]);
        }

        void restore() {
            cancel();
            player.setLevel(level);
            player.setExp(exp);
        }
    }
}
