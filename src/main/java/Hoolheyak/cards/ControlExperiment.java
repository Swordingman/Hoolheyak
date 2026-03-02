package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.WeightlessPower; // 或者是 GravityPower，取决于你代码里的名字
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import java.util.ArrayList;

public class ControlExperiment extends BaseCard {
    public static final String ID = makeID("ControlExperiment");
    private static final int COST = 1;
    private static final int DAMAGE = 8;
    private static final int UPGRADE_PLUS_DMG = 2;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public ControlExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));

        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber), this.magicNumber));
        }));

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(m, p, new WeightlessPower(m, p, this.magicNumber), this.magicNumber));
        }));

        addToBot(new VariableAction(this, choices, true));
    }
}