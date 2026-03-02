package Hoolheyak.monsters;

import Hoolheyak.HoolheyakMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.common.SetMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.Hoolheyak.spine38.*;

public class EnemyManifold extends AbstractMonster {
    public static final String ID = HoolheyakMod.makeID("EnemyManifold");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;

    // Spine 3.8
    protected TextureAtlas atlas38;
    protected Skeleton skeleton38;
    public AnimationState state38;
    protected AnimationStateData stateData38;
    protected static final PolygonSpriteBatch psb = new PolygonSpriteBatch();
    protected static final SkeletonRenderer sr = new SkeletonRenderer();
    static { sr.setPremultipliedAlpha(true); }

    private int reviveTimer = 0;

    public EnemyManifold(float x, float y) {
        super(NAME, ID, 30, 0.0F, 0.0F, 120.0F, 200.0F, null, x, y);

        if (AbstractDungeon.ascensionLevel >= 9) {
            this.setHp(35);
        } else {
            this.setHp(30);
        }

        loadSpine();
    }

    private void loadSpine() {
        atlas38 = new TextureAtlas(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.atlas"));
        SkeletonJson json = new SkeletonJson(atlas38);
        json.setScale(Settings.renderScale / 1.5f);
        SkeletonData data = json.readSkeletonData(Gdx.files.internal("Hoolheyak/char/Mlyss/token_10030_mlyss_wtrman.json"));
        skeleton38 = new Skeleton(data);
        skeleton38.setColor(Color.WHITE);

        // 敌方单位朝左
        skeleton38.setScaleX(-Math.abs(skeleton38.getScaleX()));

        stateData38 = new AnimationStateData(data);
        state38 = new AnimationState(stateData38);
        stateData38.setDefaultMix(0.2f);
        stateData38.setMix("Idle_1", "Attack", 0.1f);
        stateData38.setMix("Attack", "Idle_1", 0.1f);
        stateData38.setMix("Idle_1", "Die", 0.1f);
        state38.setAnimation(0, "Idle_1", true);
    }

    @Override
    public void usePreBattleAction() {
        // 爪牙，不会引发掉落
        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(this, this, new MinionPower(this)));
    }

    @Override
    public void takeTurn() {
        if (this.halfDead) return;

        switch (this.nextMove) {
            case 1: // 3x3 攻击
                if (state38 != null) {
                    state38.setAnimation(0, "Attack", false);
                    state38.addAnimation(0, "Idle_1", true, 0f);
                }
                for (int i = 0; i < 3; i++) {
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                }
                break;
            case 2: // 全体加1力量
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (!m.isDead && !m.halfDead) {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, this, new StrengthPower(m, 1), 1));
                    }
                }
                break;
        }
        AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.RollMoveAction(this));
    }

    @Override
    protected void getMove(int num) {
        if (this.halfDead) {
            this.setMove((byte) 3, Intent.UNKNOWN);
            return;
        }
        if (num < 50) {
            this.setMove((byte) 1, Intent.ATTACK, 3, 3, true);
        } else {
            this.setMove((byte) 2, Intent.BUFF);
        }
    }

    // 【核心机制：假死复活】
    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        if (this.currentHealth <= 0 && !this.halfDead) {
            this.halfDead = true; // 进入假死
            if (state38 != null) state38.setAnimation(0, "Die", false);

            // 检查场上是否还有活着的“流形”
            boolean otherManifoldsAlive = false;
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                if (m instanceof EnemyManifold && !m.halfDead && !m.isDead) {
                    otherManifoldsAlive = true;
                    break;
                }
            }

            if (!otherManifoldsAlive) {
                // 如果没有活着的流形了，所有的流形真正死亡
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (m instanceof EnemyManifold && m.halfDead) {
                        m.halfDead = false;
                        m.die();
                    }
                }
            } else {
                // 还有其他流形，启动3回合倒计时
                this.reviveTimer = 3;
                this.setMove((byte) 3, Intent.UNKNOWN);
                this.createIntent();
            }
        }
    }

    @Override
    public void die() {
        if (!this.halfDead) {
            super.die();
        }
    }

    @Override
    public void update() {
        super.update();
        if (state38 != null) {
            state38.update(Gdx.graphics.getDeltaTime());
            state38.apply(skeleton38);
            skeleton38.updateWorldTransform();
            skeleton38.setPosition(this.drawX + this.animX, this.drawY + this.animY);
            skeleton38.setColor(this.tint.color);
        }

        // 倒计时复活逻辑（在回合开始更新）
        if (this.halfDead && !this.isDead) {
            if (AbstractDungeon.actionManager.turnHasEnded) {
                // do nothing
            }
        }
    }

    // 我们拦截回合结束的逻辑，在这里递减复活倒计时
    public void handleReviveCountdown() {
        if (this.halfDead && !this.isDead) {
            this.reviveTimer--;
            if (this.reviveTimer <= 0) {
                this.halfDead = false;
                this.heal(this.maxHealth / 2); // 50%血量复活
                if (state38 != null) state38.setAnimation(0, "Idle_1", true); // 站起来
                AbstractDungeon.actionManager.addToBottom(new SetMoveAction(this, (byte) 1, Intent.ATTACK, 3, 3, true));
            }
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
            // 如果是敌方流形，记得加上 !this.halfDead 判断；缪尔赛斯本体不需要判断 halfDead
            this.hb.render(sb);
            this.renderHealth(sb);
        }

        // 【关键修复】：利用 BaseMod 反射强行调用 AbstractMonster 的私有方法绘制意图
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntentVfxBehind", SpriteBatch.class).invoke(this, sb);
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntent", SpriteBatch.class).invoke(this, sb);
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntentVfxAfter", SpriteBatch.class).invoke(this, sb);
    }
}