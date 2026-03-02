package Hoolheyak.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class MonsterIntentPatch {
    // 1. 给怪物类追加一个字段，用于记录上回合的意图
    @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CLASS)
    public static class LastIntentField {
        public static SpireField<AbstractMonster.Intent> lastIntent = new SpireField<>(() -> AbstractMonster.Intent.NONE);
    }

    // 2. 拦截怪物的 rollMove，在怪物刷新新意图之前，把当前的意图存下来
    @SpirePatch(clz = AbstractMonster.class, method = "rollMove")
    public static class SaveLastIntentPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractMonster __instance) {
            // 将怪物刷新前的意图（即上回合意图）备份进新字段中
            LastIntentField.lastIntent.set(__instance, __instance.intent);
        }
    }
}