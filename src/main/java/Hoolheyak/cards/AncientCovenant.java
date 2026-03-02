package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.CovenantDexterityPower;
import Hoolheyak.powers.CovenantStrengthPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class AncientCovenant extends BaseCard {
    public static final String ID = makeID("AncientCovenant");

    private static final int COST = 1;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public AncientCovenant() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：博览 加力量
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new CovenantStrengthPower(p, magicNumber), magicNumber));
        }));

        // 选项 β：逶迤 加敏捷
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(p, p, new CovenantDexterityPower(p, magicNumber), magicNumber));
        }));

        addToBot(new VariableAction(this, choices, true));
    }
}