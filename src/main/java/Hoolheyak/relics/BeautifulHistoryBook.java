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
            if (foughtEnemies.contains(m.id)) {
                activated = true;
                // 失去 50% 最大生命值，且不能低于 1
                int hpLoss = m.maxHealth / 2;
                m.decreaseMaxHealth(hpLoss);
                addToBot(new RelicAboveCreatureAction(m, this));
            } else {
                // 如果是新敌人，加入记录
                foughtEnemies.add(m.id);
            }
        }
        if (activated) {
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