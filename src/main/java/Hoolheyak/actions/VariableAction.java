package Hoolheyak.actions;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.relics.StarMapProjection;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class VariableAction extends AbstractGameAction {
    // 引入 UIStrings
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString(HoolheyakMod.makeID("VariableActionUI"));
    public static final String[] TEXT = uiStrings.TEXT;

    private AbstractCard sourceCard;
    private ArrayList<VariableChoice> choices;
    private boolean canAllIn; // 是否允许被“星图投影”全选

    public static class VariableChoice {
        public String description;
        public Runnable effect;

        public VariableChoice(String description, Runnable effect) {
            this.description = description;
            this.effect = effect;
        }
    }

    public VariableAction(AbstractCard sourceCard, ArrayList<VariableChoice> choices) {
        this(sourceCard, choices, false);
    }

    public VariableAction(AbstractCard sourceCard, ArrayList<VariableChoice> choices, boolean canAllIn) {
        this.actionType = ActionType.SPECIAL;
        this.sourceCard = sourceCard;
        this.choices = choices;
        this.canAllIn = canAllIn;
    }

    @Override
    public void update() {
        ArrayList<AbstractCard> choiceCards = new ArrayList<>();

        // 1. 生成原本的选项
        for (int i = 0; i < choices.size(); i++) {
            VariableChoice choice = choices.get(i);
            String optionName = getLocalizedName(i);
            choiceCards.add(new VariableChoiceCard(sourceCard, optionName, i, choice.description, choice.effect));
        }

        // 2. 遗物联动：星图投影
        if (this.canAllIn && AbstractDungeon.player.hasRelic(StarMapProjection.ID)) {
            // 检查玩家当前能量是否 >= 1
            if (EnergyPanel.getCurrentEnergy() >= 1) {
                // 直接从 TEXT 数组中读取本地化文本
                String allInName = TEXT[1];
                String allInDesc = TEXT[2];

                VariableChoiceCard allInCard = new VariableChoiceCard(sourceCard, allInName, 99, allInDesc, () -> {
                    // 扣除 1 费
                    AbstractDungeon.player.energy.use(1);
                    AbstractDungeon.player.getRelic(StarMapProjection.ID).flash();

                    for (VariableChoice choice : choices) {
                        choice.effect.run();
                    }
                });

                allInCard.cost = 1;
                allInCard.costForTurn = 1;

                choiceCards.add(allInCard);
            }
        }

        addToTop(new ChooseOneAction(choiceCards));
        this.isDone = true;
    }

    private String getLocalizedName(int index) {
        // 读取 TEXT[0]（中文环境下是 "变量 "，英文环境下是 "Variable "）
        String prefix = TEXT[0];

        String suffix;
        switch (index) {
            case 0: suffix = "α"; break;
            case 1: suffix = "β"; break;
            case 2: suffix = "ω"; break;
            default: suffix = String.valueOf(index + 1); break;
        }

        return prefix + suffix;
    }
}