package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.unique.RetainCardsAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ParameterCorrection extends BaseCard {
    public static final String ID = makeID("ParameterCorrection");

    private static final int COST = -2; // 默认无法手动打出
    private static final int MAGIC = 1; // 保留 1 张
    private static final int UPGRADE_PLUS_MAGIC = 1;

    // 【新增】：用于绕过底层 canUse 检查的“通行证”开关
    private boolean isDiscardAutoplay = false;

    public ParameterCorrection() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setExhaust(true); // 被打出时会消耗
    }

    // 【核心修复】：接管 canUse 判定
    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        // 如果是被丢弃触发的自动打出，亮起绿灯，允许打出！
        if (this.isDiscardAutoplay) {
            return true;
        }
        // 否则（玩家试图在手牌中选中/打出它时），亮起红灯
        return false;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 打出成功后，安全起见将通行证重置
        this.isDiscardAutoplay = false;

        // 卡牌自带 Exhaust，这里什么都不用做，引擎会自动判定它被视作“打出技能牌”
        // 并触发逶迤、博览等能力，最后将其送入消耗堆。
    }

    // 在手牌中结束回合时，触发保留效果
    @Override
    public void triggerOnEndOfTurnForPlayingCard() {
        addToBot(new RetainCardsAction(AbstractDungeon.player, this.magicNumber));
    }

    // 当被手动丢弃时，视为被打出
    @Override
    public void triggerOnManualDiscard() {
        // 1. 开启自动打出通行证
        this.isDiscardAutoplay = true;

        // 2. 将它从弃牌堆中拽出来
        if (AbstractDungeon.player.discardPile.contains(this)) {
            AbstractDungeon.player.discardPile.removeCard(this);
        }

        // 3. 排入行动队列打出
        // 参数依次为：(要打出的卡牌, 是否随机目标, 是否自动打出不耗能, 是否排队)
        addToBot(new NewQueueCardAction(this, true, true, true));
    }
}