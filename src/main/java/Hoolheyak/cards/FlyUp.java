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
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class FlyUp extends BaseCard implements IVariableCard {
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

    // 2. 核心逻辑：将选项组装和 X 费用判定搬移到这里
    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();
        int effect = 0;

        // 🌟 分歧点：判定当前是后台自动触发，还是玩家手动打出
        if (isAutoTriggered) {
            // 自动触发：默认视为消耗了 2 点能量
            effect = 2;
            if (p.relics != null) {
                for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                    if (ChemicalX.ID.equals(r.relicId)) {
                        effect += 2;
                    }
                }
            }
            // 如果卡牌已升级，依然享受升级的 +1 加成（最终变为 3 次）
            if (this.upgraded) {
                effect += 1;
            }
        } else {
            // 手动触发：走标准的 X 费用计算逻辑
            effect = EnergyPanel.totalCount;
            if (this.energyOnUse != -1) {
                effect = this.energyOnUse;
            }
            if (p.relics != null) {
                for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                    if (ChemicalX.ID.equals(r.relicId)) {
                        effect += 2;
                    }
                }
            }
            if (this.upgraded) {
                effect += 1;
            }
        }

        // 为 Lambda 表达式准备一个不可变的 final 变量
        int finalEffect = effect;

        // 选项 α：触发 ERUDITION
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            if (finalEffect > 0) { // 只有在有效次数 > 0 时才执行具体 Action
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.ERUDITION, finalEffect));
            }
        }));

        // 选项 β：触发 MEANDER
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            if (finalEffect > 0) {
                addToBot(new TriggerKeywordAction(p, TriggerKeywordAction.KeywordType.MEANDER, finalEffect));
            }
        }));

        return choices;
    }

    // 3. 极其干净的 use 方法
    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 先上能力
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber)));

        // 视觉反馈：如果玩家有化学物X，闪烁它（因为真正的数值计算已经挪到上面了）
        if (p.hasRelic(ChemicalX.ID)) {
            p.getRelic(ChemicalX.ID).flash();
        }

        // 调用变量系统并展示选项UI
        addToBot(new VariableAction(this, getVariableChoices(p, m), true));

        // 扣除能量
        if (!this.freeToPlayOnce) {
            p.energy.use(EnergyPanel.totalCount);
        }
    }
}