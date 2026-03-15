package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.unique.RetainCardsAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ParameterCorrection extends BaseCard {
    public static final String ID = makeID("ParameterCorrection");

    private static final int COST = 0;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public ParameterCorrection() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        setExhaust(true); // 保持原版的消耗属性
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
    }

    // 回合结束时，如果这张卡在手牌中，游戏引擎会自动调用这个方法
    @Override
    public void triggerOnEndOfTurnForPlayingCard() {
        // 1. 触发原版的保留手牌效果
        addToBot(new RetainCardsAction(AbstractDungeon.player, this.magicNumber));

        // 2. 每次触发该效果时，获得 1 层 MeanderPower
        addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player,
                new MeanderPower(AbstractDungeon.player, 1), 1));
    }
}