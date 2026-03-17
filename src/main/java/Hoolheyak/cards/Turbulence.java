package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Turbulence extends BaseCard {
    public static final String ID = makeID("Turbulence");

    private static final int COST = 0;
    private static final int DAMAGE = 3;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public Turbulence() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一阶段：造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));

        // 第二阶段：一回合一次的判定
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                int turnCount = 0;
                int combatCount = 0;

                // 统计本回合打出的“湍流”数量
                for (AbstractCard c : AbstractDungeon.actionManager.cardsPlayedThisTurn) {
                    if (c.cardID.equals(ID)) {
                        turnCount++;
                    }
                }

                // 只有当这是本回合打出的第一张湍流时，才触发效果
                if (turnCount == 1) {
                    // 统计本场战斗打出的总数
                    for (AbstractCard c : AbstractDungeon.actionManager.cardsPlayedThisCombat) {
                        if (c.cardID.equals(ID)) {
                            combatCount++;
                        }
                    }

                    int cardsToAdd = (combatCount / 2) * magicNumber;
                    if (cardsToAdd > 0) {
                        AbstractCard temp = new Turbulence();
                        if (upgraded) {
                            temp.upgrade();
                        }
                        addToTop(new MakeTempCardInHandAction(temp, cardsToAdd));
                    }
                }
                this.isDone = true;
            }
        });
    }
}