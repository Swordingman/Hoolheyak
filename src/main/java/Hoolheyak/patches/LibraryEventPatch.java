package Hoolheyak.patches;

import Hoolheyak.HoolheyakMod;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import com.megacrit.cardcrawl.core.Settings;

import java.util.ArrayList;

public class LibraryEventPatch {
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(HoolheyakMod.makeID("TheLibraryPatch"));
    public static boolean recordChosen = false;

    // 1. 在事件初始化时，追加一个新的选项
    @SpirePatch(clz = TheLibrary.class, method = SpirePatch.CONSTRUCTOR)
    public static class AddOptionPatch {
        @SpirePostfixPatch
        public static void Postfix(TheLibrary __instance) {
            recordChosen = false;
            // 获取我们写在 JSON 里的选项文本
            String optionText = eventStrings.OPTIONS[0];
            __instance.imageEventText.setDialogOption(optionText);
        }
    }

    // 2. 处理点击新选项的逻辑 (原版有两个选项，索引为0和1，我们的新选项索引为2)
    @SpirePatch(clz = TheLibrary.class, method = "buttonEffect")
    public static class ButtonEffectPatch {

        // 获取你在 JSON 里定义的 UIStrings
        private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("Hoolheyak:ArchiveRewards");

        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(TheLibrary __instance, int buttonPressed) {

            // 如果处于初始选择画面且按下了我们的新按钮 (索引为 2)
            if (getScreenNum(__instance) == 0 && buttonPressed == 2) {

                // 更新事件文本和退出按钮
                __instance.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[0]);
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(TheLibrary.OPTIONS[3]); // 原版的“离开”选项

                // ================= 生成 5 张任意颜色的随机稀有牌 =================
                CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                ArrayList<AbstractCard> allRares = new ArrayList<>();

                // 1. 遍历游戏中的所有卡牌，把稀有牌筛选出来（剔除诅咒和状态牌防脏数据）
                for (AbstractCard c : CardLibrary.cards.values()) {
                    if (c.rarity == AbstractCard.CardRarity.RARE
                            && c.type != AbstractCard.CardType.CURSE
                            && c.type != AbstractCard.CardType.STATUS) {
                        allRares.add(c);
                    }
                }

                // 2. 随机抽取 5 张不重复的卡牌
                ArrayList<AbstractCard> selectedCards = new ArrayList<>();
                // 加入防死循环的保险：万一由于别的mod干涉导致总稀有牌不到5张
                int cardsToGenerate = Math.min(5, allRares.size());

                while (selectedCards.size() < cardsToGenerate && !allRares.isEmpty()) {
                    // 使用 AbstractDungeon 的随机数生成器，保证同种子情况下的结果一致性
                    AbstractCard randomCard = allRares.get(AbstractDungeon.cardRandomRng.random(allRares.size() - 1));

                    boolean dupe = false;
                    for (AbstractCard existing : selectedCards) {
                        if (existing.cardID.equals(randomCard.cardID)) {
                            dupe = true;
                            break;
                        }
                    }

                    if (!dupe) {
                        AbstractCard copy = randomCard.makeCopy();
                        selectedCards.add(copy);
                        group.addToBottom(copy);
                    }
                }
                // =================================================================

                // 打开选牌界面，并读取 UIStrings 里的第三项文本 ("选择一张卡牌加入你的牌组")
                AbstractDungeon.gridSelectScreen.open(group, 1, uiStrings.TEXT[2], false);
                recordChosen = true;
                setScreenNum(__instance, 1); // 切换状态

                // 拦截原版逻辑
                return SpireReturn.Return();
            }

            // 如果按的是原版的 0 或 1 选项，正常放行
            return SpireReturn.Continue();
        }
    }

    // 3. 在事件的 update 中处理选牌结束后的获取逻辑
    @SpirePatch(clz = TheLibrary.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void Postfix(TheLibrary __instance) {
            if (recordChosen && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
                c.upgrade(); // 根据需要，如果想给牌升级就留着，不想就删掉这行
                AbstractDungeon.effectList.add(new ShowCardAndObtainEffect(c, Settings.WIDTH / 2.0F, Settings.HEIGHT / 2.0F));
                AbstractDungeon.gridSelectScreen.selectedCards.clear();
                recordChosen = false;
            }
        }
    }

    // 辅助方法，通过反射获取私有变量 screenNum
    private static int getScreenNum(TheLibrary event) {
        try {
            java.lang.reflect.Field f = TheLibrary.class.getDeclaredField("screenNum");
            f.setAccessible(true);
            return f.getInt(event);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void setScreenNum(TheLibrary event, int num) {
        try {
            java.lang.reflect.Field f = TheLibrary.class.getDeclaredField("screenNum");
            f.setAccessible(true);
            f.setInt(event, num);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}