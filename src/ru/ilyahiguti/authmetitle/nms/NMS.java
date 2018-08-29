package ru.ilyahiguti.authmetitle.nms;

import org.bukkit.entity.Player;

public interface NMS {
    /**
     * Отправляет title игроку с расширенными параметрами.
     *
     * @param player   игрок
     * @param title    титр
     * @param subtitle субтитр
     * @param fadeIn   длительность появления
     * @param stay     длительность отображения
     * @param fadeOut  длительность исчезания
     */
    void sendTitle(Player player,
                   String title,
                   String subtitle,
                   int fadeIn,
                   int stay,
                   int fadeOut);
}