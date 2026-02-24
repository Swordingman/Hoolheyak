package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class TwinExperiment extends BaseCard {
    public static final String ID = makeID("TwinExperiment");

    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 1;

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
                AbstractMonster target1 = aliveMonsters.get(0);
                AbstractMonster target2 = aliveMonsters.size() > 1 ? aliveMonsters.get(1) : null;

                // 第一阶段：对选中的目标各造成 2 次伤害
                // 注意：addToTop 是后进先出，为了让表现正常，我们用 addToTop 按顺序压入
                for (int i = 0; i < 2; i++) {
                    // 我们要把动作反向压入队列，这样 target1 先受击，target2 后受击
                    if (target2 != null) {
                        TwinExperiment.this.calculateCardDamage(target2);
                        addToTop(new DamageAction(target2, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.SLASH_DIAGONAL));
                    }
                    TwinExperiment.this.calculateCardDamage(target1);
                    addToTop(new DamageAction(target1, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.SLASH_DIAGONAL));
                }

                // 第二阶段：如果确实选中了两个敌人，追加后续的伤害判定
                if (target2 != null) {
                    addToBot(new AbstractGameAction() {
                        @Override
                        public void update() {
                            AbstractMonster extraTarget = null;

                            // 确保目标还没死透
                            boolean t1Alive = !target1.isDeadOrEscaped();
                            boolean t2Alive = !target2.isDeadOrEscaped();

                            if (t1Alive && t2Alive) {
                                // 都有剩余血量，比较谁的血量更多（如果一样多，默认选 target1）
                                extraTarget = (target1.currentHealth >= target2.currentHealth) ? target1 : target2;
                            } else if (t1Alive) {
                                extraTarget = target1;
                            } else if (t2Alive) {
                                extraTarget = target2;
                            }

                            // 对血量更多的那个额外造成 2 次伤害
                            if (extraTarget != null) {
                                for (int i = 0; i < 2; i++) {
                                    TwinExperiment.this.calculateCardDamage(extraTarget);
                                    // 使用更重的打击特效以示区分
                                    addToTop(new DamageAction(extraTarget, new DamageInfo(p, TwinExperiment.this.damage, damageTypeForTurn), AttackEffect.BLUNT_HEAVY));
                                }
                            }
                            this.isDone = true;
                        }
                    });
                }
                this.isDone = true;
            }
        });
    }
}