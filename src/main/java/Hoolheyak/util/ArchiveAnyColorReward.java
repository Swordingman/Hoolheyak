package Hoolheyak.util;

import basemod.abstracts.CustomReward;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;

public class ArchiveAnyColorReward extends CustomReward {
    // 读取本地化文本，"Hoolheyak:ArchiveRewards" 必须和你 JSON 里的 ID 对应
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("Hoolheyak:ArchiveRewards");
    public static final String[] TEXT = uiStrings.TEXT;

    public ArchiveAnyColorReward() {
        // TEXT[0] 就是 "任意颜色卡牌" / "Any Color Card"
        super(ImageMaster.REWARD_CARD_NORMAL, TEXT[0], ArchiveRewardEnums.ARCHIVE_ANY_COLOR);

        this.cards.clear();
        for (int i = 0; i < 3; i++) {
            this.cards.add(CardLibrary.getAnyColorCard(AbstractDungeon.rollRarity()).makeCopy());
        }
    }

    @Override
    public boolean claimReward() {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD) {
            // TEXT[2] 就是 "选择一张卡牌加入你的牌组"
            AbstractDungeon.cardRewardScreen.open(this.cards, this, TEXT[2]);
            AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        }
        return false;
    }
}