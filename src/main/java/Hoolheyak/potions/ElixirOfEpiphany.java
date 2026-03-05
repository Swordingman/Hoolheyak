package Hoolheyak.potions;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class ElixirOfEpiphany extends BasePotion {
    public static final String POTION_ID = HoolheyakMod.makeID("ElixirOfEpiphany");

    public ElixirOfEpiphany() {
        super(
                POTION_ID,
                1, // 基础效力
                PotionRarity.UNCOMMON,
                PotionSize.SPHERE,
                Color.CHARTREUSE,
                Color.BLUE,
                null
        );
        this.isThrown = false;       // 不需要丢向敌人
        this.targetRequired = false; // 不需要目标
    }

    @Override
    public String getDescription() {
        return DESCRIPTIONS[0] + (this.potency * 2) + DESCRIPTIONS[1] + this.potency + DESCRIPTIONS[2];
    }

    @Override
    public void use(AbstractCreature target) {
        AbstractCreature p = AbstractDungeon.player;

        // 1. 获得解析
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.potency * 2), this.potency * 2));

        // 2. 延迟触发博览和逶迤，确保解析已经加上了
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.ERUDITION, potency));
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.MEANDER, potency));

                this.isDone = true;
            }
        });
    }
}