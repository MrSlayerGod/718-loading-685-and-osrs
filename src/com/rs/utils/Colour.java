package com.rs.utils;

/**
 * @author Simplex
 * @since Jul 19, 2020
 */
public enum Colour {

    RED("FF0000"), //todo figure out what rs uses (changed for color blindness)
    YELLOW("FFFF00"), //todo figure out what rs uses (changed for color blindness)
    GREEN("00FF00"), //todo figure out what rs uses (changed for color blindness)

    ORANGE("FFA500"),
    ORANGE_RED("FF4500"),
    TOMATO("FF6347"),
    CRIMSON("DC143C"),

    BLUE("0000FF"),
    COOL_BLUE("0040ff"),
    BABY_BLUE("1E90FF"),
    CYAN("00FFFF"),

    PURPLE("800080"),
    VIOLET("EE82EE"),
    PINK("FFC0CB"),

    WHITE("FFFFFF"),
    WHEAT("F5DEB3"),
    SILVER("C0C0C0"),

    OLIVE("808000"),
    BRONZE("D37E2A"),
    GOLD("FFD700"),

    DARK_RED("6f0000"),
    DARK_GREEN("006600"),

    RAID_PURPLE("ef20ff"),

    STRIKE("str");

    public final String hex;

    Colour(String hex) {
        this.hex = hex;
    }

    public String tag() {
        return this == STRIKE ? "<str>" : "<col=" + hex + ">";
    }

    public String wrap(int s) {
        return wrap("" + s);
    }
    public String wrap(String s) {
        return tag() + s + (this == STRIKE ? "</str>" : "</col>");
    }

}