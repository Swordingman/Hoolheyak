package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;

import java.util.ArrayList;

public class Archive extends BaseCard {
    public static final String ID = makeID("Archive");

    private static final int COST = 1;
    private static final int DAMAGE = 10;
    private static final int UPGRADE_PLUS_DMG = 3;

    public Archive() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.RARE,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setExhaust(true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            {
                this.target = m;
                this.actionType = ActionType.DAMAGE;
                this.duration = 0.1F;
            }

            @Override
            public void update() {
                if (this.duration == 0.1F && this.target != null) {

                    // 1. 造成伤害与特效
                    AbstractDungeon.effectList.add(new FlashAtkImgEffect(this.target.hb.cX, this.target.hb.cY, AttackEffect.SLASH_HEAVY));
                    this.target.damage(new DamageInfo(p, damage, damageTypeForTurn));

                    // 【核心修复 1】必须先执行清空动作队列！否则它会把我们马上要呼出的 UI 扬掉
                    if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                        AbstractDungeon.actionManager.clearPostCombatActions();
                    }

                    // 2. 然后再判定斩杀并推入我们的 UI
                    // 2. 然后再判定斩杀并推入我们的 UI
                    if ((this.target.isDying || this.target.currentHealth <= 0) && !this.target.halfDead && !this.target.hasPower("Minion")) {

                        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

                        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
                            RewardItem reward = new RewardItem(p.getCardColor());
                            reward.cards.clear();
                            for (int i = 0; i < 3; i++) {
                                reward.cards.add(AbstractDungeon.returnTrulyRandomCardInCombat().makeCopy());
                            }
                            addCustomReward(reward);
                        }));

                        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
                            RewardItem reward = new RewardItem(AbstractCard.CardColor.COLORLESS);
                            addCustomReward(reward);
                        }));

                        // 【最终修复】创建动作后，强制修改它的 actionType
                        VariableAction varAction = new VariableAction(Archive.this, choices);
                        varAction.actionType = AbstractGameAction.ActionType.DAMAGE; // 赋予免死金牌！

                        addToTop(varAction);
                    }
                }

                this.tickDuration();
            }
        });
    }

    // 辅助添加奖励方法保持不变
    private void addCustomReward(RewardItem reward) {
        AbstractDungeon.getCurrRoom().rewards.add(reward);
        if (AbstractDungeon.getCurrRoom().isBattleOver && AbstractDungeon.combatRewardScreen != null) {
            AbstractDungeon.combatRewardScreen.rewards.add(reward);
            AbstractDungeon.combatRewardScreen.positionRewards();
        }
    }
}