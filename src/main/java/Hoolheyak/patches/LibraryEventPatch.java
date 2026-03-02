package Hoolheyak.patches;

import Hoolheyak.HoolheyakMod;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
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
        @SpireInsertPatch(rloc = 25) // 插入在原版 switch 语句前
        public static void Insert(TheLibrary __instance, int buttonPressed) {
            // 如果处于初始选择画面（screenNum == 0）且按下了第 3 个按钮
            if (buttonPressed == 2 && getScreenNum(__instance) == 0) {
                __instance.imageEventText.updateBodyText(eventStrings.DESCRIPTIONS[0]);
                __instance.imageEventText.clearAllDialogs();
                __instance.imageEventText.setDialogOption(TheLibrary.OPTIONS[3]); // 原版的“离开”选项

                // 生成 5 张随机职业的稀有牌
                CardGroup group = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
                ArrayList<AbstractCard> cards = new ArrayList<>();
                while (cards.size() < 5) {
                    AbstractCard c = AbstractDungeon.getCard(AbstractCard.CardRarity.RARE).makeCopy();
                    boolean dupe = false;
                    for (AbstractCard existing : cards) {
                        if (existing.cardID.equals(c.cardID)) {
                            dupe = true;
                            break;
                        }
                    }
                    if (!dupe) {
                        cards.add(c);
                        group.addToBottom(c);
                    }
                }

                // 打开选牌界面
                AbstractDungeon.gridSelectScreen.open(group, 1, "选择一张牌加入你的牌组", false);
                recordChosen = true;
                setScreenNum(__instance, 1); // 切换状态
            }
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