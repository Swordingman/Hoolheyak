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

                        // 根据被消耗的牌的类型，安全且随机地生成同类型卡牌
                        AbstractCard newCard;
                        switch (c.type) {
                            case ATTACK:
                            case SKILL:
                            case POWER:
                                // 常规类型调用原版方法
                                newCard = AbstractDungeon.returnTrulyRandomCardInCombat(c.type).makeCopy();
                                break;
                            case CURSE:
                                // 诅咒有专门的底层方法
                                newCard = AbstractDungeon.returnRandomCurse().makeCopy();
                                break;
                            case STATUS:
                                // 状态牌没有官方随机池，我们手动建一个小池子
                                AbstractCard[] statuses = new AbstractCard[] {
                                        new com.megacrit.cardcrawl.cards.status.Dazed(),   // 眩晕
                                        new com.megacrit.cardcrawl.cards.status.Wound(),   // 伤口
                                        new com.megacrit.cardcrawl.cards.status.Burn(),    // 灼伤
                                        new com.megacrit.cardcrawl.cards.status.VoidCard(),// 虚空
                                        new com.megacrit.cardcrawl.cards.status.Slimed()   // 黏液
                                };
                                newCard = statuses[AbstractDungeon.cardRandomRng.random(statuses.length - 1)].makeCopy();
                                break;
                            default:
                                // 兜底机制：万一遇到其他 Mod 添加的奇葩类型，给一张完全随机的牌
                                newCard = AbstractDungeon.returnTrulyRandomCardInCombat().makeCopy();
                                break;
                        }

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