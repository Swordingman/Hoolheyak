package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class WindShear extends BaseCard {
    public static final String ID = makeID("WindShear");

    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 3;

    public WindShear() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR, // 请根据您的实际颜色枚举替换
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一阶段：正常的造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_VERTICAL));
        addToBot(new RepeatAction(this, m, 1));
    }
}