package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.FriendlyManifold;
import Hoolheyak.powers.PureWaterPower;
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

    // 【新增】全局静态指针，直接向 Patch 暴露当前的分身！
    public static FriendlyManifold activeManifold = null;

    public FriendlyManifold manifold = null;
    private int deadCounter = 0;

    public PureWaterSpriteAssist() {
        super(ID, "PureWaterSpriteAssist", RelicTier.SPECIAL, LandingSound.MAGICAL);
    }

    @Override
    public void atBattleStart() {
        this.manifold = new FriendlyManifold();
        this.deadCounter = 0;
        this.grayscale = false;
        activeManifold = this.manifold;
        addToBot(new ApplyPowerAction(this.manifold, this.manifold, new PureWaterPower(this.manifold)));
        this.flash();
    }

    // 【新增】战斗结束时务必清空，防止在地图界面还在渲染！
    @Override
    public void onVictory() {
        activeManifold = null;
    }

    @Override
    public void atTurnStart() {
        if (this.manifold != null && !this.manifold.isDead) {
            this.manifold.loseBlock();

            this.manifold.nextMove = AbstractDungeon.aiRng.random(0, 2);
        }

        // 复活逻辑
        if (this.manifold != null && this.manifold.isDead) {
            this.deadCounter++;
            if (this.deadCounter >= 3) {
                this.manifold.isDead = false;
                this.manifold.heal(this.manifold.maxHealth);
                this.deadCounter = 0;
                this.grayscale = false;
                this.flash();
                this.manifold.playReviveAnimation();
                this.manifold.nextMove = AbstractDungeon.aiRng.random(0, 2);
                addToBot(new ApplyPowerAction(this.manifold, this.manifold, new PureWaterPower(this.manifold)));
            }
        }
    }

    @Override
    public void onPlayerEndTurn() {
        if (this.manifold != null && !this.manifold.isDead) {
            this.flash();

            if (this.manifold.nextMove == 0) {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        manifold.playAttackAnimation();
                        this.isDone = true;
                    }
                });

                // 行动 1：打 3x3
                for (int i = 0; i < 3; i++) {
                    addToBot(new DamageRandomEnemyAction(new DamageInfo(this.manifold, 3, DamageInfo.DamageType.NORMAL), AbstractGameAction.AttackEffect.BLUNT_LIGHT));
                }
            } else if (this.manifold.nextMove == 1) {
                // 行动 2：加 12 格挡
                addToBot(new GainBlockAction(this.manifold, this.manifold, 12));
            } else {
                // 行动 3：加力量
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this.manifold, new StrengthPower(AbstractDungeon.player, 1), 1));
                addToBot(new ApplyPowerAction(this.manifold, this.manifold, new StrengthPower(this.manifold, 1), 1));
            }

            this.manifold.nextMove = -1;
        }
    }

    @Override
    public void onUnequip() {activeManifold = null;}
}