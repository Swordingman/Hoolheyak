package Hoolheyak.character;

import Hoolheyak.HoolheyakMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.Hoolheyak.spine38.AnimationState;
import com.Hoolheyak.spine38.AnimationStateData;
import com.Hoolheyak.spine38.Skeleton;
import com.Hoolheyak.spine38.SkeletonJson;
import com.Hoolheyak.spine38.SkeletonData;
import com.Hoolheyak.spine38.SkeletonRenderer;
import com.megacrit.cardcrawl.localization.MonsterStrings;

public class FriendlyManifold extends AbstractCreature {
    protected TextureAtlas atlas38;
    protected Skeleton skeleton38;
    public AnimationState state38;
    protected AnimationStateData stateData38;

    protected static final PolygonSpriteBatch psb = new PolygonSpriteBatch();
    protected static final SkeletonRenderer sr = new SkeletonRenderer();
    static { sr.setPremultipliedAlpha(true); }

    public static final String ID = HoolheyakMod.makeID("FriendlyManifold");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;

    public FriendlyManifold() {
        this.name = NAME; // ✅ 名字改为流形
        this.id = ID;
        this.maxHealth = 16;
        this.currentHealth = 16;

        this.drawX = AbstractDungeon.player.drawX + 180.0F * Settings.scale;
        this.drawY = AbstractDungeon.player.drawY;

        this.hb_x = -40.0F * Settings.scale;
        this.hb_y = 0;
        this.hb_w = 120.0F * Settings.scale;
        this.hb_h = 200.0F * Settings.scale;

        loadSpine();
        this.refreshHitboxLocation();
        this.healthBarUpdatedEvent();
    }

    private void loadSpine() {
        atlas38 = new TextureAtlas(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.atlas"));
        SkeletonJson json = new SkeletonJson(atlas38);
        json.setScale(Settings.renderScale / 1.5f);
        SkeletonData data = json.readSkeletonData(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.json"));
        skeleton38 = new Skeleton(data);
        skeleton38.setColor(Color.WHITE);
        stateData38 = new AnimationStateData(data);
        state38 = new AnimationState(stateData38);

        stateData38.setDefaultMix(0.2f);
        stateData38.setMix("Idle_1", "Attack", 0.1f);
        stateData38.setMix("Attack", "Idle_1", 0.1f);
        stateData38.setMix("Idle_1", "Die", 0.1f);

        state38.setAnimation(0, "Idle_1", true);
    }

    public void playAttackAnimation() {
        if (state38 != null) {
            state38.setAnimation(0, "Attack", false);
            state38.addAnimation(0, "Idle_1", true, 0f);
        }
    }

    public void playDeathAnimation() {
        if (state38 != null) state38.setAnimation(0, "Die", false);
    }

    @Override
    public void damage(com.megacrit.cardcrawl.cards.DamageInfo info) {
        int damageAmount = info.output;

        // 1. 先扣除格挡
        if (this.currentBlock > 0) {
            if (damageAmount > this.currentBlock) {
                damageAmount -= this.currentBlock;
                this.currentBlock = 0;
            } else {
                this.currentBlock -= damageAmount;
                damageAmount = 0;
            }
        }

        // 2. 如果伤害穿透了格挡，扣除生命值
        if (damageAmount > 0) {
            this.currentHealth -= damageAmount;
            this.healthBarUpdatedEvent(); // 刷新血条UI
        }

        // 3. 死亡判定
        if (this.currentHealth <= 0 && !this.isDead) {
            this.currentHealth = 0;
            this.isDead = true;
            this.currentBlock = 0;
            playDeathAnimation(); // 播放死亡动画
        }
    }

    public void update() {
        this.updateHealthBar();
        this.hb.update();
        if (state38 != null) {
            state38.update(Gdx.graphics.getDeltaTime());
            state38.apply(skeleton38);
            skeleton38.updateWorldTransform();
            skeleton38.setPosition(this.drawX + this.animX, this.drawY + this.animY);
            skeleton38.setColor(this.tint.color);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (!this.isDead || (state38 != null && !state38.getCurrent(0).isComplete())) {
            sb.end();
            psb.begin();
            sr.draw(psb, skeleton38);
            psb.end();
            sb.begin();
            this.hb.render(sb);
            this.renderHealth(sb);
        }
    }
}