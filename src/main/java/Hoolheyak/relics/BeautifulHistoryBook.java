package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.patches.HistoryBookScreenEffect;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class BeautifulHistoryBook extends BaseRelic implements CustomSavable<ArrayList<String>> {
    public static final String ID = HoolheyakMod.makeID("BeautifulHistoryBook");

    // 记录交战过的敌人 ID 列表
    public ArrayList<String> foughtEnemies = new ArrayList<>();

    public BeautifulHistoryBook() {
        super(ID, "BeautifulHistoryBook", RelicTier.RARE, LandingSound.HEAVY);
    }

    @Override
    public void atBattleStart() {
        boolean activated = false;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            // 只负责检查和扣血
            if (foughtEnemies.contains(m.id)) {
                activated = true;
                // 失去 50% 最大生命值
                int hpLoss = m.maxHealth / 2;
                m.decreaseMaxHealth(hpLoss);
                addToBot(new RelicAboveCreatureAction(m, this));
            }
        }
        if (activated) {
            this.flash();
        }
    }

    // 在战斗胜利（结束）时记录敌人 ID
    @Override
    public void onVictory() {
        boolean recordedNew = false;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            // 如果列表里还没有这个敌人的 ID，则加入
            if (!foughtEnemies.contains(m.id)) {
                foughtEnemies.add(m.id);
                recordedNew = true;
            }
        }

        // 可选：如果记录了新敌人，遗物闪烁一下提示玩家
        if (recordedNew) {
            this.flash();
        }
    }

    // --- 纯原版右键检测 (无需 StSLib) ---
    @Override
    public void update() {
        super.update();
        // 如果鼠标悬停在遗物上，且按下了右键
        if (this.hb.hovered && InputHelper.justClickedRight) {
            CardCrawlGame.sound.play("UI_CLICK_1"); // 播放点击音效
            onRightClick();
        }
    }

    private void onRightClick() {
        // 调用我们手写的 UI 特效屏幕
        AbstractDungeon.topLevelEffects.add(new HistoryBookScreenEffect(this.foughtEnemies));
    }

    // --- BaseMod 存档支持 ---
    @Override
    public ArrayList<String> onSave() {
        return foughtEnemies;
    }

    @Override
    public void onLoad(ArrayList<String> strings) {
        if (strings != null) {
            this.foughtEnemies = strings;
        }
    }
}