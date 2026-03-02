package Hoolheyak.cards;

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
        // 1. 计算当前的 X 值（能量）
        int effect = EnergyPanel.totalCount;
        if (this.energyOnUse != -1) {
            effect = this.energyOnUse;
        }

        // 2. 兼容原版遗物“化学物 X” (+2 效果)
        if (p.relics != null) {
            for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                if (ChemicalX.ID.equals(r.relicId)) {
                    effect += 2;
                    r.flash();
                }
            }
        }

        // 3. 升级效果：额外触发 1 次
        if (this.upgraded) {
            effect += 1;
        }

        // 4. 如果 X > 0，执行你的变量逻辑
        if (effect > 0) {
            // 这里推测你的底层逻辑是给予相应的 Power 层数来触发效果。
            // 假设默认每 5 层触发 1 次（按你原来的 5 * x）
            int threshold = 5;

            // 如果玩家有“六合”等改变触发阈值的状态，需要在这里调整单次触发所需的层数。
            // （我将你原来的 x -= 2 逻辑优化为了直接修改阈值，这样数学算起来更严谨，不会出现负数层数）
            if (p.hasPower(SextilePower.POWER_ID)) {
                threshold = 3;
            }

            // 计算需要给予的总层数 = 触发次数(effect) * 每次触发所需层数(threshold)
            int total = threshold * effect;

            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            // 选项 α：触发 X 次博览
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
                addToBot(new ApplyPowerAction(p, p, new EruditionPower(p, total), total));
            }));

            // 选项 β：触发 X 次逶迤
            choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                addToBot(new ApplyPowerAction(p, p, new MeanderPower(p, total), total));
            }));

            addToBot(new VariableAction(this, choices, true));
        }

        // 5. 扣除玩家的能量（X费卡的标准结算）
        if (!this.freeToPlayOnce) {
            p.energy.use(EnergyPanel.totalCount);
        }
    }
}