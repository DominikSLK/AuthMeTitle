package ru.ilyahiguti.authmetitle.vanish;

import org.bukkit.entity.Player;

public interface IVanish {
    void markAsUnlogged(Player player);

    void markAsLoggedIn(Player player);

    void onQuit(Player player);
}
