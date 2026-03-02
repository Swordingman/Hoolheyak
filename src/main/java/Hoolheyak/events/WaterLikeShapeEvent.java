package Hoolheyak.events;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.relics.PureWaterSpriteAssist;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.events.RoomEventDialog;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;
import java.util.ArrayList;

public class WaterLikeShapeEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("WaterLikeShape");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private int screenNum = 0;

    public WaterLikeShapeEvent() {
        super(eventStrings.NAME, eventStrings.DESCRIPTIONS[0], "HoolheyakResources/images/events/Muelsyse.png");

        // [准备战斗]
        this.imageEventText.setDialogOption(eventStrings.OPTIONS[0]);
        // [提倡同行]
        this.imageEventText.setDialogOption(eventStrings.OPTIONS[1], new PureWaterSpriteAssist());
        // [找借口开溜]
        this.imageEventText.setDialogOption(eventStrings.OPTIONS[2]);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0: // 准备战斗
                        screenNum = 1;
                        // 安排接下来的战斗遭遇
                        AbstractDungeon.getCurrRoom().monsters = com.megacrit.cardcrawl.helpers.MonsterHelper.getEncounter(Hoolheyak.monsters.Muelsyse.ID + "_Encounter");
                        AbstractDungeon.getCurrRoom().rewards.clear();
                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.RARE); // 奖励稀有遗物
                        AbstractDungeon.getCurrRoom().phase = com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT;
                        AbstractDungeon.getCurrRoom().monsters.init();
                        AbstractDungeon.getCurrRoom().eliteTrigger = true;

                        // 移除事件界面，进入战斗
                        RoomEventDialog.waitForInput = true;
                        this.enterCombatFromImage();
                        break;
                    case 1: // 提倡同行
                        // 1. 获取并移除最高稀有度卡牌
                        ArrayList<AbstractCard> cards = new ArrayList<>();
                        AbstractCard.CardRarity[] priorities = { AbstractCard.CardRarity.RARE, AbstractCard.CardRarity.UNCOMMON, AbstractCard.CardRarity.COMMON, AbstractCard.CardRarity.BASIC };
                        for (AbstractCard.CardRarity rarity : priorities) {
                            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                                if (c.rarity == rarity) cards.add(c);
                            }
                            if (!cards.isEmpty()) break; // 找到了最高稀有度的牌
                        }

                        if (!cards.isEmpty()) {
                            AbstractCard cardToRemove = cards.get(AbstractDungeon.miscRng.random(cards.size() - 1));
                            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(cardToRemove, (Settings.WIDTH / 2.0F), (Settings.HEIGHT / 2.0F)));
                            AbstractDungeon.player.masterDeck.removeCard(cardToRemove);
                        }

                        // 2. 获得遗物
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), new PureWaterSpriteAssist());

                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[1]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption("离开");
                        screenNum = 2;
                        break;
                    case 2: // 找借口开溜
                        AbstractRelic r = AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.COMMON);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), r);

                        this.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[2]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption("离开");
                        screenNum = 2;
                        break;
                }
                break;
            case 2:
                this.openMap();
                break;
        }
    }
}