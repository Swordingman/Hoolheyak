package Hoolheyak.events;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.relics.StarryRevelation; // 等下我会为你提供这个遗物的代码
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardBrieflyEffect;
import java.util.Iterator;
import java.util.ArrayList;

public class FalseDomeEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("FalseDome");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private int screenNum = 0;
    private boolean pickCards = false;

    public FalseDomeEvent() {
        super(eventStrings.NAME, eventStrings.DESCRIPTIONS[0], "HoolheyakResources/images/events/Kristen.png");

        // 选项 1：缅怀先驱，获得事件专属遗物
        imageEventText.setDialogOption(eventStrings.OPTIONS[0], new StarryRevelation());

        // 选项 2：解析轨迹，选择三张牌升级
        if (AbstractDungeon.player.masterDeck.getUpgradableCards().size() < 3) {
            imageEventText.setDialogOption(eventStrings.OPTIONS[1] + " (可升级的牌不足3张)", true);
        } else {
            imageEventText.setDialogOption(eventStrings.OPTIONS[1]);
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
            imageEventText.setDialogOption(eventStrings.OPTIONS[2]);
        } else {
            imageEventText.setDialogOption(eventStrings.OPTIONS[2] + " (没有诅咒牌)", true);
        }
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0: // 缅怀先驱
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), new StarryRevelation());
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[1]);
                        break;
                    case 1: // 解析轨迹
                        pickCards = true;
                        AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.masterDeck.getUpgradableCards(), 3, "选择三张牌升级", false, false, false, false);
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[2]);
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
                            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2.0F) + (i * 30 * Settings.scale), (Settings.HEIGHT / 2.0F)));
                            AbstractDungeon.player.masterDeck.removeCard(c);
                        }
                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[3]);
                        break;
                }
                this.imageEventText.clearAllDialogs();
                this.imageEventText.setDialogOption("离开");
                screenNum = 1;
                break;
            case 1:
                this.openMap();
                break;
        }
    }

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