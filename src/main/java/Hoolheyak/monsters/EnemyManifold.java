package Hoolheyak.monsters;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.EnemyPureWaterPower;
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
    private boolean isSelfDestructing = false;

    public EnemyManifold(float x, float y) {
        super(NAME, ID, 24, 0.0F, 0.0F, 120.0F, 200.0F, null, x, y);

        if (AbstractDungeon.ascensionLevel >= 9) {
            this.setHp(30);
        } else {
            this.setHp(24);
        }

        this.damage.add(new DamageInfo(this, 3));

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
        stateData38.setMix("A_Idle", "A_Attack", 0.1f);
        stateData38.setMix("A_Attack", "A_Idle", 0.1f);
        stateData38.setMix("A_Idle", "A_Die", 0.1f);
        state38.setAnimation(0, "A_Idle", true);
    }

    @Override
    public void takeTurn() {
        if (this.halfDead) return;

        switch (this.nextMove) {
            case 1: // 3x3 攻击
                if (state38 != null) {
                    state38.setAnimation(0, "A_Attack", false); // A系列攻击
                    state38.addAnimation(0, "A_Idle", true, 0f); // 回归A系列待机
                }
                for (int i = 0; i < 3; i++) {
                    // 这里现在能安全拿到 this.damage.get(0) 了
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

    public void playDeathAnimation() {
        if (state38 != null) {
            state38.setAnimation(0, "A_Die", false);          // 死亡倒下
            state38.addAnimation(0, "Start", false, 0f);      // 化作水滩的过程
            state38.addAnimation(0, "Idle_1", true, 0f);      // 水滩待机循环
        }
    }

    // 【核心机制：假死复活】
    @Override
    public void damage(DamageInfo info) {
        // 正常扣血。如果血量归零，原版机制会在这里自动调用 this.die()
        super.damage(info);
    }

    @Override
    public void die() {
        // 当 super.damage() 触发致死时，来到这里，此时 halfDead 还是 false
        if (!this.halfDead) {
            // 【关键拦截】拦截掉 super.die()，不让它清空 powers
            this.halfDead = true;
            this.isDead = false;
            this.isDying = false;
            this.currentHealth = 0;

            this.playDeathAnimation();

            boolean otherManifoldsAlive = false;
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                // 注意排除自己
                if (m instanceof EnemyManifold && !m.halfDead && !m.isDead && m != this) {
                    otherManifoldsAlive = true;
                    break;
                }
            }

            if (!otherManifoldsAlive) {
                // 如果其他流形全灭，一起死
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (m instanceof EnemyManifold && m.halfDead) {
                        m.halfDead = false;
                        m.die(); // 这里会递归调用 die()，但此时 halfDead 为 false，会走下方的 else 分支彻底死亡
                    }
                }
            } else {
                this.reviveTimer = 3;
                this.setMove((byte) 3, Intent.UNKNOWN);
                this.createIntent();
                AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.TextAboveCreatureEffect(this.hb.cX, this.hb.cY, "复活倒计时: 3", com.badlogic.gdx.graphics.Color.WHITE));
            }
        } else {
            // 真正死亡时，才允许执行原版的清理和彻底退场逻辑
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

        // ===== 【核心修复】：检测缪尔赛斯是否存活 =====
        if (!this.isDead && !this.isSelfDestructing) {
            boolean bossAlive = false;
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                // 遍历寻找缪尔赛斯。如果找到且没死，说明 Boss 还在
                if (m.id.equals(Muelsyse.ID) && !m.isDead && !m.isDying) {
                    bossAlive = true;
                    break;
                }
            }

            // 如果缪尔赛斯死了，流形立刻自我销毁
            if (!bossAlive) {
                this.isSelfDestructing = true;
                this.halfDead = false; // 【关键】必须先解除假死状态，否则下面的 die() 无法生效

                if (state38 != null) {
                    state38.setAnimation(0, "A_Die", false); // 顺便播个死亡动画化成水
                }

                // 直接调用死亡方法彻底销毁
                this.die();
            }
        }
        // ===============================================
    }

    // 我们拦截回合结束的逻辑，在这里递减复活倒计时
    public void handleReviveCountdown() {
        if (this.halfDead && !this.isDead) {
            this.reviveTimer--;

            if (this.reviveTimer > 0) {
                // 【关键修复 3】：倒数时飘字提示
                AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.TextAboveCreatureEffect(this.hb.cX, this.hb.cY, "复活倒计时: " + this.reviveTimer, com.badlogic.gdx.graphics.Color.WHITE));
            } else {
                // 复活逻辑
                this.halfDead = false;
                this.heal(this.maxHealth / 2);
                if (state38 != null) {
                    // 假设 "End" 是从水滩聚拢成人的动画
                    state38.setAnimation(0, "A_Start", false);
                    state38.addAnimation(0, "A_Idle", true, 0f); // 变完人之后，继续站立待机
                }
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
        }

        if (!this.halfDead) {
            this.hb.render(sb);
            this.intentHb.render(sb);
            this.healthHb.render(sb);

            if (!com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.isDead) {
                this.renderHealth(sb);
                // 渲染名字
                basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderName", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
            }

            if (!this.powers.isEmpty()) {
                float offset = 10.0F * com.megacrit.cardcrawl.core.Settings.scale;
                // 先画图标
                for (com.megacrit.cardcrawl.powers.AbstractPower p : this.powers) {
                    p.renderIcons(sb, this.hb.cX - this.hb.width / 2.0F + offset, this.hb.cY - this.hb.height / 2.0F - 48.0F * com.megacrit.cardcrawl.core.Settings.scale, com.badlogic.gdx.graphics.Color.WHITE);
                    offset += 48.0F * com.megacrit.cardcrawl.core.Settings.scale;
                }
                offset = 10.0F * com.megacrit.cardcrawl.core.Settings.scale;
                // 再画层数数字 (如果有的话)
                for (com.megacrit.cardcrawl.powers.AbstractPower p : this.powers) {
                    p.renderAmount(sb, this.hb.cX - this.hb.width / 2.0F + offset, this.hb.cY - this.hb.height / 2.0F - 48.0F * com.megacrit.cardcrawl.core.Settings.scale, com.badlogic.gdx.graphics.Color.WHITE);
                    offset += 48.0F * com.megacrit.cardcrawl.core.Settings.scale;
                }
            }

            if (!this.isDying && !this.isEscaping && com.megacrit.cardcrawl.dungeons.AbstractDungeon.getCurrRoom().phase == com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.isDead && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.hasRelic("Runic Dome") && this.intent != Intent.NONE && !com.megacrit.cardcrawl.core.Settings.hideCombatElements) {
                basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntentVfxBehind", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
                basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntent", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
                basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntentVfxAfter", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);

                basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderDamageRange", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
            }
        }
    }
}