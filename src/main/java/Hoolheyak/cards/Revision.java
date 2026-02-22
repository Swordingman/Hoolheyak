package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Revision extends BaseCard {
    public static final String ID = makeID("Revision");

    private static final int COST = 1;

    public Revision() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setExhaust(true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            private boolean openedScreen = false;

            {
                this.actionType = ActionType.EXHAUST;
                this.duration = Settings.ACTION_DUR_FAST;
            }

            @Override
            public void update() {
                // 第一阶段：打开选牌界面
                if (!this.openedScreen) {
                    if (p.hand.isEmpty()) {
                        this.isDone = true;
                        return;
                    }
                    // EXTENDED_DESCRIPTION[0] 为 "选择一张牌消耗。"
                    AbstractDungeon.handCardSelectScreen.open(cardStrings.EXTENDED_DESCRIPTION[0], 1, false, false, false, false);
                    this.openedScreen = true;
                    return;
                }

                // 第二阶段：处理玩家选定的牌
                if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                    for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                        // 将牌移至消耗堆
                        p.hand.moveToExhaustPile(c);

                        // 根据被消耗的牌的类型，随机生成一张同类型卡牌
                        AbstractCard.CardType type = c.type;
                        AbstractCard newCard = AbstractDungeon.returnTrulyRandomCardInCombat(type).makeCopy();

                        // 升级后，使其在本回合降为0费
                        if (upgraded) {
                            newCard.setCostForTurn(0);
                        }

                        // 将新牌加入手牌
                        addToBot(new MakeTempCardInHandAction(newCard, 1));
                    }
                    AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                    AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                    this.isDone = true;
                }
            }
        });
    }
}