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
    private static final String SKILL_3 = "Skill_3_Loop"; // 博览动画
    private static final String SKILL_2 = "Skill_2_Loop"; // 逶迤动画

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

    // 触发博览（播放一次）
    public void playErudition() {
        AnimationState.TrackEntry trackEntry = state.setAnimation(0, SKILL_3, false);
        trackEntry.setTimeScale(1.5f);

        state.addAnimation(0, IDLE_NORMAL, true, 0f);

        this.currentIdleAnim = IDLE_NORMAL;

        Animation animObj = data.findAnimation(SKILL_3);
        if (animObj != null) {
            this.animationLockTimer = animObj.getDuration() / 1.5f ;
        } else {
            this.animationLockTimer = 1.0f / 1.5f ;
        }
    }

    // 触发逶迤开始（无限循环，直到手动停止）
    public void playMeanderStart() {
        state.setAnimation(0, SKILL_2, true); // loop = true
        this.currentIdleAnim = SKILL_2;
        // 设置一个极长的锁，防止 update 把它切回待机，直到我们手动结束它
        this.animationLockTimer = 999.0f;
    }

    // 触发逶迤结束（清空锁，自动回到待机）
    public void playMeanderEnd() {
        this.animationLockTimer = 0.0f;
        updateIdleStance();
    }

    private void updateIdleStance() {
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