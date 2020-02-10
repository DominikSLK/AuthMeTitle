package ru.ilyahiguti.authmetitle;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.SpigotConfig;
import ru.ilyahiguti.authmetitle.listener.ChatCommandListener;
import ru.ilyahiguti.authmetitle.listener.ExperienceListener;
import ru.ilyahiguti.authmetitle.listener.JoinListener;
import ru.ilyahiguti.authmetitle.listener.LogoutKickListener;
import ru.ilyahiguti.authmetitle.nms.BukkitImpl;
import ru.ilyahiguti.authmetitle.nms.NMS;
import ru.ilyahiguti.authmetitle.title.TitleAnimation;
import ru.ilyahiguti.authmetitle.title.TitleDescriptionException;
import ru.ilyahiguti.authmetitle.vanish.VanishManager;

import java.io.File;
import java.util.List;

public class AuthMeTitle extends JavaPlugin {
    private AuthMeApi authMeApi;
    private NMS nms;

    private TitleAnimation unregisteredAnimation;
    private TitleAnimation unloggedAnimation;
    private TitleAnimation authorizedAnimation;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("AuthMe") == null || !Bukkit.getPluginManager().getPlugin("AuthMe").isEnabled()) {
            getLogger().severe("AuthMe not found on this server!");
            setEnabled(false);
            return;
        }

        if (!setupNMS()) {
            getLogger().severe("Could not find support for this Bukkit version (" + getServer().getClass().getPackage().getName().substring(23) + ")");
            setEnabled(false);
            return;
        }

        authMeApi = AuthMeApi.getInstance();

        loadPlugin();
    }

    private boolean setupNMS() {
        try {
            Class<?> nmsClass = Class.forName("ru.ilyahiguti.authmetitle.nms." + getServer().getClass().getPackage().getName().substring(23));

            nms = (NMS) nmsClass.getConstructor().newInstance();
            return true;
        } catch (ClassNotFoundException e) {
            try {
                Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
                nms = BukkitImpl.class.getConstructor().newInstance();
                return true;
            } catch (Exception ex) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private void loadPlugin() {
        HandlerList.unregisterAll(this);

        reloadConfig();
        loadTitleConfigs();

        VanishManager.init(this, getConfig().getBoolean(("Hide_Players.Enabled")), getConfig().getBoolean(("Hide_Players.Show_Players.Enabled")), getConfig().getLong("Hide_Players.Show_Players.Delay_In_Ticks"));

        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        if (getConfig().getBoolean("Kick_On_Logout_Or_Unregister.Enabled")) {
            Bukkit.getPluginManager().registerEvents(new LogoutKickListener(getConfig().getString("Kick_On_Logout_Or_Unregister.Message", "§cYou have successfully logged out.")), this);
        }

        if (getConfig().getBoolean("Show_Timeout_In_Experience.Enabled")) {
            int timeout = Bukkit.getPluginManager().getPlugin("AuthMe").getConfig().getInt("settings.restrictions.timeout");
            if (timeout < 1) {
                getLogger().warning("Timeout disconnection is disabled in the AuthMe config.");
            } else {
                Bukkit.getPluginManager().registerEvents(new ExperienceListener(this, timeout, getConfig().getBoolean("Show_Timeout_In_Experience.Save_Previous_Data")), this);
            }
        }

        if (getConfig().getBoolean("Chat_Auth.Enabled")) {
            boolean isAllowedChat = Bukkit.getPluginManager().getPlugin("AuthMe").getConfig().getBoolean("settings.restrictions.allowChat");
            if (!isAllowedChat) {
                getLogger().warning("Set \"settings.restrictions.allowChat\" parameter to \"true\" in the Authme config.yml");
            }
            Bukkit.getPluginManager().registerEvents(new ChatCommandListener(this), this);
        }
    }

    private void loadTitleConfigs() {
        if (!new File(getDataFolder(), "config.yml").exists()) saveResource("config.yml", false);
        if (!new File(getDataFolder(), "title_unregister.yml").exists()) saveResource("title_unregister.yml", false);
        if (!new File(getDataFolder(), "title_login.yml").exists()) saveResource("title_login.yml", false);
        if (!new File(getDataFolder(), "title_auth.yml").exists()) saveResource("title_auth.yml", false);

        YamlConfiguration title_unregister = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_unregister.yml"));
        YamlConfiguration title_login = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_login.yml"));
        YamlConfiguration title_auth = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "title_auth.yml"));

        try {
            List<String> titleSList = title_unregister.getStringList("Title");
            boolean repeat = title_unregister.getBoolean("Repeat.Enabled");
            int repeatFrom = title_unregister.getInt("Repeat.Start_From");

            if (repeatFrom > (titleSList.size() - 1)) {
                repeatFrom = 0;
                getLogger().warning("Repeat_From value is greater than the title frames in Unregister Title, will use 0 index.");
            }

            unregisteredAnimation = new TitleAnimation(titleSList, repeat, repeatFrom, this);
        } catch (TitleDescriptionException e) {
            getLogger().severe("An error occurred while loading the Unregister Title: " + e.getMessage());
        }

        try {
            List<String> titleSList = title_login.getStringList("Title");
            boolean repeat = title_login.getBoolean("Repeat.Enabled");
            int repeatFrom = title_login.getInt("Repeat.Start_From");

            if (repeatFrom > (titleSList.size() - 1)) {
                repeatFrom = 0;
                getLogger().warning("Repeat_From value is greater than the title frames in Login Title, will use 0 index.");
            }

            unloggedAnimation = new TitleAnimation(titleSList, repeat, repeatFrom, this);
        } catch (TitleDescriptionException e) {
            getLogger().severe("An error occurred while loading the Login Title: " + e.getMessage());
        }

        try {
            List<String> titleSList = title_auth.getStringList("Title");

            authorizedAnimation = new TitleAnimation(titleSList, false, 0, this);
        } catch (TitleDescriptionException e) {
            getLogger().severe("An error occurred while loading the Auth Title: " + e.getMessage());
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            loadPlugin();
            getLogger().info("Config reloaded.");
        } else {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
        }
        return true;
    }

    public NMS getNMS() {
        return nms;
    }

    public AuthMeApi getAuthMeApi() {
        return authMeApi;
    }

    public TitleAnimation getUnregisteredAnimation() {
        return unregisteredAnimation;
    }

    public TitleAnimation getUnloggedAnimation() {
        return unloggedAnimation;
    }

    public TitleAnimation getAuthorizedAnimation() {
        return authorizedAnimation;
    }
}