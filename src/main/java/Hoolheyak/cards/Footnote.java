package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import basemod.BaseMod;

public class Footnote extends BaseCard {
    public static final String ID = makeID("Footnote");

    private static final int COST = 1;
    private static final int BLOCK = 8;
    private static final int UPGRADE_PLUS_BLOCK = 3;

    public Footnote() {
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
        // 第一阶段：获得格挡
        addToBot(new GainBlockAction(p, p, block));

        // 第二阶段：检索技能牌
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                if (p.drawPile.isEmpty() && p.discardPile.isEmpty()) {
                    this.isDone = true;
                    return;
                }

                boolean hasSkillInDraw = false;
                for (AbstractCard c : p.drawPile.group) {
                    if (c.type == AbstractCard.CardType.SKILL) {
                        hasSkillInDraw = true;
                        break;
                    }
                }

                // 如果抽牌堆没有技能牌，检查弃牌堆；如果有，洗牌并重新将本 Action 压入队列
                if (!hasSkillInDraw) {
                    boolean hasSkillInDiscard = false;
                    for (AbstractCard c : p.discardPile.group) {
                        if (c.type == AbstractCard.CardType.SKILL) {
                            hasSkillInDiscard = true;
                            break;
                        }
                    }

                    if (hasSkillInDiscard) {
                        addToTop(this); // 把自己再次压入顶部（等洗牌完毕后执行）
                        addToTop(new EmptyDeckShuffleAction());
                    }
                    this.isDone = true;
                    return;
                }

                // 从牌库顶往底找，抽出第一张技能牌
                for (int i = p.drawPile.size() - 1; i >= 0; i--) {
                    AbstractCard c = p.drawPile.group.get(i);
                    if (c.type == AbstractCard.CardType.SKILL) {
                        if (p.hand.size() == BaseMod.MAX_HAND_SIZE) {
                            p.createHandIsFullDialog();
                        } else {
                            // 动画与位移表现
                            c.unhover();
                            c.lighten(true);
                            c.setAngle(0.0F);
                            c.drawScale = 0.12F;
                            c.targetDrawScale = 0.75F;
                            c.current_x = CardGroup.DRAW_PILE_X;
                            c.current_y = CardGroup.DRAW_PILE_Y;

                            p.drawPile.removeCard(c);
                            p.hand.addToTop(c);
                            p.hand.refreshHandLayout();
                            p.hand.applyPowers();
                        }
                        break; // 找到后立即打断循环
                    }
                }
                this.isDone = true;
            }
        });
    }
}