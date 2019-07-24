package ru.ilyahiguti.authmetitle.title;

import org.bukkit.ChatColor;

public class Title {
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    private final int delayNext;

    public static Title parse(String configLine) throws TitleDescriptionException {
        String[] parts = configLine.split(" :: ");

        try {
            return new Title(
                    ChatColor.translateAlternateColorCodes('&', parts[0]),
                    ChatColor.translateAlternateColorCodes('&', parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]));
        } catch (Exception e) {
            throw new TitleDescriptionException("Wrong title description:" + configLine);
        }
    }

    private Title(String title, String subtitle, int fadeIn, int stay, int fadeOut, int delayNext) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        this.delayNext = delayNext;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public int getDelayNext() {
        return delayNext;
    }
}
