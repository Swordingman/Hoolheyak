package Hoolheyak.vfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

import java.util.ArrayList;
import java.util.Iterator;

public class PhaseAuroraEffect extends AbstractGameEffect {
    private String phasePowerId;
    private Color mainColor;
    private float alpha;
    private float maxAlpha;

    private int layerCount;
    private float[] waveTimers;
    private float[] waveSpeeds;
    private float[] xOffsets;
    private float[] widths;
    private float[] baseAngles;

    private TextureRegion spotlightRegion;
    private TextureRegion starRegion;

    // 【新增】星光碎屑的容器
    private ArrayList<StarParticle> particles = new ArrayList<>();

    public PhaseAuroraEffect(Color color, String powerId, float maxAlpha) {
        this.mainColor = color.cpy();
        this.phasePowerId = powerId;
        this.alpha = 0f;
        this.maxAlpha = maxAlpha;
        this.renderBehind = false;

        this.spotlightRegion = new TextureRegion(ImageMaster.SPOTLIGHT_VFX);
        this.starRegion = new TextureRegion(ImageMaster.WHITE_SQUARE_IMG);

        this.layerCount = MathUtils.random(8, 12);
        this.waveTimers = new float[this.layerCount];
        this.waveSpeeds = new float[this.layerCount];
        this.xOffsets = new float[this.layerCount];
        this.widths = new float[this.layerCount];
        this.baseAngles = new float[this.layerCount];

        for (int i = 0; i < this.layerCount; i++) {
            this.waveTimers[i] = MathUtils.random(0f, 100f);
            this.waveSpeeds[i] = MathUtils.random(0.2f, 0.6f);
            this.xOffsets[i] = MathUtils.random(-60f, 60f) * Settings.scale;
            this.widths[i] = MathUtils.random(Settings.WIDTH * 0.08f, Settings.WIDTH * 0.25f);
            this.baseAngles[i] = MathUtils.random(-55f, 55f);
        }
    }

    @Override
    public void update() {
        // 更新光柱时间
        for (int i = 0; i < this.layerCount; i++) {
            this.waveTimers[i] += Gdx.graphics.getDeltaTime() * this.waveSpeeds[i];
        }

        // 绑定状态逻辑
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasPower(this.phasePowerId)) {
            if (this.alpha < this.maxAlpha) {
                this.alpha += Gdx.graphics.getDeltaTime() * 0.5f;
            }
            // 【新增】只有当主极光存在且亮度较高时，才持续生成星光碎屑
            if (this.alpha > this.maxAlpha * 0.3f) {
                // 每帧有 40% 的概率生成 1~2 颗星星（调整概率可以控制碎屑的密集程度）
                if (MathUtils.randomBoolean(0.4f)) {
                    int spawnCount = MathUtils.random(1, 2);
                    for (int i = 0; i < spawnCount; i++) {
                        particles.add(new StarParticle(this.mainColor));
                    }
                }
            }
        } else {
            this.alpha -= Gdx.graphics.getDeltaTime() * 1.5f;
            if (this.alpha <= 0f && particles.isEmpty()) { // 等到所有碎屑也消失后，再彻底结束特效
                this.isDone = true;
            }
        }

        // 【新增】更新所有的星光碎屑
        Iterator<StarParticle> i = particles.iterator();
        while (i.hasNext()) {
            StarParticle p = i.next();
            p.update(Gdx.graphics.getDeltaTime());
            // 如果粒子生命周期结束，将其销毁
            if (p.life <= 0f) {
                i.remove();
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        // 1. 绘制暗场
        if (this.alpha > 0) {
            sb.setBlendFunction(770, 771);
            sb.setColor(new Color(0f, 0f, 0f, 0.4f * (this.alpha / this.maxAlpha)));
            sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);

            // 2. 绘制扇形光柱
            sb.setBlendFunction(770, 1);
            for (int i = 0; i < this.layerCount; i++) {
                float layerAlpha = this.alpha + MathUtils.sin(this.waveTimers[i]) * 0.15f;
                layerAlpha = MathUtils.clamp(layerAlpha, 0f, 1f);
                Color layerColor = this.mainColor.cpy();
                layerColor.a = layerAlpha * 0.12f;

                if (i % 3 == 1) layerColor.r = MathUtils.clamp(layerColor.r + 0.15f, 0f, 1f);
                if (i % 3 == 2) layerColor.b = MathUtils.clamp(layerColor.b + 0.15f, 0f, 1f);

                sb.setColor(layerColor);

                float swayAngle = this.baseAngles[i] + MathUtils.sin(this.waveTimers[i] * 0.4f) * 12f;
                float lightWidth = this.widths[i];
                float lightHeight = Settings.HEIGHT * 1.2f;

                float x = Settings.WIDTH * 0.5f - lightWidth / 2f + this.xOffsets[i];
                float y = Settings.HEIGHT - lightHeight + (40f * Settings.scale);

                sb.draw(this.spotlightRegion,
                        x, y,
                        lightWidth / 2f, lightHeight,
                        lightWidth, lightHeight,
                        1.0f, 1.0f,
                        swayAngle);
            }
        }

        // 3. 【新增】绘制飘落的星光碎屑
        // 将粒子绘制放在光柱后面，且仍然使用加法混合 (770, 1)，让粒子发光
        sb.setBlendFunction(770, 1);
        float masterAlpha = this.alpha / this.maxAlpha;
        for (StarParticle p : particles) {
            p.render(sb, masterAlpha);
        }
        sb.setBlendFunction(770, 771); // 最后恢复正常混合模式
    }

