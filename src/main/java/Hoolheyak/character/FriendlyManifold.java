package Hoolheyak.character;

import Hoolheyak.HoolheyakMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.vfx.combat.StrikeEffect;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.effectList;

public class FriendlyManifold extends AbstractCreature {
    protected TextureAtlas atlas38;
    protected Skeleton skeleton38;
    public AnimationState state38;
    protected AnimationStateData stateData38;

    protected static PolygonSpriteBatch psb = null;
    protected static SkeletonRenderer sr = null;

    public static final String ID = HoolheyakMod.makeID("FriendlyManifold");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;

    public int nextMove = -1;

    private float healthBarHeightOffset = 20.0F * Settings.scale;

    public FriendlyManifold() {
        this.name = NAME;
        this.id = ID;
        this.maxHealth = 16;
        this.currentHealth = 16;
        this.isPlayer = false;

        this.drawX = AbstractDungeon.player.drawX + 180.0F * Settings.scale;
        this.drawY = AbstractDungeon.player.drawY;

        this.hb_x = -40.0F * Settings.scale;
        this.hb_y = 0;
        this.hb_w = 120.0F * Settings.scale;
        this.hb_h = 280.0F * Settings.scale;

        this.hb = new com.megacrit.cardcrawl.helpers.Hitbox(this.hb_w, this.hb_h);
        this.healthHb = new com.megacrit.cardcrawl.helpers.Hitbox(this.hb_w, 72.0F * Settings.scale);

        this.nextMove = -1;

        try {
            loadSpine();
        } catch (Exception e) {
            System.out.println(">>> [报错] Spine 模型加载失败: " + e.getMessage());
        }

        this.refreshHitboxLocation();
        this.showHealthBar();
        this.healthBarUpdatedEvent();
    }

    private void loadSpine() {
        atlas38 = new TextureAtlas(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.atlas"));
        SkeletonJson json = new SkeletonJson(atlas38);
        json.setScale(Settings.scale / 1.5f);
        SkeletonData data = json.readSkeletonData(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.json"));
        skeleton38 = new Skeleton(data);

        skeleton38.setToSetupPose();
        skeleton38.setColor(Color.WHITE.cpy());

        stateData38 = new AnimationStateData(data);
        state38 = new AnimationState(stateData38);
        stateData38.setDefaultMix(0.2f);

        state38.setAnimation(0, "Change_A", false);
        state38.addAnimation(0, "A_Idle", true, 0f);
    }

    public void playAttackAnimation() {
        if (state38 != null) {
            state38.setAnimation(0, "A_Attack", false);
            state38.addAnimation(0, "A_Idle", true, 0f);
        }
    }

    public void playDeathAnimation() {
        if (state38 != null) {
            state38.setAnimation(0, "A_Die", false);          // 死亡倒下
            state38.addAnimation(0, "Start", false, 0f);      // 化作水滩的过程
            state38.addAnimation(0, "Idle_1", true, 0f);      // 水滩待机循环
        }
    }

    public void playReviveAnimation() {
        if (state38 != null) {
            state38.setAnimation(0, "A_Start", false);        // 从水滩重新站起
            state38.addAnimation(0, "A_Idle", true, 0f);      // 恢复正常待机
        }
    }

    @Override
    public void damage(com.megacrit.cardcrawl.cards.DamageInfo info) {
        int damageAmount = info.output;

        if (damageAmount > 0) {
            effectList.add(new StrikeEffect(this, this.hb.cX, this.hb.cY, damageAmount));
        }

        if (this.currentBlock > 0) {
            if (damageAmount > this.currentBlock) {
                damageAmount -= this.currentBlock;
                this.currentBlock = 0;
            } else {
                this.currentBlock -= damageAmount;
                damageAmount = 0;
            }
        }
        if (damageAmount > 0) {
            this.currentHealth -= damageAmount;
            this.healthBarUpdatedEvent();
        }
        if (this.currentHealth <= 0 && !this.isDead) {
            this.currentHealth = 0;
            this.isDead = true;
            this.currentBlock = 0;
            playDeathAnimation();
        }
    }

    public void update() {
        this.hb.move(this.drawX + this.hb_x + this.hb_w / 2.0F, this.drawY + this.hb_y + this.hb_h / 2.0F);
        this.healthHb.move(this.hb.cX, this.hb.cY - this.hb_h / 2.0F - this.healthBarHeightOffset);

        this.hb.update();
        this.healthHb.update();
        this.updateHealthBar();

        for (int i = 0; i < this.powers.size(); i++) {
            this.powers.get(i).update(i);
        }

        if (state38 != null && skeleton38 != null) {
            try {
                state38.update(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
                state38.apply(skeleton38);
                skeleton38.setPosition(this.drawX + this.animX, this.drawY + this.animY);
                skeleton38.updateWorldTransform();
                skeleton38.setColor(com.badlogic.gdx.graphics.Color.WHITE.cpy());
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if (skeleton38 != null) {
            try {
                if (psb == null) {
                    psb = new PolygonSpriteBatch();
                    sr = new SkeletonRenderer();
                    sr.setPremultipliedAlpha(false);
                }
                sb.end();
                psb.setProjectionMatrix(sb.getProjectionMatrix());
                psb.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
                psb.begin();
                sr.draw(psb, skeleton38);
                psb.end();
                sb.begin();
            } catch (Exception e) {
                if (!sb.isDrawing()) sb.begin();
            }
        }

        if (this.hb.hovered) {
            this.renderPowerTips(sb);
        }

        if (!this.isDead) {
            sb.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            sb.setColor(com.badlogic.gdx.graphics.Color.WHITE.cpy());

            this.hb.render(sb);
            this.renderHealth(sb);
            sb.setColor(com.badlogic.gdx.graphics.Color.WHITE.cpy());

            // 绘制意图
            if (this.nextMove != -1) {
                float intentSize = 64f * Settings.scale;
                float intentX = this.drawX - (intentSize / 2f);
                float intentY = this.drawY + this.hb_h + 50f * Settings.scale;

                if (this.nextMove == 0) {
                    int actualDamage = 3 + (this.hasPower(StrengthPower.POWER_ID) ? this.getPower(StrengthPower.POWER_ID).amount : 0);
                    actualDamage = Math.max(0, actualDamage);
                    String intentMsg = actualDamage + "x3";
                    sb.draw(ImageMaster.INTENT_ATK_1, intentX, intentY, intentSize, intentSize);
                    FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, intentMsg, this.drawX, intentY, com.badlogic.gdx.graphics.Color.WHITE.cpy());
                } else if (this.nextMove == 1) {
                    sb.draw(ImageMaster.INTENT_DEFEND, intentX, intentY, intentSize, intentSize);
                    FontHelper.renderFontCentered(sb, FontHelper.topPanelInfoFont, "12", this.drawX, intentY, com.badlogic.gdx.graphics.Color.WHITE.cpy());
                } else if (this.nextMove == 2) {
                    sb.draw(ImageMaster.INTENT_BUFF, intentX, intentY, intentSize, intentSize);
                }
            }
        }
    }

    @Override
    public boolean isDeadOrEscaped() {
        return this.isDead;
    }
}