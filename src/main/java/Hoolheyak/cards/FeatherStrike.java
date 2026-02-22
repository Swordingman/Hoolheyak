package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

public class FeatherStrike extends BaseCard {

    public static final String ID = makeID("FeatherStrike");

    private static final int COST = 0;
    private static final int DAMAGE = 3;
    private static final int UPGRADE_PLUS_DMG = 3;
    private static final int LIFT = 1;

    public FeatherStrike() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                AbstractCard.CardType.ATTACK,
                AbstractCard.CardRarity.BASIC,
                AbstractCard.CardTarget.ENEMY,
                COST
        ));

        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(LIFT);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        addToBot(new ApplyPowerAction(m, p, new LiftPower(m, p, this.magicNumber), this.magicNumber));
    }
}
