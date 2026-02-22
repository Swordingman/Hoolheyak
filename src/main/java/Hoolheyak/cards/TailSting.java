package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;

public class TailSting extends BaseCard {
    public static final String ID = makeID("TailSting");

    private static final int COST = 1;
    private static final int DAMAGE = 8;
    private static final int UPGRADE_PLUS_DMG = 2;
    private static final int VULNERABLE = 1;
    private static final int UPGRADE_PLUS_VULN = 1;

    public TailSting() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(VULNERABLE, UPGRADE_PLUS_VULN);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));
        addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, magicNumber, false), magicNumber));
    }
}