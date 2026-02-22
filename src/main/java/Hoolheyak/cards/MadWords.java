package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class MadWords extends BaseCard {
    public static final String ID = makeID("MadWords");

    private static final int COST = 3;
    private static final int DAMAGE = 9;
    private static final int MAGIC = 3; // 额外重复打出的次数
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public MadWords() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.RARE,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setCustomVar("LIFT", 2); // 2 层升力
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
        addToBot(new ApplyPowerAction(m, p, new LiftPower(m, p, customVar("LIFT")), customVar("LIFT")));
        addToBot(new RepeatAction(this, m, this.magicNumber));
    }
}