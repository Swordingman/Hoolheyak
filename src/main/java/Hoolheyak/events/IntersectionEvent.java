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
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;

public class IntersectionEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("IntersectionOfPastAndPresent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    // --- 提取静态常量 ---
    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;

    private int hpLoss;
    private int screenNum = 0;

    public IntersectionEvent() {
        super(NAME, DESCRIPTIONS[0], "Hoolheyak/images/events/tin_man.png");

        // 进阶15及以上失去8点生命，否则5点
        if (AbstractDungeon.ascensionLevel >= 15) {
            this.hpLoss = 8;
        } else {
            this.hpLoss = 5;
        }

        // 选项 1：获得50金币，随机升级一张牌
        if (AbstractDungeon.player.masterDeck.getUpgradableCards().isEmpty()) {
            this.imageEventText.setDialogOption(OPTIONS[0] + OPTIONS[5], true);
        } else {
            this.imageEventText.setDialogOption(OPTIONS[0]);
        }

        // 选项 2：失去生命，移除一张牌
        this.imageEventText.setDialogOption(OPTIONS[1] + this.hpLoss + OPTIONS[2]);

        // 选项 3：获得两瓶药水
        this.imageEventText.setDialogOption(OPTIONS[3]);
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
                        this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        break;

                    case 1: // 呛他两句
                        AbstractDungeon.player.damage(new com.megacrit.cardcrawl.cards.DamageInfo(null, this.hpLoss));
                        // 稍微优化：提取出可移除牌组，避免重复调用
                        CardGroup purgeable = CardGroup.getGroupWithoutBottledCards(AbstractDungeon.player.masterDeck.getPurgeableCards());
                        if (purgeable.size() > 0) {
                            AbstractDungeon.gridSelectScreen.open(purgeable, 2, OPTIONS[6], false, false, false, true);
                        }
                        this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
                        break;

                    case 2: // 寻求帮助
                        // 1. 清理后台可能存在的默认奖励
                        AbstractDungeon.getCurrRoom().rewards.clear();

                        // 【关键修复 1】：强制禁止这个房间产生标准卡牌掉落，杜绝其他机制的干扰
                        AbstractDungeon.getCurrRoom().rewardAllowed = false;

                        // 【关键修复 2】：告诉游戏底层“当前房间结算已完成”，防止系统强行补发奖励
                        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;

                        // 添加你的两瓶药水
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(AbstractDungeon.returnRandomPotion()));
                        AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(AbstractDungeon.returnRandomPotion()));

                        // 强行打开奖励界面
                        AbstractDungeon.combatRewardScreen.open();
                        this.imageEventText.updateBodyText(DESCRIPTIONS[3]);
                        break;
                }

                this.imageEventText.clearAllDialogs();
                // 使用 OPTIONS[4] 作为离开选项
                this.imageEventText.setDialogOption(OPTIONS[4]);
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