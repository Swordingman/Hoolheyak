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

    // 【核心修改 1】：把费用改成 0 费（或其他任意 >= -1 的费用）
    private static final int COST = 0;
    private static final int MAGIC = 1; // 保留 1 张
    private static final int UPGRADE_PLUS_MAGIC = 1;

    // 用于绕过 canUse 检查的“通行证”开关
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
        setExhaust(true);
    }

    // 【核心修改 2】：彻底接管能否打出的判定
    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        // 如果是系统丢弃自动打出，亮起绿灯！
        if (this.isDiscardAutoplay) {
            return true;
        }

        // 如果是玩家手动点击，亮起红灯，并弹出提示。
        // （你可以把这段文字写进你的 JSON EXTENDED_DESCRIPTION 里以支持多语言）
        this.cantUseMessage = "我不能手动打出这张牌。";
        return false;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 成功打出后，重置通行证
        this.isDiscardAutoplay = false;
    }

    @Override
    public void triggerOnEndOfTurnForPlayingCard() {
        addToBot(new RetainCardsAction(AbstractDungeon.player, this.magicNumber));
    }

    @Override
    public void triggerOnManualDiscard() {
        // 1. 发放自动打出通行证
        this.isDiscardAutoplay = true;

        // 2. 把它从弃牌堆里捞出来（防止被视作已在弃牌堆而产生报错）
        if (AbstractDungeon.player.discardPile.contains(this)) {
            AbstractDungeon.player.discardPile.removeCard(this);
        }

        // 3. 强行排入队列打出
        addToBot(new NewQueueCardAction(this, true, true, true));
    }
}