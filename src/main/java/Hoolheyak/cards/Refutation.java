package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;

import java.util.ArrayList;

public class Refutation extends BaseCard {
    public static final String ID = makeID("Refutation");

    private static final int COST = 1;
    private static final int DAMAGE = 6;
    private static final int UPGRADE_PLUS_DMG = 3;

    public Refutation() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一阶段：造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));

        // 第二阶段：随机剥夺一层增益
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                // 确保怪物存活，防止空指针或无效操作
                if (m != null && !m.isDeadOrEscaped()) {
                    ArrayList<AbstractPower> buffs = new ArrayList<>();

                    // 收集所有类型为 BUFF 的能力
                    for (AbstractPower power : m.powers) {
                        if (power.type == AbstractPower.PowerType.BUFF) {
                            buffs.add(power);
                        }
                    }

                    if (!buffs.isEmpty()) {
                        // 随机选取一个 BUFF
                        AbstractPower targetBuff = buffs.get(AbstractDungeon.miscRng.random(buffs.size() - 1));

                        // 层数 <= 1，或者是没有具体层数的能力（amount == -1，如原版的壁垒等），则直接移除
                        if (targetBuff.amount <= 1) {
                            addToTop(new RemoveSpecificPowerAction(m, p, targetBuff));
                        } else {
                            // 否则仅减少 1 层
                            addToTop(new ReducePowerAction(m, p, targetBuff, 1));
                        }
                    }
                }
                this.isDone = true;
            }
        });
    }
}