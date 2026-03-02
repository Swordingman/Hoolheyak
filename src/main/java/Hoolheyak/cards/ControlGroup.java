package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.WeightlessPower;
import Hoolheyak.util.CustomTags;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class ControlGroup extends BaseCard {
    public static final String ID = makeID("ControlGroup");

    public ControlGroup() {
        super(ID, 1, CardType.ATTACK, CardTarget.ENEMY, CardRarity.COMMON, CardColor.COLORLESS);
        setDamage(5, 3);
        setMagic(1);

        this.tags.add(CustomTags.HOOLHEYAK_VARIABLE);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {

        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        choices.add(new VariableAction.VariableChoice(
                this.cardStrings.EXTENDED_DESCRIPTION[0],
                () -> {
                    this.type = CardType.ATTACK;

                    AbstractDungeon.actionManager.addToBottom(
                            new ApplyPowerAction(p, p, new EruditionPower(p, 1), 1)
                    );
                }
        ));

        choices.add(new VariableAction.VariableChoice(
                this.cardStrings.EXTENDED_DESCRIPTION[1],
                () -> {
                    this.type = CardType.SKILL;

                    AbstractDungeon.actionManager.addToBottom(
                            new ApplyPowerAction(p, p, new MeanderPower(p, 1), 1)
                    );
                }
        ));

        addToBot(new VariableAction(this, choices, true));
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
        addToBot(new ApplyPowerAction(m, p, new WeightlessPower(m, p, this.magicNumber), this.magicNumber));
    }
}