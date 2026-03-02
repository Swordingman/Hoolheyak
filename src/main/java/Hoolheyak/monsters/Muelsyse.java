package Hoolheyak.monsters;

import Hoolheyak.HoolheyakMod;
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
                // 流形生成在缪缪前面不同位置
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(new EnemyManifold(-100.0F, 0.0F), true));
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(new EnemyManifold(-250.0F, 10.0F), true));
                AbstractDungeon.actionManager.addToBottom(new SpawnMonsterAction(new EnemyManifold(-400.0F, -10.0F), true));
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