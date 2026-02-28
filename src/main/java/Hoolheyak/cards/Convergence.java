package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.GravityPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Convergence extends BaseCard {

    public static final String ID = makeID("Convergence"); // 卡牌ID：气流辐合
    private static final int COST = 1;
    private static final int BLOCK = 8;
    private static final int UPGRADE_PLUS_BLOCK = 2; // 升级后10点基础格挡

    // 用 magicNumber 来控制额外格挡的上限：基础15点，升级后25点
    private static final int MAGIC = 15;
    private static final int UPGRADE_PLUS_MAGIC = 10;

    public Convergence() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF, // 这是一个给自己上格挡的牌
                COST
        ));

        setBlock(BLOCK, UPGRADE_PLUS_BLOCK);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setExhaust(true); // 消耗
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 先获得基础格挡
        addToBot(new GainBlockAction(p, p, this.block));

        // 2. 遍历所有怪物，计算重力之和并施加额外格挡
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                int totalGravity = 0;

                // 遍历当前房间里的所有怪物
                for (AbstractMonster mo : AbstractDungeon.getMonsters().monsters) {
                    // 确保怪物没有死也没有逃跑，且身上有重力状态
                    if (!mo.isDeadOrEscaped() && mo.hasPower(GravityPower.POWER_ID)) {
                        totalGravity += mo.getPower(GravityPower.POWER_ID).amount;
                    }
                }

                if (totalGravity > 0) {
                    // 取 重力之和 与 上限(magicNumber) 之间的最小值
                    int extraBlock = Math.min(totalGravity, magicNumber);
                    addToTop(new GainBlockAction(p, p, extraBlock));
                }
                this.isDone = true;
            }
        });
    }
}