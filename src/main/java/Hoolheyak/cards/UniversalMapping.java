package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.NewQueueCardAction;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class UniversalMapping extends BaseCard {

    public static final String ID = makeID("UniversalMapping");

    private static final int COST = 3;
    private static final int DAMAGE = 6;

    // 基础魔法数字（标准难度）
    private static final int MAGIC = 2;
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public UniversalMapping() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.RARE,
                CardTarget.ENEMY,
                COST
        ));

        setDamage(DAMAGE);
        // 构造函数里只设置基础值，不在这里做难度判断！
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    // 每次玩家数值变化、抽到这张牌时，都会调用这个方法。
    // 在这里做难度判定，才能让卡面描述实时且正确地显示当前的魔法数字。
    @Override
    public void applyPowers() {
        int finalMagic = MAGIC;

        // 实时检测当前难度
        if (HoolheyakDifficultyHelper.currentDifficulty == HoolheyakDifficultyHelper.DifficultyLevel.HARD) {
            finalMagic = 1;
        } else if (HoolheyakDifficultyHelper.currentDifficulty == HoolheyakDifficultyHelper.DifficultyLevel.EASY) {
            finalMagic = 3;
        }

        // 如果卡牌已经升级，要加上升级的增量
        if (this.upgraded) {
            finalMagic += UPGRADE_PLUS_MAGIC;
        }

        // 更新卡牌的魔法数字
        if (this.baseMagicNumber != finalMagic) {
            this.baseMagicNumber = finalMagic;
            this.magicNumber = this.baseMagicNumber;
            this.isMagicNumberModified = true;
        }

        super.applyPowers();
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 先造成基础伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));

        // 2. 将“稀有攻击牌池”的构建提取到外面！只遍历一次，极大优化性能
        ArrayList<AbstractCard> rareAttacks = new ArrayList<>();
        for (AbstractCard c : CardLibrary.getAllCards()) {
            // 修复了枚举编译错误，并且排除了本家卡牌和自身
            if (c.type == CardType.ATTACK
                    && c.rarity == CardRarity.RARE
                    && c.color != Hoolheyak.Meta.CARD_COLOR
                    && !c.cardID.equals(UniversalMapping.ID)) {
                rareAttacks.add(c);
            }
        }

        // 3. 根据最终的魔法数字，循环抽卡并打出
        if (!rareAttacks.isEmpty()) {
            for (int i = 0; i < this.magicNumber; i++) {
                // 随机抽取一张并复制
                AbstractCard randomCard = rareAttacks.get(AbstractDungeon.cardRandomRng.random(rareAttacks.size() - 1)).makeCopy();

                // 设定为打出后消耗，且本次打出不耗费能量
                randomCard.purgeOnUse = true;
                randomCard.freeToPlayOnce = true;

                // 为这张衍生牌随机指定一个存活的敌人
                AbstractMonster target = AbstractDungeon.getRandomMonster();

                // 直接用 addToBot 排队打出动作，无需再套一层匿名的 AbstractGameAction
                addToBot(new NewQueueCardAction(randomCard, target, false, true));

                // 加上一点延迟，让多张牌连打时动画有层次感，不会糊在一起
                addToBot(new WaitAction(0.2F));
            }
        }
    }
}