package edu.westminsteru.cmpt328.cachesim;

public enum Version {

    V_2021_0("2021.0"),
    V_2021_1("2021.1"),
    V_2022_0("2022.0"),
    V_2022_1("2022.1"),
    V_2022_2("2022.2"),
    V_2024_0("2024.0"),
    V_2026_0("2026.0");

    private final String tag;

    Version(String tag) {
        this.tag = tag;
    }

    public String tag() {
        return tag;
    }

    public static Version latest() {
        return Version.values()[Version.values().length - 1];
    }
}
