package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;

public class FrenziedSundial extends BaseRelic {
    // 使用 Mod 统一的 ID 生成方法
    public static final String ID = HoolheyakMod.makeID("FrenziedSundial");

    public FrenziedSundial() {
        super(
                ID,
                "FrenziedSundial", // 请确保图片路径为 HoolheyakResources/images/relics/Bibliotheca.png
                Hoolheyak.Meta.CARD_COLOR, // 绑定为霍尔海雅的专属遗物
                RelicTier.BOSS, // 基础（初始）遗物
                LandingSound.MAGICAL // 魔法音效，契合图书馆的神秘感
        );
    }

    // 装备时 +1 能量上限
    @Override
    public void onEquip() {
        AbstractDungeon.player.energy.energyMaster += 1;
    }

    // 卸下时 -1 能量上限
    @Override
    public void onUnequip() {
        AbstractDungeon.player.energy.energyMaster -= 1;
    }

    // 每回合开始时随机赋予一个相位
    @Override
    public void atTurnStart() {
        this.flash();
        PhaseManager.applyPhase(PhaseManager.getRandomPhase(AbstractDungeon.player));
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}