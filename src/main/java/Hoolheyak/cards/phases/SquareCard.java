package Hoolheyak.cards.phases;

import Hoolheyak.cards.BaseCard;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.phases.SextilePower;
import Hoolheyak.powers.phases.SquarePower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class SquareCard extends BaseCard {
    public static final String ID = makeID("SquareCard");

    public SquareCard() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.STATUS,
                CardRarity.SPECIAL,
                CardTarget.NONE,
                -2
        ));
    }

    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        return false;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}

    @Override
    public void triggerWhenDrawn() {
        PhaseManager.triggerPhaseCardDrawn(this, new SquarePower(AbstractDungeon.player));
    }

    @Override
    public void onChoseThisOption() {
        PhaseManager.applyPhase(new SquarePower(AbstractDungeon.player));
    }
}