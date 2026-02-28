package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction; // 确保引入你的 RepeatAction
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class AugmentationExperiment extends BaseCard {
    public static final String ID = makeID("AugmentationExperiment");
    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 3;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public AugmentationExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 基础伤害（本体和复制体都会执行这一步）
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));

        // 2. 【核心修改】：利用你在 RepeatAction 里设下的 dontTriggerOnUseCard 标记
        // 确保只有玩家手动打出的“本体”才会呼出选择界面
        if (!this.dontTriggerOnUseCard) {
            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            // 选项 A：使用自定义的 RepeatAction 额外打出
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
                // 把这张卡(this)和目标(m)传给你的 RepeatAction
                addToBot(new RepeatAction(this, m, this.magicNumber));
            }));

            // 选项 B：获得解析
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber), this.magicNumber));
            }));

            addToBot(new VariableAction(this, choices));
        }
    }
}