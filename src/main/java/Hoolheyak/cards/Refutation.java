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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Refutation extends BaseCard {
    public static final String ID = makeID("Refutation");

    private static final int COST = 1;
    private static final int DAMAGE = 6;
    private static final int UPGRADE_PLUS_DMG = 3;

    // --- 【白名单机制】 ---
    // 只有在此列表中的原版/自定义能力，才允许被“驳斥”剥夺
    private static final Set<String> WHITELIST = new HashSet<>(Arrays.asList(
            "Strength",       // 力量
            "Dexterity",      // 敏捷
            "Artifact",       // 人工制品
            "Plated Armor",   // 多层护甲
            "Metallicize",    // 金属化
            "Thorns",         // 荆棘
            "Ritual",         // 仪式 (觉醒者等)
            "Angry",          // 生气
            "Enrage",         // 激怒
            "Regeneration",   // 再生
            "Intangible",     // 无实体
            "Barricade",      // 壁垒 (圆球守护者)
            "Buffer",         // 缓冲
            "Flight",         // 飞行
            "Spore Cloud",    // 孢子云
            "Malleable",      // 柔韧
            "Strength Up",    // 力量提升
            "Sharp Hide",     // 锋利外甲
            "Mode Shift",     // 形态转换
            "Thievery"        // 盗窃
    ));

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
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));

        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                if (m != null && !m.isDeadOrEscaped()) {
                    ArrayList<AbstractPower> validBuffs = new ArrayList<>();

                    // 收集所有类型为 BUFF，且存在于白名单中的能力
                    for (AbstractPower power : m.powers) {
                        if (power.type == AbstractPower.PowerType.BUFF && WHITELIST.contains(power.ID)) {
                            validBuffs.add(power);
                        }
                    }

                    // 如果找到了合法的 BUFF，则随机剥夺一层
                    if (!validBuffs.isEmpty()) {
                        AbstractPower targetBuff = validBuffs.get(AbstractDungeon.miscRng.random(validBuffs.size() - 1));

                        if (targetBuff.amount <= 1) {
                            addToTop(new RemoveSpecificPowerAction(m, p, targetBuff));
                        } else {
                            addToTop(new ReducePowerAction(m, p, targetBuff, 1));
                        }
                    }
                }
                this.isDone = true;
            }
        });
    }
}