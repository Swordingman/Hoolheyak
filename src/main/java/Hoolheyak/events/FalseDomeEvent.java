package Hoolheyak.events;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.relics.StarryRevelation;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import java.util.ArrayList;

public class FalseDomeEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("FalseDome");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    // --- 像第一个事件一样，提取出常量 ---
    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;

    private int screenNum = 0;
    private boolean pickCards = false;

    public FalseDomeEvent() {
        super(NAME, DESCRIPTIONS[0], "HoolheyakResources/images/events/Kristen.png");

        // 选项 1：缅怀先驱，获得事件专属遗物
        this.imageEventText.setDialogOption(OPTIONS[0], new StarryRevelation());

        // 选项 2：解析轨迹，选择三张牌升级
        if (AbstractDungeon.player.masterDeck.getUpgradableCards().size() < 3) {
            this.imageEventText.setDialogOption(OPTIONS[1] + " (可升级的牌不足3张)", true);
        } else {
            this.imageEventText.setDialogOption(OPTIONS[1]);
        }

        // 选项 3：追寻未来，移除所有诅咒牌
        boolean hasCurses = false;
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.type == AbstractCard.CardType.CURSE) {
                hasCurses = true;
                break;
            }
        }
        if (hasCurses) {
            this.imageEventText.setDialogOption(OPTIONS[2]);
        } else {
            this.imageEventText.setDialogOption(OPTIONS[2] + " (没有诅咒牌)", true);
        }
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0: // 缅怀先驱
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                                (float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), new StarryRevelation());

                        this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]); // 假设 OPTIONS[3] 是你的"离开"
                        screenNum = 1;
                        break;

                    case 1: // 解析轨迹
                        pickCards = true;
                        // 这里的提示文本由于是直接显示在选牌界面的，可以直接写死或者配进JSON字典里
                        AbstractDungeon.gridSelectScreen.open(
                                AbstractDungeon.player.masterDeck.getUpgradableCards(), 3, "选择三张牌升级", false, false, false, false);

                        this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]); // 离开
                        screenNum = 1;
                        break;

                    case 2: // 追寻未来
                        ArrayList<AbstractCard> cursesToRemove = new ArrayList<>();
                        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                            if (c.type == AbstractCard.CardType.CURSE) {
                                cursesToRemove.add(c);
                            }
                        }
                        for (int i = 0; i < cursesToRemove.size(); i++) {
                            AbstractCard c = cursesToRemove.get(i);
                            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(
                                    c, (Settings.WIDTH / 2.0F) + (i * 30 * Settings.scale), (Settings.HEIGHT / 2.0F)));
                            AbstractDungeon.player.masterDeck.removeCard(c);
                        }

                        this.imageEventText.updateBodyText(DESCRIPTIONS[3]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[3]); // 离开
                        screenNum = 1;
                        break;
                }
                break;

            case 1: // 普通选项后的离开
                this.openMap();
                break;
        }
    }

    // 此方法必须保留！用于处理玩家网格选牌的异步回调
    @Override
    public void update() {
        super.update();
        if (pickCards && !AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            for (AbstractCard c : AbstractDungeon.gridSelectScreen.selectedCards) {
                c.upgrade();
                AbstractDungeon.player.bottledCardUpgradeCheck(c);
                AbstractDungeon.effectList.add(new ShowCardBrieflyEffect(c.makeStatEquivalentCopy()));
            }
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            pickCards = false;
        }
    }
}