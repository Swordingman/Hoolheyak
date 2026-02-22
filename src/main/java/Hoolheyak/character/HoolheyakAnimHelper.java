package Hoolheyak.character;

import com.badlogic.gdx.Gdx;
import com.Hoolheyak.spine38.Animation;
import com.Hoolheyak.spine38.AnimationState;
import com.Hoolheyak.spine38.SkeletonData;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class HoolheyakAnimHelper {

    private AnimationState state;
    private SkeletonData data;
    private String currentIdleAnim = "Idle";
    private float animationLockTimer = 0.0f;

    // --- 基础动画名称常量 ---
    // 如果霍尔海雅的 Spine 动画名字不一样，请在这里修改
    private static final String IDLE_NORMAL = "Idle";
    private static final String ATTACK_NORMAL = "Attack";

    public HoolheyakAnimHelper(AnimationState state, SkeletonData data) {
        this.state = state;
        this.data = data;
    }

    public void update() {
        // 不在战斗中，或者玩家已死、怪物全清的情况，不更新状态
        if (AbstractDungeon.getCurrRoom() == null || AbstractDungeon.getCurrRoom().phase != AbstractRoom.RoomPhase.COMBAT) return;
        if (AbstractDungeon.player == null || AbstractDungeon.player.isDead) return;
        if (AbstractDungeon.getMonsters() == null || AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
            return;
        }

        float dt = Gdx.graphics.getDeltaTime();

        // 倒计时动画锁（防止攻击动画被 Idle 立刻切掉）
        if (animationLockTimer > 0) {
            animationLockTimer -= dt;
        }

        // 锁结束后，确保回到待机状态
        if (animationLockTimer <= 0) {
            updateIdleStance();
        }
    }

    public void playAttack() {
        // 播放普通攻击动画
        state.setAnimation(0, ATTACK_NORMAL, false);
        // 排队：攻击结束后自动循环播放待机动画
        state.addAnimation(0, IDLE_NORMAL, true, 0f);

        this.currentIdleAnim = IDLE_NORMAL;

        // 获取攻击动画的时长并设置动画锁
        Animation animObj = data.findAnimation(ATTACK_NORMAL);
        if (animObj != null) {
            this.animationLockTimer = animObj.getDuration();
        } else {
            this.animationLockTimer = 1.0f; // 找不到动画时的默认后摇时间
        }
    }

    private void updateIdleStance() {
        // 如果当前不在待机状态，则重置回待机
        if (!currentIdleAnim.equals(IDLE_NORMAL)) {
            state.setAnimation(0, IDLE_NORMAL, true);
            currentIdleAnim = IDLE_NORMAL;
        }
    }

    public void reset() {
        // 战斗结束或需要重置时调用
        this.animationLockTimer = 0.0F;
        this.currentIdleAnim = IDLE_NORMAL;
        state.setAnimation(0, IDLE_NORMAL, true);
    }
}