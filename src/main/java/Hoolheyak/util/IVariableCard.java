package Hoolheyak.util;

import Hoolheyak.actions.VariableAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public interface IVariableCard {

    // 🌟 核心新方法：带上“是否为自动触发”的标记
    ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered);

    // 兼容旧代码：如果你只传 p 和 m，默认就是手动触发 (false)
    default ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m) {
        return getVariableChoices(p, m, false);
    }

    default boolean canBeAutoTriggered() {
        return true;
    }
}