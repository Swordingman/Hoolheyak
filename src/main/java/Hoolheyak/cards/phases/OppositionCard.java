package Hoolheyak.cards.phases;

import Hoolheyak.cards.BaseCard;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.phases.OppositionPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class OppositionCard extends BaseCard {
    public static final String ID = makeID("OppositionCard");

    public OppositionCard() {
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
        PhaseManager.triggerPhaseCardDrawn(this, new OppositionPower(AbstractDungeon.player));
    }

    @Override
    public void onChoseThisOption() {
        PhaseManager.applyPhase(new OppositionPower(AbstractDungeon.player));
    }
}