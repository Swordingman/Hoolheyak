package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.LiftPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.ChemicalX;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

public class MassOffset extends BaseCard {
    public static final String ID = makeID("MassOffset");

    private static final int COST = -1; // X 费
    private static final int MAGIC = 0; // 升级前基础额外 +0
    private static final int UPGRADE_PLUS_MAGIC = 1; // 升级后额外 +1

    public MassOffset() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.ALL_ENEMY,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            @Override
            public void update() {
                // 计算 X 的实际数值
                int effect = EnergyPanel.totalCount;
                if (energyOnUse != -1) {
                    effect = energyOnUse;
                }

                // 联动【化学物X】遗物
                if (p.relics != null) {
                    for (com.megacrit.cardcrawl.relics.AbstractRelic r : p.relics) {
                        if (ChemicalX.ID.equals(r.relicId)) {
                            effect += 2;
                            r.flash();
                        }
                    }
                }

                // 加上升级带来的额外修正 (+1)
                effect += magicNumber;

                if (effect > 0) {
                    // 给予全体敌人升力
                    for (AbstractMonster mo : AbstractDungeon.getCurrRoom().monsters.monsters) {
                        if (!mo.isDeadOrEscaped()) {
                            addToTop(new ApplyPowerAction(mo, p, new LiftPower(mo, p, effect), effect));
                        }
                    }
                    // 给予自己解析
                    addToTop(new ApplyPowerAction(p, p, new AnalysisPower(p, effect), effect));
                }

                // 扣除能量
                if (!freeToPlayOnce) {
                    p.energy.use(EnergyPanel.totalCount);
                }

                this.isDone = true;
            }
        });
    }
}