package Hoolheyak.events;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

public class IntersectionEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("IntersectionOfPastAndPresent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private int hpLoss;
    private int screenNum = 0;

    public IntersectionEvent() {
        super(eventStrings.NAME, eventStrings.DESCRIPTIONS[0], "HoolheyakResources/images/events/TinMan.png"); // 请准备好对应的事件图片

        // 进阶15及以上失去8点生命，否则5点
        if (AbstractDungeon.ascensionLevel >= 15) {
            this.hpLoss = 8;
        } else {
            this.hpLoss = 5;
        }

        // 选项 1：获得50金币，随机升级一张牌
        if (AbstractDungeon.player.masterDeck.getUpgradableCards().isEmpty()) {
            imageEventText.setDialogOption(eventStrings.OPTIONS[0] + " (没有可升级的牌)", true);
        } else {
            imageEventText.setDialogOption(eventStrings.OPTIONS[0]);
        }

        // 选项 2：失去生命，移除一张牌
        imageEventText.setDialogOption(eventStrings.OPTIONS[1] + this.hpLoss + eventStrings.OPTIONS[2]);

        // 选项 3：获得两瓶药水
        imageEventText.setDialogOption(eventStrings.OPTIONS[3]);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0: // 初始选择
                switch (buttonPressed) {
                    case 0: // 礼貌回应
                        AbstractDungeon.player.gainGold(50);
                        CardGroup upgradable = AbstractDungeon.player.masterDeck.getUpgradableCards();
                        if (!upgradable.isEmpty()) {
                            AbstractCard c = upgradable.getRandomCard(true);
                            c.upgrade();
                            AbstractDungeon.player.bottledCardUpgradeCheck(c);
                            AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy()));
                        }
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[1]);
                        break;
                    case 1: // 呛他两句
                        AbstractDungeon.player.damage(new com.megacrit.cardcrawl.cards.DamageInfo(null, this.hpLoss));
                        if (CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()).size() > 0) {
                            AbstractDungeon.gridSelectScreen.open(CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards()), 1, "选择一张牌移除", false, false, false, true);
                        }
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[2]);
                        break;
                    case 2: // 寻求帮助
                        AbstractDungeon.getCurrRoom().rewards.clear();
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(AbstractDungeon.returnRandomPotion()));
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(AbstractDungeon.returnRandomPotion()));
                        AbstractDungeon.combatRewardScreen.open();
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[3]);
                        break;
                }
                this.imageEventText.clearAllDialogs();
                this.imageEventText.setDialogOption("离开");
                screenNum = 1;
                break;
            case 1: // 离开
                this.openMap();
                break;
        }
    }

    @Override
    public void update() {
        super.update();
        // 处理删牌的后续逻辑
        if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2.0F), (Settings.HEIGHT / 2.0F)));
            AbstractDungeon.player.masterDeck.removeCard(c);
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
        }
    }
}