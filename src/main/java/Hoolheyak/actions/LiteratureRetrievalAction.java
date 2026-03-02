package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.CardRewardScreen;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;
import java.util.ArrayList;

public class LiteratureRetrievalAction extends AbstractGameAction {
    private boolean retrieveCard = false;
    private boolean upgraded;

    public LiteratureRetrievalAction(boolean upgraded) {
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = Settings.ACTION_DUR_FAST;
        this.upgraded = upgraded; // 传入是否升级，以决定是否降为0费
    }

    @Override
    public void update() {
        if (this.duration == Settings.ACTION_DUR_FAST) {
            // 调用原版的“发现”界面，展示我们生成的 3 张牌
            AbstractDungeon.cardRewardScreen.customCombatOpen(generateCardChoices(), CardRewardScreen.TEXT[1], true);
            this.tickDuration();
            return;
        }

        if (!this.retrieveCard) {
            // 当玩家做出了选择
            if (AbstractDungeon.cardRewardScreen.discoveryCard != null) {
                AbstractCard disCard = AbstractDungeon.cardRewardScreen.discoveryCard.makeStatEquivalentCopy();

                // 如果打出的是升级版的【文献检索】，将被选中的牌本回合耗能降为 0
                if (this.upgraded) {
                    disCard.setCostForTurn(0);
                }

                disCard.current_x = -1000.0F * Settings.scale;

                // 判断手牌是否满了，满了进弃牌堆，没满进手牌
                if (AbstractDungeon.player.hand.size() < 10) {
                    AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(disCard, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                } else {
                    AbstractDungeon.effectList.add(new ShowCardAndAddToDiscardEffect(disCard, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                }

                // 清空选择缓存
                AbstractDungeon.cardRewardScreen.discoveryCard = null;
            }
            this.retrieveCard = true;
        }
        this.tickDuration();
    }

    // 生成 3 张其他职业的随机牌
    private ArrayList<AbstractCard> generateCardChoices() {
        ArrayList<AbstractCard> derp = new ArrayList<>();
        while (derp.size() != 3) {
            boolean dupe = false;
            // 获取任意颜色的随机牌（带有随机稀有度）
            AbstractCard tmp = CardLibrary.getAnyColorCard(AbstractDungeon.rollRarity());

            // 过滤掉玩家本职业的牌，以及确保选项中没有重复的牌
            if (tmp.color == AbstractDungeon.player.getCardColor()) {
                dupe = true;
            } else {
                for (AbstractCard c : derp) {
                    if (c.cardID.equals(tmp.cardID)) {
                        dupe = true;
                        break;
                    }
                }
            }

            if (!dupe) {
                derp.add(tmp.makeCopy());
            }
        }
        return derp;
    }
}