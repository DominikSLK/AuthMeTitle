package ru.ilyahiguti.authmetitle.title;

import org.bukkit.entity.Player;
import ru.ilyahiguti.authmetitle.AuthMeTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TitleAnimation {
    private final AuthMeTitle plugin;
    private List<Title> titleFrames = new ArrayList<>();
    private boolean repeat;
    private int repeatFrom;

    public TitleAnimation(List<String> animSTitleList, boolean repeat, int repeatFrom, AuthMeTitle plugin) throws TitleDescriptionException {
        this.repeat = repeat;
        this.repeatFrom = repeatFrom;
        this.plugin = plugin;
        for (String line : animSTitleList) {
            titleFrames.add(Title.parse(line));
        }
    }

    public void play(Player player, Predicate<Player> isNeedToStop) {
        new TitleShowTask(player, titleFrames, repeat, repeatFrom, isNeedToStop, plugin);
    }
}
