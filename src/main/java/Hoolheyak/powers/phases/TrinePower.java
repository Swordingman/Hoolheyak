package Hoolheyak.powers.phases;

import Hoolheyak.powers.BasePower;
import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class TrinePower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Trine");

    // 【新增】我们自己声明一个控制变量，默认在刚实例化时为 true
    private boolean isJustApplied = true;

    public TrinePower(AbstractCreature owner) {
        super(POWER_ID, PowerType.BUFF, false, owner, -1);
        // 确保每次新建这个能力时，它都处于“刚刚被施加”的状态
        this.isJustApplied = true;
    }

    // 1. 获得该相位时，立刻生效
    @Override
    public void onInitialApplication() {
        this.flash();
        addToBot(new GainEnergyAction(1));
        addToBot(new DrawCardAction(this.owner, 2));
    }

    // 2. 如果玩家带着这个相位跨越了回合，则在后续回合继续生效
    @Override
    public void atStartOfTurnPostDraw() {
        // 【核心拦截】如果是这回合刚挂上的，直接跳过，防止同一回合内触发两次
        if (this.isJustApplied) {
            return;
        }

        this.flash();
        addToBot(new GainEnergyAction(1));
        addToBot(new DrawCardAction(this.owner, 2));
    }

    // 3. 【关键清理机制】回合结束时，将标记重置，保证下个回合它能正常执行
    @Override
    public void atEndOfRound() {
        this.isJustApplied = false;
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.ATTACK) {
            this.flash();
            action.exhaustCard = true;
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }
}