package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Surveying extends BaseCard {
    public static final String ID = makeID("Surveying");

    private static final int COST = 1;
    private static final int DAMAGE = 8;
    private static final int UPGRADE_PLUS_DMG = 2;
    private static final int ANALYSIS = 1;
    private static final int ANALYSIS_UPG = 1;

    public Surveying() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(ANALYSIS, ANALYSIS_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, magicNumber), magicNumber));
    }
}