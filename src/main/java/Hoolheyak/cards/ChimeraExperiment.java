package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.PlayTopTypeCardsAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;
import java.util.ArrayList;

public class ChimeraExperiment extends BaseCard {
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
        this.exhaust = true; // X费连续打牌非常强，建议消耗
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 计算 X 的值
        int effect = EnergyPanel.totalCount;
        if (this.energyOnUse != -1) {
            effect = this.energyOnUse;
        }

        // 化学物 X 遗物加成
        if (p.relics != null) {
            for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                if (ChemicalX.ID.equals(r.relicId)) {
                    effect += 2;
                    r.flash();
                }
            }
        }

        // 升级后 X+1
        if (this.upgraded) {
            effect += 1;
        }

        // 扣除能量
        if (!this.freeToPlayOnce) {
            p.energy.use(EnergyPanel.totalCount);
        }

        int finalEffect = effect;

        if (finalEffect > 0) {
            ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

            choices.add(new VariableAction.VariableChoice("打出攻击牌", () -> {
                addToBot(new PlayTopTypeCardsAction(finalEffect, CardType.ATTACK));
            }));

            choices.add(new VariableAction.VariableChoice("打出技能牌", () -> {
                addToBot(new PlayTopTypeCardsAction(finalEffect, CardType.SKILL));
            }));

            addToBot(new VariableAction(this, choices, true));
        }
    }
}