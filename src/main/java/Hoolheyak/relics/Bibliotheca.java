package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class Bibliotheca extends BaseRelic {
    // 使用 Mod 统一的 ID 生成方法
    public static final String ID = HoolheyakMod.makeID("Bibliotheca");

    public Bibliotheca() {
        super(
                ID,
                "Bibliotheca", // 请确保图片路径为 HoolheyakResources/images/relics/Bibliotheca.png
                Hoolheyak.Meta.CARD_COLOR, // 绑定为霍尔海雅的专属遗物
                RelicTier.STARTER, // 基础（初始）遗物
                LandingSound.MAGICAL // 魔法音效，契合图书馆的神秘感
        );
    }

    // 在每场战斗开始时触发
    @Override
    public void atBattleStart() {
        // 遗物闪烁提示玩家触发了效果
        this.flash();

        // 给玩家施加 2 层解析
        addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new AnalysisPower(AbstractDungeon.player, 2), 2));
    }
}