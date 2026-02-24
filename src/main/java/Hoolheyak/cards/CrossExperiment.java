package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class CrossExperiment extends BaseCard {
    public static final String ID = makeID("CrossExperiment");

    private static final int COST = 1;
    private static final int DAMAGE = 4;
    private static final int BLOCK = 4;
    private static final int MAGIC = 1;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    // 引入一个监控器，用于对比当前的 misc
    private int lastMisc = 0;

    public CrossExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setDamage(DAMAGE);
        setBlock(BLOCK);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
        this.misc = 0;
        this.lastMisc = 0;
    }

    // 封装一个同步计算属性的方法
    private void syncMisc() {
        int bonusDamage = this.misc % 1000;
        int bonusBlock = this.misc / 1000;
        this.baseDamage = DAMAGE + bonusDamage;
        this.baseBlock = BLOCK + bonusBlock;
        this.lastMisc = this.misc;
    }

    // 【核心修复2：突破引擎硬编码】使用 update() 持续监听 misc，完美解决卡组面板不刷新的问题
    @Override
    public void update() {
        super.update();
        if (this.misc != this.lastMisc) {
            syncMisc();
        }
    }

    // 重写这两个战斗计算方法，确保应用力量和敏捷前，基础数值是正确的
    @Override
    public void applyPowers() {
        syncMisc();
        super.applyPowers();
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        syncMisc();
        super.calculateCardDamage(mo);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 先造成伤害和格挡
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
        addToBot(new GainBlockAction(p, p, block));

        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：永久增加伤害
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            increasePermanentStats(true, magicNumber);
        }));

        // 选项 β：永久增加格挡
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            increasePermanentStats(false, magicNumber);
        }));

        addToBot(new VariableAction(this, choices));
    }

    // 处理永久增长的核心逻辑
    private void increasePermanentStats(boolean isDamage, int amount) {
        // 1. 同步修改卡组里的那张卡
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.uuid.equals(this.uuid)) {
                if (isDamage) {
                    c.misc += amount; // 低位存伤害
                } else {
                    c.misc += (amount * 1000); // 高位存格挡
                }
                // 我们不需要在这里写复杂的更新代码，只要改了 misc，
                // 那张牌的 update() 方法就会在下一帧自动捕获并修正面板。
                c.superFlash(); // 闪烁一下提示玩家牌成长了
            }
        }

        // 2. 同时修改当前战斗中手中的这张卡
        if (isDamage) {
            this.misc += amount; // 【核心修复1】：之前你漏了修改自身的 misc！
        } else {
            this.misc += (amount * 1000);
        }

        // 立刻刷新战斗中的面板
        this.applyPowers();
    }
}