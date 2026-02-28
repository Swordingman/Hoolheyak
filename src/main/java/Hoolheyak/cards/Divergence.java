package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Divergence extends BaseCard {

    public static final String ID = makeID("Divergence"); // 卡牌ID：气流辐散
    private static final int COST = 2;
    private static final int BLOCK = 12;
    private static final int UPGRADE_PLUS_BLOCK = 2; // 升级后14点基础格挡

    // 用 magicNumber 来控制每层升力转化的格挡数：基础2点，升级后3点
    private static final int MAGIC = 2;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public Divergence() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.ENEMY, // 需要指定消耗升力的目标
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

        // 2. 动态结算目标身上的升力
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                if (m != null && m.hasPower(LiftPower.POWER_ID)) {
                    int liftAmount = m.getPower(LiftPower.POWER_ID).amount;

                    if (liftAmount > 0) {
                        int extraBlock = liftAmount * magicNumber;

                        // 先清空敌人的升力
                        addToTop(new RemoveSpecificPowerAction(m, p, LiftPower.POWER_ID));
                        // 再获得额外的格挡
                        addToTop(new GainBlockAction(p, p, extraBlock));
                    }
                }
                this.isDone = true;
            }
        });
    }
}