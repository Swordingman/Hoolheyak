package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.DeconstructionPower;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class BaroclinicInstability extends BaseCard {
    public static final String ID = makeID("BaroclinicInstability");

    private static final int COST = 1;

    // 基础伤害 6，升级后 +2 (变为 8)
    private static final int DAMAGE = 6;
    private static final int UPGRADE_PLUS_DMG = 2;

    // 基础解构 4，升级后 +2 (变为 6)
    private static final int MAGIC = 4;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    // 基础升力 1，升级后 +2 (变为 3)
    private static final int LIFT = 1;
    private static final int UPGRADE_PLUS_LIFT = 1;

    public BaroclinicInstability() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ALL_ENEMY, // 【关键修改】目标改为所有敌人
                COST
        ));

        // 【关键修改】标记为多段/群体伤害，这样游戏才能正确计算力量加成等属性
        this.isMultiDamage = true;

        setCustomVar("LIFT", LIFT, UPGRADE_PLUS_LIFT);
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 【关键修改】使用 DamageAllEnemiesAction，并且传入 this.multiDamage
        addToBot(new DamageAllEnemiesAction(p, this.multiDamage, this.damageTypeForTurn, AbstractGameAction.AttackEffect.SLASH_DIAGONAL));

        // 【关键修改】遍历房间内所有怪物，分别为存活的怪物施加状态
        for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
            if (!mo.isDeadOrEscaped()) {
                addToBot(new ApplyPowerAction(mo, p, new DeconstructionPower(mo, this.magicNumber), this.magicNumber));
                addToBot(new ApplyPowerAction(mo, p, new LiftPower(mo, p, customVar("LIFT")), customVar("LIFT")));
            }
        }
    }
}