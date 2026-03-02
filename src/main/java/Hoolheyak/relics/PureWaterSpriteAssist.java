package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.FriendlyManifold;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class PureWaterSpriteAssist extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("PureWaterSpriteAssist");

    public FriendlyManifold manifold = null;
    private int deadCounter = 0;

    public PureWaterSpriteAssist() {
        super(ID, "PureWaterSpriteAssist", RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public void atBattleStart() {
        // 战斗开始，生成分身实体
        this.manifold = new FriendlyManifold();
        this.deadCounter = 0;
        this.grayscale = false;
        this.flash();
    }

    @Override
    public void atTurnStart() {
        // 复活逻辑
        if (this.manifold != null && this.manifold.isDead) {
            this.deadCounter++;
            if (this.deadCounter >= 3) {
                this.manifold.isDead = false;
                this.manifold.heal(this.manifold.maxHealth); // 满血复活
                this.deadCounter = 0;
                this.grayscale = false; // 恢复遗物颜色
                this.flash();
            }
        }
    }

    @Override
    public void onPlayerEndTurn() {
        // 如果分身存活，替玩家行动
        if (this.manifold != null && !this.manifold.isDead) {
            this.flash();

            // 意图1：打 3x3 随机敌人
            for (int i = 0; i < 3; i++) {
                addToBot(new DamageRandomEnemyAction(new DamageInfo(this.manifold, 3, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
            }

            // 意图2：获得 12 格挡（加在分身自己身上）
            addToBot(new GainBlockAction(this.manifold, this.manifold, 12));

            // 意图3：给予自己和霍尔海雅 1 力量
            addToBot(new ApplyPowerAction(AbstractDungeon.player, this.manifold, new StrengthPower(AbstractDungeon.player, 1), 1));
            addToBot(new ApplyPowerAction(this.manifold, this.manifold, new StrengthPower(this.manifold, 1), 1));
        }
    }

    // 【核心机制：强制嘲讽转移伤害】
    @Override
    public int onAttacked(DamageInfo info, int damageAmount) {
        // 如果伤害来源是怪物，且分身存活
        if (info.owner instanceof AbstractMonster && this.manifold != null && !this.manifold.isDead) {
            this.flash();

            // 将玩家准备承受的伤害，让分身去扣除（包括破甲计算）
            this.manifold.damage(new DamageInfo(info.owner, damageAmount, info.type));

            // 如果分身在这次攻击中死了，遗物变灰
            if (this.manifold.isDead) {
                this.grayscale = true;
            }

            // 返回0，玩家承受 0 点伤害！完美拦截！
            return 0;
        }
        return damageAmount;
    }

    @Override
    public void update() {
        super.update();
        if (AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT && this.manifold != null) {
            this.manifold.update(); // 让分身的血条保持刷新
        }
    }
}