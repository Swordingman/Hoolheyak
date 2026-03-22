package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.MeanderPower;
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

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 获得格挡
        addToBot(new GainBlockAction(p, p, block));


        // 如果玩家拥有逶迤，且层数为3（打出这张技能牌后刚好触发），则重复打出 1 次
        if (p.hasPower(MeanderPower.POWER_ID)){
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.MEANDER);
            if (p.getPower(MeanderPower.POWER_ID).amount >= currentThreshold) {
                addToBot(new RepeatAction(this, m, 1));
            }
        }
    }

    // 提示框变黄的逻辑
    @Override
    public void triggerOnGlowCheck() {
        this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy(); // 默认蓝框

        // 检查发光条件：玩家有逶迤，且层数刚好为3
        if (player.hasPower(MeanderPower.POWER_ID)) {
            int currentThreshold = TriggerKeywordAction.getThreshold(player, TriggerKeywordAction.KeywordType.MEANDER);
            if (player.getPower(MeanderPower.POWER_ID).amount >= currentThreshold) {
                this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy(); // 满足则金框
            }
        }
    }
}