package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.powers.phases.QuincunxPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class Wingblade extends BaseCard {
    public static final String ID = makeID("Wingblade");

    private static final int COST = 1;
    private static final int DAMAGE = 9;
    private static final int UPGRADE_PLUS_DMG = 3;

    public Wingblade() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    // 辅助方法：判断打出这张牌会不会增加“博览”层数
    private boolean willGainEruditionStack(AbstractPlayer p) {
        // 默认情况下，攻击牌会增加层数
        boolean willGain = (this.type == AbstractCard.CardType.ATTACK);
        // 如果有特定的能力（比如你代码里的 Quincunx），则变成技能牌增加
        if (p.hasPower(QuincunxPower.POWER_ID)) {
            willGain = (this.type == AbstractCard.CardType.SKILL);
        }
        return willGain;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 造成基础伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));

        // 2. 预判触发条件
        if (!this.purgeOnUse && p.hasPower(EruditionPower.POWER_ID)) {
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.ERUDITION);
            int currentAmount = p.getPower(EruditionPower.POWER_ID).amount;

            // 预判打出后的层数 = 当前层数 + (如果这张牌能增加层数则 +1)
            int projectedAmount = currentAmount + (willGainEruditionStack(p) ? 1 : 0);

            if (projectedAmount >= currentThreshold) {
                // 满足条件！复制并双发。
                // 注意：这里我们不再扣除层数，扣除动作会交由稍后触发的 EruditionPower 自动完成。
                AbstractCard tmp = this.makeStatEquivalentCopy();
                tmp.purgeOnUse = true;
                addToBot(new com.megacrit.cardcrawl.actions.utility.NewQueueCardAction(tmp, m, false, true));
            }
        }
    }

    @Override
    public void triggerOnGlowCheck() {
        this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
        if (AbstractDungeon.player.hasPower(EruditionPower.POWER_ID)) {
            AbstractPlayer p = AbstractDungeon.player;
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.ERUDITION);
            int currentAmount = p.getPower(EruditionPower.POWER_ID).amount;

            // 同样使用预判逻辑，保证在 3 层时就能提前发金光！
            int projectedAmount = currentAmount + (willGainEruditionStack(p) ? 1 : 0);

            if (projectedAmount >= currentThreshold) {
                this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy();
            }
        }
    }
}