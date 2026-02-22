package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class LaminarFlow extends BaseCard{
    public static final String ID = makeID("LaminarFlow");

    private static final int COST = 1;
    private static final int DAMAGE = 3;
    private static final int DRAW = 1;
    private static final int DRAW_UPG = 1;

    public LaminarFlow() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));

        setDamage(DAMAGE);
        setMagic(DRAW, DRAW_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
        addToBot(new DrawCardAction(p, this.magicNumber));
    }
}
