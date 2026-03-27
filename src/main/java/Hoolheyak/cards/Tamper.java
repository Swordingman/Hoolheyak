package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.phases.QuincunxPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class Tamper extends BaseCard {
    public static final String ID = makeID("Tamper");

    private static final int COST = 1;
    private static final int BLOCK = 8;
    private static final int UPGRADE_PLUS_BLOCK = 3;

    public Tamper() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));
        setBlock(BLOCK, UPGRADE_PLUS_BLOCK);
    }

    // 辅助方法：判断打出这张牌会不会增加“逶迤”层数
    private boolean willGainMeanderStack(AbstractPlayer p) {
        // 默认情况下，技能牌会增加逶迤层数
        boolean willGain = (this.type == AbstractCard.CardType.SKILL);

        if (p.hasPower(QuincunxPower.POWER_ID)) {
            willGain = (this.type == AbstractCard.CardType.ATTACK);
        }

        return willGain;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 获得基础格挡
        addToBot(new GainBlockAction(p, p, block));

        // 2. 预判“逶迤”触发条件
        if (!this.purgeOnUse && p.hasPower(MeanderPower.POWER_ID)) {
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.MEANDER);
            int currentAmount = p.getPower(MeanderPower.POWER_ID).amount;

            // 预判打出后的层数
            int projectedAmount = currentAmount + (willGainMeanderStack(p) ? 1 : 0);

            if (projectedAmount >= currentThreshold) {
                // 满足条件！复制并额外打出一次。
                // 同样，扣除层数的操作交给 MeanderPower 的 onUseCard / checkAndTrigger 去做
                AbstractCard tmp = this.makeStatEquivalentCopy();
                tmp.purgeOnUse = true;
                addToBot(new com.megacrit.cardcrawl.actions.utility.NewQueueCardAction(tmp, m, false, true));
            }
        }
    }

    // 提示框变黄的逻辑
    @Override
    public void triggerOnGlowCheck() {
        this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();

        if (AbstractDungeon.player.hasPower(MeanderPower.POWER_ID)) {
            AbstractPlayer p = AbstractDungeon.player;
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.MEANDER);
            int currentAmount = p.getPower(MeanderPower.POWER_ID).amount;

            // 同样使用预判逻辑，保证差1层满时就能提前发金光
            int projectedAmount = currentAmount + (willGainMeanderStack(p) ? 1 : 0);

            if (projectedAmount >= currentThreshold) {
                this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy();
            }
        }
    }
}