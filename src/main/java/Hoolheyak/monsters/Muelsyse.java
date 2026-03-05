package Hoolheyak.monsters;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.powers.EnemyPureWaterPower;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.MinionPower;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.Hoolheyak.spine38.*;
import basemod.ReflectionHacks;

public class Muelsyse extends AbstractMonster {
    public static final String ID = HoolheyakMod.makeID("Muelsyse");
    private static final MonsterStrings monsterStrings = CardCrawlGame.languagePack.getMonsterStrings(ID);
    public static final String NAME = monsterStrings.NAME;

    protected TextureAtlas atlas38;
    protected Skeleton skeleton38;
    public AnimationState state38;
    protected AnimationStateData stateData38;
    protected static final PolygonSpriteBatch psb = new PolygonSpriteBatch();
    protected static final SkeletonRenderer sr = new SkeletonRenderer();
    static { sr.setPremultipliedAlpha(true); }

    private boolean firstTurn = true;

    public Muelsyse(float x, float y) {
        // 缪缪本体站在最后面（比如 x=150）
        super(NAME, ID, 99, 0.0F, 0.0F, 180.0F, 240.0F, null, x + 150.0F, y);
        this.type = EnemyType.ELITE; // 作为事件强敌

        this.damage.add(new com.megacrit.cardcrawl.cards.DamageInfo(this, 1));

        loadSpine();
    }

    private void loadSpine() {
        atlas38 = new TextureAtlas(Gdx.files.internal("Hoolheyak/char/Mlyss/char_249_mlyss.atlas"));
        SkeletonJson json = new SkeletonJson(atlas38);
        json.setScale(Settings.renderScale / 1.5f);
        SkeletonData data = json.readSkeletonData(Gdx.files.internal("Hoolheyak/char/Mlyss/char_249_mlyss.json"));
        skeleton38 = new Skeleton(data);
        skeleton38.setColor(Color.WHITE);

        skeleton38.setScaleX(-Math.abs(skeleton38.getScaleX()));

        stateData38 = new AnimationStateData(data);
        state38 = new AnimationState(stateData38);
        stateData38.setDefaultMix(0.2f);
        stateData38.setMix("Idle", "Attack", 0.1f);
        stateData38.setMix("Attack", "Idle", 0.1f);
        state38.setAnimation(0, "Idle", true);
    }

    @Override
    public void takeTurn() {
        switch (this.nextMove) {
            case 1: // 召唤3个流形
                // 1. 先把流形实例化并存入变量，方便后续精准点名
                EnemyManifold m1 = new EnemyManifold(-100.0F, 0.0F);
                EnemyManifold m2 = new EnemyManifold(-250.0F, 10.0F);
                EnemyManifold m3 = new EnemyManifold(-400.0F, -10.0F);

                // 2. 将生成怪物的动作加入队列
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(m1, true));
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(m2, true));
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(m3, true));

                // 3. 紧接着在动作队列里，给它们挨个贴上“纯水”能力
                // 注意：施法者 (source) 可以填 this (缪尔赛斯)，目标 (target) 填对应的流形
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m1, this, new EnemyPureWaterPower(m1)));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m2, this, new EnemyPureWaterPower(m2)));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m3, this, new EnemyPureWaterPower(m3)));

                // 4. 顺便把爪牙 (Minion) 能力也在这里给，最稳妥
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m1, this, new MinionPower(m1)));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m2, this, new MinionPower(m2)));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m3, this, new MinionPower(m3)));

                this.firstTurn = false;
                break;
            case 2: // 3x6 伤害
                if (state38 != null) {
                    state38.setAnimation(0, "Attack", false);
                    state38.addAnimation(0, "Idle", true, 0f);
                }
                for (int i = 0; i < 6; i++) {
                    AbstractDungeon.actionManager.addToBottom(new DamageAction(AbstractDungeon.player, this.damage.get(0), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                }
                break;
            case 3: // 加力量加格挡
                for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                    if (!m.isDead && !m.halfDead) {
                        AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, this, new StrengthPower(m, 1), 1));
                        AbstractDungeon.actionManager.addToBottom(new GainBlockAction(m, this, 12));
                    }
                }
                break;
            case 4: // 虚弱和脆弱
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, 2, true), 2));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(AbstractDungeon.player, this, new VulnerablePower(AbstractDungeon.player, 2, true), 2));
                break;
        }

        // 让流形的倒计时推进
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (m instanceof EnemyManifold) {
                ((EnemyManifold) m).handleReviveCountdown();
            }
        }

        AbstractDungeon.actionManager.addToBottom(new com.megacrit.cardcrawl.actions.common.RollMoveAction(this));
    }

    @Override
    protected void getMove(int num) {
        if (firstTurn) {
            this.setMove((byte) 1, Intent.UNKNOWN);
            return;
        }

        // 防止连续使用同一个技能
        if (num < 40 && !this.lastMove((byte) 2)) {
            this.setMove((byte) 2, Intent.ATTACK, 3, 6, true);
        } else if (num < 70 && !this.lastMove((byte) 3)) {
            this.setMove((byte) 3, Intent.DEFEND_BUFF);
        } else {
            if (!this.lastMove((byte) 4)) {
                this.setMove((byte) 4, Intent.STRONG_DEBUFF);
            } else {
                this.setMove((byte) 2, Intent.ATTACK, 3, 6, true);
            }
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

        this.hb.render(sb);
        this.intentHb.render(sb);
        this.healthHb.render(sb);

        if (!com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.isDead) {
            this.renderHealth(sb); // 渲染血条
            // 【关键修复 2】：反射调用渲染怪物名字
            basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderName", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
        }

        if (!this.isDying && !this.isEscaping && com.megacrit.cardcrawl.dungeons.AbstractDungeon.getCurrRoom().phase == com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.isDead && !com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.hasRelic("Runic Dome") && this.intent != Intent.NONE && !com.megacrit.cardcrawl.core.Settings.hideCombatElements) {
            basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntentVfxBehind", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
            basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntent", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
            basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderIntentVfxAfter", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);

            // 【关键修复 3】：渲染意图上面的攻击数值文本！(例如 3x6)
            basemod.ReflectionHacks.privateMethod(com.megacrit.cardcrawl.monsters.AbstractMonster.class, "renderDamageRange", com.badlogic.gdx.graphics.g2d.SpriteBatch.class).invoke(this, sb);
        }

        // 【关键修复】：利用 BaseMod 反射强行调用 AbstractMonster 的私有方法绘制意图
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntentVfxBehind", SpriteBatch.class).invoke(this, sb);
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntent", SpriteBatch.class).invoke(this, sb);
        ReflectionHacks.privateMethod(AbstractMonster.class, "renderIntentVfxAfter", SpriteBatch.class).invoke(this, sb);
    }
}