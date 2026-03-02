package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;

public class BottledCloud extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("BottledCloud");

    public BottledCloud() {
        super(ID, "BottledCloud", RelicTier.UNCOMMON, LandingSound.MAGICAL);
    }
}