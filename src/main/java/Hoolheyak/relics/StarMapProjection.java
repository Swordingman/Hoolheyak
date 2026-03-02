package Hoolheyak.relics; // 记得改成你实际的包名

import Hoolheyak.HoolheyakMod; // 确保导入了你的核心类
import Hoolheyak.character.Hoolheyak; // 导入你的角色类
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class StarMapProjection extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("StarMapProjection");

    public StarMapProjection() {
        super(
                ID,
                "StarMapProjection",
                Hoolheyak.Meta.CARD_COLOR,
                RelicTier.BOSS,
                LandingSound.MAGICAL
        );
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}