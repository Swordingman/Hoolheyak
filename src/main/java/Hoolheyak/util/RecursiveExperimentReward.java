package Hoolheyak.util;

import basemod.abstracts.CustomReward;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;

public class RecursiveExperimentReward extends CustomReward {
    // 记得在你的 UIStrings JSON 文件中注册这个 ID
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("Hoolheyak:RecursiveRewards");
    public static final String[] TEXT = uiStrings.TEXT;

    // 保存卡牌的类型，用于存档和读档
    public AbstractCard.CardType savedCardType;

    public RecursiveExperimentReward(AbstractCard.CardType type) {
        // 参数：图标 (可以使用自定义材质), 奖励条上的文本, 枚举类型
        super(ImageMaster.REWARD_CARD_NORMAL, TEXT[0], ArchiveRewardEnums.RECURSIVE_EXPERIMENT);

        this.savedCardType = type;
        this.cards.clear();

        // 抽取3张不重复的同类型卡牌
        while (this.cards.size() < 3) {
            AbstractCard c = AbstractDungeon.returnTrulyRandomCardInCombat(type).makeCopy();
            boolean isDuplicate = false;
            for (AbstractCard rc : this.cards) {
                if (rc.cardID.equals(c.cardID)) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                this.cards.add(c);
            }
        }
    }

    @Override
    public boolean claimReward() {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD) {
            // TEXT[1] 可以是打开奖励界面时的提示文本，比如 "选择一张卡牌"
            AbstractDungeon.cardRewardScreen.open(this.cards, this, TEXT[1]);
            AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        }
        return false;
    }
}