package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
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

    // 用 magic number 来控制打出稀有牌的数量：初始3张，升级+1变成4张
    private static final int MAGIC = 3;
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
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 先造成 6 点基础伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));

        // 2. 根据魔法数字（3或4），循环生成并打出稀有攻击牌
        for (int i = 0; i < this.magicNumber; i++) {
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    // 构建稀有攻击牌的临时卡池
                    ArrayList<AbstractCard> rareAttacks = new ArrayList<>();
                    for (AbstractCard c : CardLibrary.getAllCards()) {
                        // 筛选条件：攻击牌 + 稀有 + 必须把【万界测绘】自己排除在外（防止无限套娃死循环！）
                        if (c.type == CardType.ATTACK
                                && c.rarity == CardRarity.RARE
                                && !c.cardID.equals(UniversalMapping.ID)) {
                            rareAttacks.add(c);
                        }
                    }

                    if (!rareAttacks.isEmpty()) {
                        // 随机抽取一张并复制
                        AbstractCard randomCard = rareAttacks.get(AbstractDungeon.cardRandomRng.random(rareAttacks.size() - 1)).makeCopy();

                        // 设定为打出后消耗（不进弃牌堆），且本次打出不耗费能量
                        randomCard.purgeOnUse = true;
                        randomCard.freeToPlayOnce = true;

                        // 因为衍生出来的牌有些可能是单体目标，这里为它随机指定一个存活的敌人
                        AbstractMonster target = AbstractDungeon.getRandomMonster();

                        // 加入到系统的出牌队列中自动打出
                        AbstractDungeon.actionManager.addToTop(new NewQueueCardAction(randomCard, target, false, true));
                    }
                    this.isDone = true;
                }
            });

            addToBot(new WaitAction(0.4F));
        }
    }
}