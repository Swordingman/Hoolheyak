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
        // 【新增最优先判定】：先看缪尔赛斯死了没
        boolean bossAlive = false;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m.id.equals(Muelsyse.ID) && !m.isDead && !m.isDying) {
                bossAlive = true;
                break;
            }
        }

        // 1. 如果缪尔赛斯已经挂了（或者正在死），流形没有任何理由假死，直接真死退场！
        if (!bossAlive) {
            this.halfDead = false; // 确保不会拦截原版死亡逻辑
            super.die();
            return; // 极其重要：直接 return 截断后面的假死逻辑，保证死得透透的
        }

        // 2. 缪尔赛斯还活着，走正常的假死与全灭判定逻辑
        if (!this.halfDead) {
            // 进入假死状态
            this.halfDead = true;
            this.isDead = false;
            this.isDying = false;
            this.currentHealth = 0;

            this.playDeathAnimation();

            // 检查是否有其他流形存活
            boolean otherManifoldsAlive = false;
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                // 排除自己，找活着的流形
                if (m instanceof EnemyManifold && !m.halfDead && !m.isDead && m != this) {
                    otherManifoldsAlive = true;
                    break;
                }
            }

            if (!otherManifoldsAlive) {
                // 如果其他流形全灭（全是假死状态），一起真死
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (m instanceof EnemyManifold && m.halfDead) {
                        m.die(); // 此时会进入底下的 else 分支，彻底死亡
                    }
                }
            } else {
                // 还有队友活着，开始读条复活
                this.reviveTimer = 3;
                this.setMove((byte) 3, Intent.UNKNOWN);
                this.createIntent();
                AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.TextAboveCreatureEffect(this.hb.cX, this.hb.cY, "复活倒计时: 3", com.badlogic.gdx.graphics.Color.WHITE));
            }
        } else {
            // 3. 彻底死亡的兜底分支 (halfDead 已经是 false，或者被机制要求强行处决)
            this.halfDead = false;
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

        // ===== 检测缪尔赛斯是否存活 =====
        if (!this.isDead && !this.isSelfDestructing) {
            boolean bossAlive = false;
            for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                if (m.id.equals(Muelsyse.ID) && !m.isDead && !m.isDying) {
                    bossAlive = true;
                    break;
                }
            }

            // 如果缪尔赛斯死了，流形立刻自我销毁
            if (!bossAlive) {
                this.isSelfDestructing = true;

                // 【修复3】为了保险起见，强制清空血量
                this.currentHealth = 0;

                // 【修复4】这里必须设为 true！
                // 这样后续直接调用 die() 时，才能跳过假死逻辑，进入 else 分支彻底销毁
                this.halfDead = true;

                if (state38 != null) {
                    state38.setAnimation(0, "A_Die", false); // 顺便播个死亡动画化成水
                }

                // 直接调用死亡方法彻底销毁
                this.die();
            }
        }
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
        if (!this.isDead || (state38 != null && state38.getCurrent(0) != null && !state38.getCurrent(0).isComplete())) {
            sb.end();
            psb.begin();
            sr.draw(psb, skeleton38);
            psb.end();
            sb.begin();
        }

        super.render(sb);
    }
}