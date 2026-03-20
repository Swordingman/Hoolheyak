package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TwinExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("TwinExperiment");

    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 1;

    // 缓存随机选中的两个敌人，以便在变量选项中追踪他们的血量
    private AbstractMonster target1 = null;
    private AbstractMonster target2 = null;

    public TwinExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ALL_ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                // 获取所有存活的敌人
                ArrayList<AbstractMonster> aliveMonsters = new ArrayList<>();
                for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                    if (!mo.isDeadOrEscaped()) {
                        aliveMonsters.add(mo);
                    }
                }

                if (aliveMonsters.isEmpty()) {
                    this.isDone = true;
                    return;
                }

                // 打乱顺序，随机选取最多两个目标
                Collections.shuffle(aliveMonsters, new Random(AbstractDungeon.cardRandomRng.randomLong()));
                target1 = aliveMonsters.get(0);
                target2 = aliveMonsters.size() > 1 ? aliveMonsters.get(1) : null;

                // 第一阶段：对选中的目标各造成 2 次伤害
                for (int i = 0; i < 2; i++) {
                    if (target2 != null) {
                        TwinExperiment.this.calculateCardDamage(target2);
                        addToTop(new DamageAction(target2, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.SLASH_DIAGONAL));
                    }
                    TwinExperiment.this.calculateCardDamage(target1);
                    addToTop(new DamageAction(target1, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.SLASH_DIAGONAL));
                }

                // 第二阶段：如果确实选中了两个不同敌人，触发变量选项
                if (target2 != null) {
                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            // 调用接口方法获取选项，canAllIn 传入 true 允许星图投影全选
                            ArrayList<VariableAction.VariableChoice> choices = getVariableChoices(p, m, false);
                            addToTop(new VariableAction(TwinExperiment.this, choices, true));
                            this.isDone = true;
                        }
                    });
                }

                this.isDone = true;
            }
        });
    }

    // 实现 IVariableCard 的核心方法
    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            applyExtraDamage(p, true);
        }));

        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            applyExtraDamage(p, false);
        }));

        return choices;
    }

    // 重写方法，使其不能被自动触发
    @Override
    public boolean canBeAutoTriggered() {
        return false;
    }

    // 辅助方法：动态判定血量并追加2次伤害
    private void applyExtraDamage(AbstractPlayer p, boolean hitMoreHp) {
        // 使用 Action 包装，保证在触发“全选”时，第二次判定的血量是第一段伤害结算后的最新血量
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                AbstractMonster extraTarget = null;

                boolean t1Alive = target1 != null && !target1.isDeadOrEscaped();
                boolean t2Alive = target2 != null && !target2.isDeadOrEscaped();

                if (t1Alive && t2Alive) {
                    if (hitMoreHp) {
                        // 找血量更多的（平血默认给 target1）
                        extraTarget = (target1.currentHealth >= target2.currentHealth) ? target1 : target2;
                    } else {
                        // 找血量更少的（平血默认给 target1）
                        extraTarget = (target1.currentHealth <= target2.currentHealth) ? target1 : target2;
                    }
                } else if (t1Alive) {
                    extraTarget = target1; // 只剩一个活着就直接打活着的
                } else if (t2Alive) {
                    extraTarget = target2;
                }

                // 造成额外 2 次重击
                if (extraTarget != null) {
                    for (int i = 0; i < 2; i++) {
                        TwinExperiment.this.calculateCardDamage(extraTarget);
                        addToTop(new DamageAction(extraTarget, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.BLUNT_HEAVY));
                    }
                }
                this.isDone = true;
            }
        });
    }
}