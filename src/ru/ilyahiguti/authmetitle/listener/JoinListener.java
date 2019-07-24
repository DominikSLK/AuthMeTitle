package ru.ilyahiguti.authmetitle.listener;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.ilyahiguti.authmetitle.AuthMeTitle;
import ru.ilyahiguti.authmetitle.vanish.VanishManager;

public class JoinListener implements Listener {
    private AuthMeTitle plugin;
    private boolean invisibility;
    private long delayOnJoin;
    private Location spawn = null;

    public JoinListener(AuthMeTitle plugin) {
        this.plugin = plugin;
        this.invisibility = plugin.getConfig().getBoolean("Invisibility.Enabled");
        this.delayOnJoin = plugin.getConfig().getLong("Titles.Delay_In_Ticks");

        try {
            if (plugin.getConfig().getBoolean("Tp_To_Spawn.Enabled")) {

                String worldName = plugin.getConfig().getString("Tp_To_Spawn.World");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new RuntimeException("World " + worldName + " not found");
                }

                spawn = world.getSpawnLocation();

                spawn.setYaw((float) plugin.getConfig().getDouble("Tp_To_Spawn.Yaw"));
                spawn.setPitch((float) plugin.getConfig().getDouble("Tp_To_Spawn.Pitch"));

                if (plugin.getConfig().getBoolean("Tp_To_Spawn.To_Center")) {
                    spawn.add(0.5, 0, 0.5);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load \"Tp_To_Spawn\" settings: " + e.getMessage());
            spawn = null;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (invisibility) event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

        VanishManager.markAsUnlogged(player);

        if (spawn != null) {
            player.teleport(spawn);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getAuthMeApi().isAuthenticated(player)) return;

            if (plugin.getAuthMeApi().isRegistered(player.getName())) {
                plugin.getUnloggedAnimation().play(player, p -> plugin.getAuthMeApi().isAuthenticated(player));
            } else {
                plugin.getUnregisteredAnimation().play(player, p -> plugin.getAuthMeApi().isAuthenticated(player));
            }
        }, delayOnJoin);
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (invisibility) event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        VanishManager.markAsLoggedIn(event.getPlayer());
        plugin.getAuthorizedAnimation().play(event.getPlayer(), p -> false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (invisibility) event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
    }
}
