package Hoolheyak.events;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.relics.PureWaterSpriteAssist;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractImageEvent;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;

import java.util.ArrayList;

public class WaterLikeShapeEvent extends AbstractImageEvent {
    public static final String ID = HoolheyakMod.makeID("WaterLikeShape");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);

    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;

    // 状态机梳理：
    // 0 = 初始界面
    // 1 = 普通选项（非战斗）结束后的等待离开状态
    // 2 = 进入战斗状态
    // 3 = 战斗胜利且结算完奖励后的等待离开状态
    private int screenNum = 0;

    private AbstractCard cardToRemove = null;

    public WaterLikeShapeEvent() {
        super(NAME, DESCRIPTIONS[0], "Hoolheyak/images/events/water_like_shape.png");

        // [准备战斗]
        this.imageEventText.setDialogOption(OPTIONS[0]);

        // 寻找稀有度最高的随机牌
        ArrayList<AbstractCard> cards = new ArrayList<>();
        AbstractCard.CardRarity[] priorities = {
                AbstractCard.CardRarity.RARE,
                AbstractCard.CardRarity.UNCOMMON,
                AbstractCard.CardRarity.COMMON,
                AbstractCard.CardRarity.BASIC
        };

        for (AbstractCard.CardRarity rarity : priorities) {
            for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
                if (c.rarity == rarity) {
                    cards.add(c);
                }
            }
            if (!cards.isEmpty()) break;
        }

        if (!cards.isEmpty()) {
            this.cardToRemove = cards.get(AbstractDungeon.miscRng.random(cards.size() - 1));
            this.imageEventText.setDialogOption(
                    OPTIONS[1] + this.cardToRemove.name + OPTIONS[2],
                    new PureWaterSpriteAssist()
            );
        } else {
            this.imageEventText.setDialogOption(OPTIONS[5], true);
        }

        // [找借口开溜]
        this.imageEventText.setDialogOption(OPTIONS[3]);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch (screenNum) {
            case 0:
                switch (buttonPressed) {
                    case 0:
                        screenNum = 2;
                        AbstractDungeon.getCurrRoom().rewardAllowed = false;

                        AbstractDungeon.getCurrRoom().monsters = com.megacrit.cardcrawl.helpers.MonsterHelper.getEncounter(Hoolheyak.monsters.Muelsyse.ID + "_Encounter");
                        AbstractDungeon.getCurrRoom().eliteTrigger = true;
                        AbstractDungeon.lastCombatMetricKey = Hoolheyak.monsters.Muelsyse.ID + "_Encounter";

                        this.enterCombatFromImage();
                        break;

                    case 1: // 提倡同行
                        if (this.cardToRemove != null) {
                            AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(this.cardToRemove, (Settings.WIDTH / 2.0F), (Settings.HEIGHT / 2.0F)));
                            AbstractDungeon.player.masterDeck.removeCard(this.cardToRemove);
                        }

                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), new PureWaterSpriteAssist());

                        this.imageEventText.updateBodyText(DESCRIPTIONS[1]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[4]); // 离开
                        screenNum = 1; // 【修改】普通选项结束状态设为1
                        break;

                    case 2: // 找借口开溜
                        AbstractRelic r = AbstractDungeon.returnRandomScreenlessRelic(AbstractRelic.RelicTier.COMMON);
                        AbstractDungeon.getCurrRoom().spawnRelicAndObtain((float)(Settings.WIDTH / 2), (float)(Settings.HEIGHT / 2), r);

                        this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
                        this.imageEventText.clearAllDialogs();
                        this.imageEventText.setDialogOption(OPTIONS[4]); // 离开
                        screenNum = 1; // 【修改】普通选项结束状态设为1
                        break;
                }
                break;

            case 1: // 普通选项后的离开
            case 3: // 战斗后领完奖励的离开 (如果玩家点击了底部的离开按钮)
                this.openMap();
                break;
        }
    }

    @Override
    public void reopen() {
        if (screenNum != 2) return;

        this.enterImageFromCombat();

        // 1. 设置房间状态为 COMPLETE (防止地图路径卡死)
        AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;

        // 2. 彻底清空后台可能存在的默认奖励
        AbstractDungeon.getCurrRoom().rewards.clear();

        // ================= 开始添加自定义奖励 =================

        // 1. 固定 60 金币
        AbstractDungeon.getCurrRoom().addGoldToRewards(60);

        // 2. 一瓶随机药水
        AbstractDungeon.getCurrRoom().addPotionToRewards(com.megacrit.cardcrawl.helpers.PotionHelper.getRandomPotion());

        // 3. 一个稀有遗物
        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.RARE);

        // 黑星判断（如果有黑星，再多给一个稀有遗物）
        if (AbstractDungeon.player.hasRelic("Black Star")) {
            AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractRelic.RelicTier.RARE);
        }

        // 3. 重新允许发放奖励，并强制打开奖励结算界面
        AbstractDungeon.getCurrRoom().rewardAllowed = true;
        AbstractDungeon.combatRewardScreen.open();

        screenNum = 1;

        // 4. 更新事件界面文本，等待玩家领完奖励后点击离开
        this.imageEventText.updateBodyText(DESCRIPTIONS[3]);
        this.imageEventText.clearAllDialogs();
        this.imageEventText.setDialogOption(OPTIONS[4]);
    }
}