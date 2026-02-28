package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class BorrowedWeapons extends BaseCard {
    public static final String ID = makeID("BorrowedWeapons");
    private static final int COST = 1;
    private static final int DAMAGE = 9;
    private static final int UPGRADE_PLUS_DMG = 3;

    public BorrowedWeapons() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setExhaust(true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HEAVY));

        // 2. 获取非本职业的随机攻击牌
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                AbstractCard randomCard = null;
                boolean isValid = false;

                // 循环抽取，直到抽到符合条件的牌为止
                while (!isValid) {
                    // 【关键修复1】：先通过底层算法 roll 一个随机的稀有度
                    AbstractCard.CardRarity rarity = AbstractDungeon.rollRarity();

                    // 【关键修复2】：同时传入 类型 (ATTACK) 和 稀有度 (rarity)
                    randomCard = CardLibrary.getAnyColorCard(AbstractCard.CardType.ATTACK, rarity).makeCopy();

                    // 检查这张牌是不是其他职业的，并且不能是带有治疗标签的牌（防滥用机制）
                    if (randomCard.color != Hoolheyak.Meta.CARD_COLOR && !randomCard.hasTag(AbstractCard.CardTags.HEALING)) {
                        isValid = true;
                    }
                }

                // randomCard.setCostForTurn(0); // 如果你想让借来的牌本回合 0 费，可以取消这行注释
                addToTop(new MakeTempCardInHandAction(randomCard, 1));
                this.isDone = true;
            }
        });
    }
}