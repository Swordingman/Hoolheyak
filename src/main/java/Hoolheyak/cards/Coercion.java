package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LevitatePower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Coercion extends BaseCard {
    public static final String ID = makeID("Coercion");

    private static final int COST = 2;
    private static final int DAMAGE = 12;
    private static final int UPGRADE_PLUS_DMG = 4;
    private static final int ENERGY_REFUND = 1;
    private static final int UPGRADE_PLUS_ENERGY = 1;

    public Coercion() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(ENERGY_REFUND, UPGRADE_PLUS_ENERGY);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一步：造成普通攻击伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SMASH));

        // 第二步：判定浮空，如果存在则强制触发落地并返还能量
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                if (m != null && m.hasPower(LevitatePower.POWER_ID)) {
                    LevitatePower levitate = (LevitatePower) m.getPower(LevitatePower.POWER_ID);
                    int fallDamage = levitate.triggerFall();
                    addToBot(new GainEnergyAction(magicNumber));

                    if (fallDamage > 0) {
                        addToBot(new GainBlockAction(p, p, fallDamage));
                    }
                }
                this.isDone = true;
            }
        });
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }
}