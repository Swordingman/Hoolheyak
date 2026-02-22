package Hoolheyak.cards;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;

public class Defend extends BaseCard {

    public static final String ID = makeID("Defend");

    private static final int COST = 1;
    private static final int BLOCK = 5;
    private static final int UPGRADE_PLUS_BLOCK = 3;

    public Defend() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.BASIC,
                CardTarget.SELF,
                COST
        ));

        // 基础格挡5，升级增加3
        setBlock(BLOCK, UPGRADE_PLUS_BLOCK);

        // 添加基础防御标签
        this.tags.add(CardTags.STARTER_DEFEND);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
    }
}