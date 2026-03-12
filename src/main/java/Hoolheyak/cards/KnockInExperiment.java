package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.ForbiddenKnowledgeEruPower;
import Hoolheyak.powers.ForbiddenKnowledgeMeaPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class KnockInExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("KnockInExperiment");

    private static final int COST = 1;
    private static final int DAMAGE = 7;
    private static final int UPGRADE_PLUS_DMG = 3; // 默认升级给与 +3 伤害

    public KnockInExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();
        // 选项 α：消耗 1 能量，回复 3 生命
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            // 安全判定：确保玩家真的有能量可以扣除
            if (EnergyPanel.totalCount >= 1) {
                p.energy.use(1);
                addToBot(new HealAction(p, p, 3));
            }
        }));

        // 选项 β：获得 1 能量
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new GainEnergyAction(1));
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一阶段：造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));

        addToBot(new VariableAction(this, getVariableChoices(p, m)));
    }
}