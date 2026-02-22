package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Headwind extends BaseCard {
    public static final String ID = makeID("Headwind");

    private static final int COST = 1;
    private static final int DAMAGE = 2;
    private static final int UPGRADE_PLUS_DMG = 1;
    private static final int HITS = 3;

    public Headwind() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ALL_ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(HITS);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    // 获取一个随机存活的敌人
                    AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                    if (target != null) {
                        // 针对该敌人计算伤害（应用易伤等状态）
                        calculateCardDamage(target);

                        // addToTop 是栈结构（后进先出），所以我们先压入“给升力”，再压入“造成伤害”
                        // 这样结算时就会先跳伤害，再附加升力
                        addToTop(new ApplyPowerAction(target, p, new LiftPower(target, p, 1), 1));
                        addToTop(new DamageAction(target, new DamageInfo(p, damage, damageTypeForTurn), AttackEffect.SLASH_DIAGONAL));
                    }
                    this.isDone = true;
                }
            });
        }
    }
}