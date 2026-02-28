package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.SeparationAction;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Separation extends BaseCard {
    public static final String ID = makeID("Separation");

    private static final int COST = 1;
    private static final int DAMAGE = 14;
    private static final int UPGRADE_PLUS_DMG = 4;

    public Separation() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.RARE,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 呼叫专属的扣血斩杀 Action
        addToBot(new SeparationAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn)));
    }
}