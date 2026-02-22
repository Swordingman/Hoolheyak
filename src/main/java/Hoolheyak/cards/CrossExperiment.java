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
        this.misc = 0; // 初始化永久变量
    }

    // 每次计算状态或从存档中加载时，通过 misc 还原永久增长的值
    @Override
    public void applyPowers() {
        int bonusDamage = this.misc % 1000;
        int bonusBlock = this.misc / 1000;
        this.baseDamage = DAMAGE + bonusDamage;
        this.baseBlock = BLOCK + bonusBlock;
        super.applyPowers();
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
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
        // 同步修改卡组里的那张卡
        for (AbstractCard c : AbstractDungeon.player.masterDeck.group) {
            if (c.uuid.equals(this.uuid)) {
                if (isDamage) {
                    c.misc += amount; // 低位存伤害
                    c.baseDamage += amount;
                } else {
                    c.misc += (amount * 1000); // 高位存格挡
                    c.baseBlock += amount;
                }
                c.isDamageModified = true;
                c.isBlockModified = true;
                c.superFlash();
            }
        }

        // 同时修改当前战斗中手中的这张卡
        if (isDamage) {
            this.baseDamage += amount;
        } else {
            this.baseBlock += amount;
        }
        this.applyPowers();
    }
}