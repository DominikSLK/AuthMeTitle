package ru.ilyahiguti.authmetitle;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import ru.ilyahiguti.authmetitle.nms.NMS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AuthMeTitle extends JavaPlugin implements Listener {
    private AuthMeApi authMeApi;
    private NMS nms;
    private YamlConfiguration title_unregister;
    private YamlConfiguration title_login;
    private YamlConfiguration title_auth;
    private Collection<Player> unloggedVanishPlayers; /* Контейнер для логики скрытия игроков */
    private Map<String, ATitleManager> repeatingTasks = new HashMap<>();

    // TODO: Заоптимизировать это гавно

    @Override
    public void onEnable() {
        if (!setupNMS()) {
            getLogger().severe("Could not find support for this Bukkit version.");
            setEnabled(false);
            return;
        }
        authMeApi = AuthMeApi.getInstance();

        PluginManager pm = Bukkit.getPluginManager();

        loadTitleConfigs();
        checkCollector(true);

        pm.registerEvents(this, this);

        if (getConfig().getBoolean("Show_Timeout_In_Experience.Enabled")) {
            int timeout = pm.getPlugin("AuthMe").getConfig().getInt("settings.restrictions.timeout");
            if (timeout < 1) {
                getLogger().warning("Timeout disconnection is disabled in AuthMe config.");
                return;
            }
            pm.registerEvents(new ExperienceListener(this, timeout, getConfig().getBoolean("Show_Timeout_In_Experience.Save_Previous_Data")), this);
        }
    }

    /**
     * Инициализирует объект NMS для плагина.
     *
     * @return {@code true} если ядро сервера поддерживается плагином.
     */
    private boolean setupNMS() {
        try {
            nms = (NMS) Class.forName("ru.ilyahiguti.authmetitle.nms." + getServer().getClass().getPackage().getName().substring(23)).getConstructor().newInstance();
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private void loadTitleConfigs() {
        // Копируем дефолтные конфигурационные файлы, если таковых не найдено в папке плагина
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
        if (!new File(getDataFolder(), "title_unregister.yml").exists()) {
            saveResource("title_unregister.yml", false);
        }
        if (!new File(getDataFolder(), "title_login.yml").exists()) {
            saveResource("title_login.yml", false);
        }
        if (!new File(getDataFolder(), "title_auth.yml").exists()) {
            saveResource("title_auth.yml", false);
        }
        // Подгружаем конфиги тайтлов
        title_unregister = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_unregister.yml"));
        title_login = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_login.yml"));
        title_auth = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_auth.yml"));
    }


    private void checkCollector(boolean isFirstStart) {
        if (isFirstStart) {
            if (getConfig().getBoolean(("Hide_Players.Enabled")) && getConfig().getBoolean(("Hide_Players.Show_Players.Enabled"))) {
                createCollector();
            }
            if (!getConfig().getBoolean(("Hide_Players.Enabled")) || !getConfig().getBoolean(("Hide_Players.Show_Players.Enabled"))) {
                deleteCollector();
            }
        } else {
            if (unloggedVanishPlayers == null && getConfig().getBoolean(("Hide_Players.Enabled")) && getConfig().getBoolean(("Hide_Players.Show_Players.Enabled"))) {
                createCollector();
            }
            if (unloggedVanishPlayers != null && (!getConfig().getBoolean(("Hide_Players.Enabled")) || !getConfig().getBoolean(("Hide_Players.Show_Players.Enabled")))) {
                deleteCollector();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player joinedPlayer = event.getPlayer();

        if (unloggedVanishPlayers != null) {
            unloggedVanishPlayers.add(joinedPlayer);
        }

        if (getConfig().getBoolean("Tp_To_Spawn.Enabled")) {
            Location spawn;
            try {
                spawn = Bukkit.getWorld(getConfig().getString("Tp_To_Spawn.World")).getSpawnLocation();
                spawn.setYaw((float) getConfig().getDouble("Tp_To_Spawn.Yaw"));
                spawn.setPitch((float) getConfig().getDouble("Tp_To_Spawn.Pitch"));
            } catch (NullPointerException e) {
                getLogger().severe("World \"" + getConfig().getString("Tp_To_Spawn.World") + "\" does not found!");
                e.printStackTrace();
                return;
            }

            if (getConfig().getBoolean("Tp_To_Spawn.To_Center")) {
                joinedPlayer.teleport(spawn.add(0.5, 0, 0.5));
            } else {
                joinedPlayer.teleport(spawn);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            ATitleManager title;
            if (!authMeApi.isAuthenticated(joinedPlayer)) {
                if (authMeApi.isRegistered(joinedPlayer.getName())) {
                    if (title_login.getBoolean("Repeat.Enabled")) {
                        title = new TitleManagerRepeatingTask(this, nms, joinedPlayer, title_login);
                        repeatingTasks.put(event.getPlayer().getName(), title);
                    } else {
                        title = new TitleManagerTask(this, nms, joinedPlayer, title_login);
                    }
                } else {
                    if (title_unregister.getBoolean("Repeat.Enabled")) {
                        title = new TitleManagerRepeatingTask(this, nms, joinedPlayer, title_unregister);
                        repeatingTasks.put(event.getPlayer().getName(), title);
                    } else {
                        title = new TitleManagerTask(this, nms, joinedPlayer, title_unregister);
                    }
                }
                title.execute();
            }
        }, getConfig().getLong("Titles.Delay_In_Ticks"));

        if (getConfig().getBoolean("Hide_Players.Enabled")) {
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (unloggedVanishPlayers == null || unloggedVanishPlayers.contains(joinedPlayer)) {
                    joinedPlayer.hidePlayer(player);
                }
                if (unloggedVanishPlayers == null || unloggedVanishPlayers.contains(player)) {
                    player.hidePlayer(joinedPlayer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (unloggedVanishPlayers != null) {
            unloggedVanishPlayers.remove(event.getPlayer());
        }
        repeatingTasks.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        final Player loggedPlayer = event.getPlayer();
        repeatingTasks.remove(loggedPlayer.getName());
        new TitleManagerTask(this, nms, loggedPlayer, title_auth).execute();

        if (unloggedVanishPlayers != null) {
            unloggedVanishPlayers.remove(loggedPlayer);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    loggedPlayer.showPlayer(p);
                }
            }, getConfig().getLong("Hide_Players.Show_Players.Delay_In_Ticks"));
        }
    }

    private void createCollector() {
        unloggedVanishPlayers = new ArrayList<>();
    }

    private void deleteCollector() {
        unloggedVanishPlayers = null;
    }

    boolean containsInMap(ATitleManager task) {
        return repeatingTasks.containsValue(task);
    }

    void removeFromMap(String name) {
        repeatingTasks.remove(name);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            reloadConfig();
            loadTitleConfigs();
            checkCollector(false);
            getLogger().info("Config reloaded.");
        } else {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
        }
        return true;
    }
}