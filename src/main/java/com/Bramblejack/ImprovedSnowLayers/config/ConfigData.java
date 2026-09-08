package com.Bramblejack.ImprovedSnowLayers.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigData {
    public List<String> whitelist = new ArrayList<>();
    public List<String> blacklist = new ArrayList<>();
    public SnowCheckerMode checkerMode = SnowCheckerMode.THREE_OF_FOUR;
    public boolean reduceZFighting = true;
    public boolean useRaySearch = false;
    public int maxHorizontalDistance = 1;
    public int maxVerticalDistance = 2;
}
