package Hoolheyak.cards.phases;

import Hoolheyak.cards.BaseCard;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.phases.ConjunctionPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ConjunctionCard extends BaseCard {
    public static final String ID = makeID("ConjunctionCard");

    public ConjunctionCard() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.STATUS,
                CardRarity.SPECIAL, // 特殊稀有度
                CardTarget.NONE,
                -2 // -2 费代表底层无法打出
        ));
    }

    @Override
    public boolean canUse(AbstractPlayer p, AbstractMonster m) {
        return false; // 绝对禁止手动打出
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}

    // 【核心机制】：当这牌被抽到手牌时，自动触发管理器
    @Override
    public void triggerWhenDrawn() {
        PhaseManager.triggerPhaseCardDrawn(this, new ConjunctionPower(AbstractDungeon.player));
    }

    @Override
    public void onChoseThisOption() {
        PhaseManager.applyPhase(new ConjunctionPower(AbstractDungeon.player));
    }
}