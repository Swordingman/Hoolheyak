package Hoolheyak.cards;

import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.PlayTopTypeCardsAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import java.util.ArrayList;

public class ChimeraExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("ChimeraExperiment");

    private static final int COST = -1; // X费

    public ChimeraExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.NONE,
                COST
        ));
    }

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

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new PlayTopTypeCardsAction(finalEffect, CardType.ATTACK));
        }));

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new PlayTopTypeCardsAction(finalEffect, CardType.SKILL));
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
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