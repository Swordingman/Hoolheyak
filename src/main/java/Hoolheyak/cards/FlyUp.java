package Hoolheyak.cards;

import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.KukulkanLegacyPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.phases.SextilePower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class FlyUp extends BaseCard {
    public static final String ID = makeID("FlyUp");

    private static final int COST = -1;
    private static final int ANALYSIS = 1;

    public FlyUp() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));

        setMagic(ANALYSIS);
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    // 移除了原本重写的 applyPowers 和 onMoveToDiscard，
    // 因为现在触发次数不再与“解析”强绑定，而是与打出时的能量挂钩。

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber)));

        // 1. 计算 X 费用的老套路
        int effect = EnergyPanel.totalCount;
        if (this.energyOnUse != -1) {
            effect = this.energyOnUse;
        }

        // 2. 兼容化学物 X
        if (p.relics != null) {
            for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                if (ChemicalX.ID.equals(r.relicId)) {
                    effect += 2;
                    r.flash();
                }
            }
        }

        // 3. 升级效果
        if (this.upgraded) {
            effect += 1;
        }

        // 4. 执行变量逻辑
        if (effect > 0) {
            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            // 选项 α：用我们封装好的 Action，直接告诉它触发 ERUDITION，次数是 effect
            int finalEffect1 = effect;
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.ERUDITION, finalEffect1));
            }));

            // 选项 β：触发 MEANDER
            int finalEffect = effect;
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.MEANDER, finalEffect));
            }));

            addToBot(new VariableAction(this, choices, true));
        }

        // 5. 扣除能量
        if (!this.freeToPlayOnce) {
            p.energy.use(EnergyPanel.totalCount);
        }
    }
}