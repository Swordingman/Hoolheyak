package Hoolheyak.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;

public class Strike extends BaseCard {

    public static final String ID = makeID("Strike");

    private static final int COST = 1;
    private static final int DAMAGE = 6;
    private static final int UPGRADE_PLUS_DMG = 3;

    public Strike() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.BASIC,
                CardTarget.ENEMY,
                COST
        ));

        setDamage(DAMAGE, UPGRADE_PLUS_DMG);

        // 添加打击标签
        this.tags.add(CardTags.STARTER_STRIKE);
        this.tags.add(CardTags.STRIKE);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
    }
}