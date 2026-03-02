package Hoolheyak.patches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;
import com.megacrit.cardcrawl.localization.MonsterStrings;

import java.util.ArrayList;

public class HistoryBookScreenEffect extends AbstractGameEffect {
    private ArrayList<String> enemyNames = new ArrayList<>();

    public HistoryBookScreenEffect(ArrayList<String> enemyIds) {
        this.duration = 0.5f;
        // 背景颜色：黑色，透明度 85%
        this.color = new Color(0, 0, 0, 0.85f);

        // 遍历 ID 列表，尝试转化为本地化的怪物名字
        for (String id : enemyIds) {
            String name = id; // 默认显示 ID 作为保底
            try {
                MonsterStrings ms = CardCrawlGame.languagePack.getMonsterStrings(id);
                if (ms != null) {
                    name = ms.NAME;
                }
            } catch (Exception e) {
                // 如果找不到名字，忽略报错，直接显示 ID
            }
            // 去重（比如三柱神 ID 不同但名字一样）
            if (!enemyNames.contains(name)) {
                enemyNames.add(name);
            }
        }
    }

    @Override
    public void update() {
        // 当玩家点击鼠标左键或右键时，关闭这个界面
        if (InputHelper.justClickedLeft || InputHelper.justClickedRight) {
            this.isDone = true;
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        // 1. 绘制暗色全屏背景
        sb.setColor(this.color);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        // 2. 绘制大标题
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, "已记录的敌人文献 (点击任意处关闭)", Settings.WIDTH / 2.0f, Settings.HEIGHT - 100f * Settings.scale, Settings.GOLD_COLOR);

        // 3. 分列渲染敌人名字 (每列最多排 15 个名字)
        float startY = Settings.HEIGHT - 200f * Settings.scale;
        for (int i = 0; i < enemyNames.size(); i++) {
            // 列数算法
            float x = Settings.WIDTH / 2.0f - 300f * Settings.scale + (i / 15) * 300f * Settings.scale;
            // 行数算法
            float y = startY - (i % 15) * 40f * Settings.scale;

            FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipBodyFont, enemyNames.get(i), x, y, Settings.CREAM_COLOR);
        }
    }

    @Override
    public void dispose() {
    }
}