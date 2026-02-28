package Hoolheyak.util;

import basemod.abstracts.CustomReward;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.UIStrings;

public class ArchiveColorlessReward extends CustomReward {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("Hoolheyak:ArchiveRewards");
    public static final String[] TEXT = uiStrings.TEXT;

    public ArchiveColorlessReward() {
        super(ImageMaster.REWARD_CARD_NORMAL, TEXT[1], ArchiveRewardEnums.ARCHIVE_COLORLESS);

        this.cards.clear();
        this.cards.addAll(AbstractDungeon.getColorlessRewardCards());
    }

    @Override
    public boolean claimReward() {
        if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.COMBAT_REWARD) {
            AbstractDungeon.cardRewardScreen.open(this.cards, this, TEXT[2]);
            AbstractDungeon.previousScreen = AbstractDungeon.CurrentScreen.COMBAT_REWARD;
        }
        return false;
    }
}