    @Override
    public void dispose() {}

    // ==========================================
    // 【内部类】专门用来处理飘落星星的逻辑
    // ==========================================
    private class StarParticle {
        float x, y, vY, vX, scale, life, maxLife;
        float twinkleOffset, twinkleSpeed;
        Color color;

        public StarParticle(Color baseColor) {
            // 在屏幕中上部随机生成
            this.x = Settings.WIDTH * 0.5f + MathUtils.random(-600f, 600f) * Settings.scale;
            this.y = Settings.HEIGHT + MathUtils.random(10f, 100f) * Settings.scale;

            // 下落速度 (vY) 和 左右横移的微风速度 (vX)
            this.vY = MathUtils.random(-60f, -180f) * Settings.scale;
            this.vX = MathUtils.random(-30f, 30f) * Settings.scale;

            // 粒子的大小 (很小的光点)
            this.scale = MathUtils.random(2.0f, 6.0f) * Settings.scale;

            // 粒子的存活时间：大概 2~4 秒后消失
            this.maxLife = MathUtils.random(2.0f, 6.0f);
            this.life = this.maxLife;

            // 控制星星闪烁频率的随机值
            this.twinkleOffset = MathUtils.random(0f, 10f);
            this.twinkleSpeed = MathUtils.random(3f, 8f);

            // 继承主色调，但让它稍微偏白一点，显得更加明亮和刺眼
            this.color = baseColor.cpy();
            this.color.r = MathUtils.clamp(this.color.r + 0.3f, 0f, 1f);
            this.color.g = MathUtils.clamp(this.color.g + 0.3f, 0f, 1f);
            this.color.b = MathUtils.clamp(this.color.b + 0.3f, 0f, 1f);
        }

        public void update(float dt) {
            this.x += this.vX * dt;
            this.y += this.vY * dt;
            this.life -= dt;
        }

        public void render(SpriteBatch sb, float masterAlpha) {
            // 1. 根据寿命计算基础透明度 (淡入淡出)
            float a = this.life / this.maxLife;
            if (this.maxLife - this.life < 0.5f) {
                a = (this.maxLife - this.life) / 0.5f; // 刚生成时的0.5秒快速淡入
            }

            // 2. 加入正弦波计算闪烁 (Twinkle)
            float twinkle = (MathUtils.sin(this.life * this.twinkleSpeed + this.twinkleOffset) + 1f) / 2f;
            a *= (0.3f + 0.7f * twinkle);

            this.color.a = a * masterAlpha;
            sb.setColor(this.color);

            // ==========================================
            // 【核心修改点】在此处实现非均匀缩放
            // ==========================================

            // 原来只有一维的 this.scale。
            // 现在我们要让它变成小长条：中间粗（ScaleX小），两头窄（通过拉长Y轴产生的视觉错觉）

            // X轴：宽度，设定为一个极小值，让小方块变成一条线
            float scaleX = 0.5f * Settings.scale;
            // Y轴：高度，设定为原来的 6 到 10 倍，拉出一个长条
            float scaleY = this.scale * MathUtils.random(2.0f, 3.0f) * Settings.scale;

            // 3. 使用外层共享的 starRegion 进行高级绘制
            sb.draw(starRegion,
                    this.x, this.y,
                    0.5f, 0.5f,  // 旋转和缩放的轴心（原点）：粒子正中央
                    1f, 1f,      // 图片的原始宽高 (1x1像素)
                    scaleX, scaleY, // 【修改这里】传入不同的X、Y缩放比例
                    this.life * 50f); // 粒子旋转
        }
    }
}