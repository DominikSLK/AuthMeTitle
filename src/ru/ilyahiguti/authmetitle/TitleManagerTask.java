package ru.ilyahiguti.authmetitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.ilyahiguti.authmetitle.nms.NMS;

import java.util.List;

class TitleManagerTask extends ATitleManager {
    private AuthMeTitle plugin;
    private NMS nms;
    private Player player;
    private int timer = 0;
    private List<String> list;

    TitleManagerTask(AuthMeTitle plugin, NMS nms, Player player, YamlConfiguration configuration) {
        this.plugin = plugin;
        this.nms = nms;
        this.player = player;
        this.list = configuration.getStringList("Title");
    }

    @Override
    void execute() {
        if (timer >= list.size()) {
            return;
        }

        String[] parts = list.get(timer).split(" :: ");

        int speed = Integer.parseInt(parts[5]);
        nms.sendTitle(player,
                      ChatColor.translateAlternateColorCodes('&', parts[0]),
                      ChatColor.translateAlternateColorCodes('&', parts[1]),
                      Integer.parseInt(parts[2]),
                      Integer.parseInt(parts[3]),
                      Integer.parseInt(parts[4]));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            timer++;
            execute();
        }, speed);
    }
}
