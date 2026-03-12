package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.DeconstructionPower;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import java.util.ArrayList;

public class ApoptosisExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("ApoptosisExperiment");
    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 2;
    private static final int MAGIC = 2; // 升力
    private static final int UPGRADE_PLUS_MAGIC = 1;
    private static final int DECONSTRUCTION = 8;
    private static final int DECONSTRUCTION_UPG = 2;

    public ApoptosisExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.COMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setCustomVar("DECONSTRUCTION", DECONSTRUCTION, DECONSTRUCTION_UPG);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(m, p, new LiftPower(m, p, this.magicNumber), this.magicNumber));
        }));

        String option2Text = cardStrings.EXTENDED_DESCRIPTION[1].replace(
                "!Hoolheyak:DECONSTRUCTION!",
                String.valueOf(customVar("DECONSTRUCTION"))
        );

        choices.add(new VariableAction.VariableChoice(option2Text, () -> {
            addToBot(new ApplyPowerAction(m, p, new DeconstructionPower(m, customVar("DECONSTRUCTION")), customVar("DECONSTRUCTION")));
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.POISON));

        addToBot(new VariableAction(this, getVariableChoices(p, m), true));
    }
}