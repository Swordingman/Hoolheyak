package Hoolheyak.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import java.util.ArrayList;

public class VariableAction extends AbstractGameAction {
    private AbstractCard sourceCard;
    private ArrayList<VariableChoice> choices;

    public static class VariableChoice {
        // 移除了 nameSuffix，交由系统自动生成
        public String description;
        public Runnable effect;

        public VariableChoice(String description, Runnable effect) {
            this.description = description;
            this.effect = effect;
        }
    }

    public VariableAction(AbstractCard sourceCard, ArrayList<VariableChoice> choices) {
        this.actionType = ActionType.SPECIAL;
        this.sourceCard = sourceCard;
        this.choices = choices;
    }

    @Override
    public void update() {
        ArrayList<AbstractCard> choiceCards = new ArrayList<>();

        // 使用普通 for 循环，方便获取当前选项的序号 (index)
        for (int i = 0; i < choices.size(); i++) {
            VariableChoice choice = choices.get(i);
            String optionName = getLocalizedName(i); // 获取双语动态名字

            // 将 index 传入以生成安全的 CardID
            choiceCards.add(new VariableChoiceCard(sourceCard, optionName, i, choice.description, choice.effect));
        }

        addToTop(new ChooseOneAction(choiceCards));
        this.isDone = true;
    }

    // 自动根据序号和语言环境生成名称
    private String getLocalizedName(int index) {
        // 判断当前语言是否为简体/繁体中文
        boolean isChinese = (Settings.language == Settings.GameLanguage.ZHS || Settings.language == Settings.GameLanguage.ZHT);
        String prefix = isChinese ? "变量" : "Variable ";

        String suffix;
        switch (index) {
            case 0: suffix = "α"; break;
            case 1: suffix = "β"; break;
            case 2: suffix = "ω"; break; // 第三项特定为 ω
            default: suffix = String.valueOf(index + 1); break; // 超过三项的备用方案
        }

        return prefix + suffix;
    }
}