package Hoolheyak.cards;

import basemod.BaseMod;
import basemod.abstracts.CustomCard;
import basemod.abstracts.DynamicVariable;
import com.badlogic.gdx.Gdx;
import Hoolheyak.HoolheyakMod;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.TriFunction;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Hoolheyak.util.GeneralUtils.removePrefix;
import static Hoolheyak.util.TextureLoader.getCardTextureString;

// 这是所有自定义卡牌的父类。
// 以后写斥罪的卡牌时，请继承 BaseCard 而不是 CustomCard。
public abstract class BaseCard extends CustomCard {
    // 存储所有自定义变量（DynamicVariable）的映射
    final private static Map<String, DynamicVariable> customVars = new HashMap<>();

    // 工具方法：自动添加Mod ID前缀
    protected static String makeID(String name) { return HoolheyakMod.makeID(name); }
    protected CardStrings cardStrings;

    // 是否有升级后的描述文本
    protected boolean upgradesDescription;

    // 基础费用
    protected int baseCost;

    // 升级相关的开关和数值
    protected boolean upgradeCost;
    protected int costUpgrade;

    protected boolean upgradeDamage;
    protected boolean upgradeBlock;
    protected boolean upgradeMagic;

    protected int damageUpgrade;
    protected int blockUpgrade;
    protected int magicUpgrade;

    // 关键字属性：消耗、虚无、固有、保留
    protected boolean baseExhaust = false;
    protected boolean upgExhaust = false;
    protected boolean baseEthereal = false;
    protected boolean upgEthereal = false;
    protected boolean baseInnate = false;
    protected boolean upgInnate = false;
    protected boolean baseRetain = false;
    protected boolean upgRetain = false;

    // 本地变量信息存储
    final protected Map<String, LocalVarInfo> cardVariables = new HashMap<>();

    // ---------- 构造函数区域 ----------
    // 使用 CardStats 对象来简化构造函数调用（推荐使用）
    public BaseCard(String ID, CardStats info) {
        this(ID, info, getCardTextureString(removePrefix(ID), info.cardType));
    }
    public BaseCard(String ID, CardStats info, String cardImage) {
        this(ID, info.baseCost, info.cardType, info.cardTarget, info.cardRarity, info.cardColor, cardImage);
    }
    // 传统构造函数
    public BaseCard(String ID, int cost, CardType cardType, CardTarget target, CardRarity rarity, CardColor color) {
        this(ID, cost, cardType, target, rarity, color, getCardTextureString(removePrefix(ID), cardType));
    }
    // 最底层的构造函数
    public BaseCard(String ID, int cost, CardType cardType, CardTarget target, CardRarity rarity, CardColor color, String cardImage)
    {
        super(ID, getName(ID), cardImage, cost, getInitialDescription(ID), cardType, color, rarity, target);
        this.cardStrings = CardCrawlGame.languagePack.getCardStrings(cardID);
        this.originalName = cardStrings.NAME;

        this.baseCost = cost;

        // 检查本地化文件里有没有写 "UPGRADE_DESCRIPTION"
        this.upgradesDescription = cardStrings.UPGRADE_DESCRIPTION != null;

        // 初始化升级标志为 false
        this.upgradeCost = false;
        this.upgradeDamage = false;
        this.upgradeBlock = false;
        this.upgradeMagic = false;

        this.costUpgrade = cost;
        this.damageUpgrade = 0;
        this.blockUpgrade = 0;
        this.magicUpgrade = 0;
    }

    // 获取卡牌名称和描述的辅助方法
    private static String getName(String ID) {
        return CardCrawlGame.languagePack.getCardStrings(ID).NAME;
    }
    private static String getInitialDescription(String ID) {
        return CardCrawlGame.languagePack.getCardStrings(ID).DESCRIPTION;
    }

    // ---------- 设置数值的方法 (Setters) ----------
    // 这些方法让你在构造卡牌时可以同时设置"初始值"和"升级增量"

    // 设置伤害
    protected final void setDamage(int damage)
    {
        this.setDamage(damage, 0);
    }
    protected final void setDamage(int damage, int damageUpgrade)
    {
        this.baseDamage = this.damage = damage;
        if (damageUpgrade != 0)
        {
            this.upgradeDamage = true;
            this.damageUpgrade = damageUpgrade;
        }
    }

    // 设置格挡
    protected final void setBlock(int block)
    {
        this.setBlock(block, 0);
    }
    protected final void setBlock(int block, int blockUpgrade)
    {
        this.baseBlock = this.block = block;
        if (blockUpgrade != 0)
        {
            this.upgradeBlock = true;
            this.blockUpgrade = blockUpgrade;
        }
    }

    // 设置魔法数字 (!M!)
    protected final void setMagic(int magic)
    {
        this.setMagic(magic, 0);
    }
    protected final void setMagic(int magic, int magicUpgrade)
    {
        this.baseMagicNumber = this.magicNumber = magic;
        if (magicUpgrade != 0)
        {
            this.upgradeMagic = true;
            this.magicUpgrade = magicUpgrade;
        }
    }


    // ---------- 自定义变量系统 (Custom Variables) ----------
    // 这些方法允许你创建类似 !D!, !B! 之外的新变量
    // 例如：斥罪的“屏障”数额，或者反伤数额

    protected final void setCustomVar(String key, int base) {
        this.setCustomVar(key, base, 0);
    }
    protected final void setCustomVar(String key, int base, int upgrade) {
        setCustomVarValue(key, base, upgrade);

        // 如果这个变量还没注册过，注册它，这样才能在卡面描述里显示
        if (!customVars.containsKey(key)) {
            QuickDynamicVariable var = new QuickDynamicVariable(key);
            customVars.put(key, var);
            BaseMod.addDynamicVariable(var);
            initializeDescription();
        }
    }

    // 变量类型枚举：决定了这个变量受什么属性影响
    protected enum VariableType {
        DAMAGE, // 受力量、易伤等影响
        BLOCK,  // 受敏捷、脆弱等影响
        MAGIC   // 不受影响，纯数字
    }

    // 带类型的自定义变量设置
    protected final void setCustomVar(String key, VariableType type, int base) {
        setCustomVar(key, type, base, 0);
    }
    protected final void setCustomVar(String key, VariableType type, int base, int upgrade) {
        setCustomVarValue(key, base, upgrade);

        // 根据类型设置计算逻辑
        switch (type) {
            case DAMAGE:
                calculateVarAsDamage(key);
                break;
            case BLOCK:
                calculateVarAsBlock(key);
                break;
        }

        if (!customVars.containsKey(key)) {
            QuickDynamicVariable var = new QuickDynamicVariable(key);
            customVars.put(key, var);
            BaseMod.addDynamicVariable(var);
            initializeDescription();
        }
    }

    // 下面是一系列更高级的自定义变量设置方法，支持自定义计算公式 (PreCalc/PostCalc)
    protected final void setCustomVar(String key, VariableType type, int base, TriFunction<BaseCard, AbstractMonster, Integer, Integer> preCalc) {
        setCustomVar(key, type, base, 0, preCalc);
    }
    protected final void setCustomVar(String key, VariableType type, int base, int upgrade, TriFunction<BaseCard, AbstractMonster, Integer, Integer> preCalc) {
        setCustomVar(key, type, base, upgrade, preCalc, LocalVarInfo::noCalc);
    }
    protected final void setCustomVar(String key, VariableType type, int base, TriFunction<BaseCard, AbstractMonster, Integer, Integer> preCalc, TriFunction<BaseCard, AbstractMonster, Integer, Integer> postCalc) {
        setCustomVar(key, type, base, 0, preCalc, postCalc);
    }
    // 最核心的变量设置逻辑
    protected final void setCustomVar(String key, VariableType type, int base, int upgrade, TriFunction<BaseCard, AbstractMonster, Integer, Integer> preCalc, TriFunction<BaseCard, AbstractMonster, Integer, Integer> postCalc) {
        setCustomVarValue(key, base, upgrade);

        switch (type) {
            case DAMAGE:
                setVarCalculation(key, (c, m, baseVal)->{
                    boolean wasMultiDamage = c.isMultiDamage;
                    c.isMultiDamage = false;

                    int origBase = c.baseDamage, origVal = c.damage;

                    c.baseDamage = preCalc.apply(c, m, baseVal); // 应用自定义预计算

                    if (m != null)
                        c.calculateCardDamage(m); // 调用游戏原生的伤害计算（应用力量等）
                    else
                        c.applyPowers();

                    c.damage = postCalc.apply(c, m, c.damage); // 应用自定义后计算

                    // 还原现场，以免影响卡牌本身的伤害值
                    c.baseDamage = origBase;
                    c.isMultiDamage = wasMultiDamage;

                    int result = c.damage;
                    c.damage = origVal;

                    return result;
                });
                break;
            case BLOCK:
                setVarCalculation(key, (c, m, baseVal)->{
                    int origBase = c.baseBlock, origVal = c.block;

                    c.baseBlock = preCalc.apply(c, m, baseVal);

                    if (m != null)
                        c.calculateCardDamage(m);
                    else
                        c.applyPowers();

                    c.block = postCalc.apply(c, m, c.block);

                    c.baseBlock = origBase;
                    int result = c.block;
                    c.block = origVal;
                    return result;
                });
                break;
            default:
                setVarCalculation(key, (c, m, baseVal)->{
                    int tmp = baseVal;
                    tmp = preCalc.apply(c, m, tmp);
                    tmp = postCalc.apply(c, m, tmp);
                    return tmp;
                });
                break;
        }

        if (!customVars.containsKey(key)) {
            QuickDynamicVariable var = new QuickDynamicVariable(key);
            customVars.put(key, var);
            BaseMod.addDynamicVariable(var);
            initializeDescription();
        }
    }

    private void setCustomVarValue(String key, int base, int upg) {
        cardVariables.compute(key, (k, old)->{
            if (old == null) {
                return new LocalVarInfo(base, upg);
            }
            else {
                old.base = base;
                old.upgrade = upg;
                return old;
            }
        });
    }

    // 设置自定义变量的颜色（默认为普通的卡牌数字色）
    protected final void colorCustomVar(String key, Color normalColor) {
        colorCustomVar(key, normalColor, Settings.GREEN_TEXT_COLOR, Settings.RED_TEXT_COLOR, Settings.GREEN_TEXT_COLOR);
    }
    protected final void colorCustomVar(String key, Color normalColor, Color increasedColor, Color decreasedColor) {
        colorCustomVar(key, normalColor, increasedColor, decreasedColor, increasedColor);
    }
    protected final void colorCustomVar(String key, Color normalColor, Color increasedColor, Color decreasedColor, Color upgradedColor) {
        LocalVarInfo var = getCustomVar(key);
        if (var == null) {
            throw new IllegalArgumentException("尝试为未注册的变量设置颜色: " + key);
        }

        var.normalColor = normalColor;
        var.increasedColor = increasedColor;
        var.decreasedColor = decreasedColor;
        var.upgradedColor = upgradedColor;
    }


    public LocalVarInfo getCustomVar(String key) {
        return cardVariables.get(key);
    }

    // 辅助方法：将变量计算逻辑绑定为伤害类型
    protected void calculateVarAsDamage(String key) {
        setVarCalculation(key, (c, m, base)->{
            boolean wasMultiDamage = c.isMultiDamage;
            c.isMultiDamage = false;

            int origBase = c.baseDamage, origVal = c.damage;

            c.baseDamage = base;
            if (m != null)
                c.calculateCardDamage(m);
            else
                c.applyPowers();

            c.baseDamage = origBase;
            c.isMultiDamage = wasMultiDamage;

            int result = c.damage;
            c.damage = origVal;

            return result;
        });
    }
    // 辅助方法：将变量计算逻辑绑定为格挡类型
    protected void calculateVarAsBlock(String key) {
        setVarCalculation(key, (c, m, base)->{
            int origBase = c.baseBlock, origVal = c.block;

            c.baseBlock = base;
            if (m != null)
                c.calculateCardDamage(m);
            else
                c.applyPowers();

            c.baseBlock = origBase;
            int result = c.block;
            c.block = origVal;
            return result;
        });
    }
    protected void setVarCalculation(String key, TriFunction<BaseCard, AbstractMonster, Integer, Integer> calculation) {
        cardVariables.get(key).calculation = calculation;
    }

    // 获取变量值的各种 Getter
    public int customVarBase(String key) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null)
            return -1;
        return var.base;
    }
    public int customVar(String key) {
        LocalVarInfo var = cardVariables == null ? null : cardVariables.get(key);
        if (var == null)
            return -1;
        return var.value;
    }
    public int[] customVarMulti(String key) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null)
            return null;
        return var.aoeValue;
    }
    public boolean isCustomVarModified(String key) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null)
            return false;
        return var.isModified();
    }
    public boolean customVarUpgraded(String key) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null)
            return false;
        return var.upgraded;
    }


    // ---------- 设置卡牌特性 (消耗/虚无等) ----------
    protected final void setCostUpgrade(int costUpgrade)
    {
        this.costUpgrade = costUpgrade;
        this.upgradeCost = true;
    }
    // 简单设置：初始和升级后一样
    protected final void setExhaust(boolean exhaust) { this.setExhaust(exhaust, exhaust); }
    protected final void setEthereal(boolean ethereal) { this.setEthereal(ethereal, ethereal); }
    protected final void setInnate(boolean innate) {this.setInnate(innate, innate); }
    protected final void setSelfRetain(boolean retain) {this.setSelfRetain(retain, retain); }

    // 复杂设置：初始和升级后不同（例如：升级后不再消耗）
    protected final void setExhaust(boolean baseExhaust, boolean upgExhaust)
    {
        this.baseExhaust = baseExhaust;
        this.upgExhaust = upgExhaust;
        this.exhaust = baseExhaust;
    }
    protected final void setEthereal(boolean baseEthereal, boolean upgEthereal)
    {
        this.baseEthereal = baseEthereal;
        this.upgEthereal = upgEthereal;
        this.isEthereal = baseEthereal;
    }
    protected void setInnate(boolean baseInnate, boolean upgInnate)
    {
        this.baseInnate = baseInnate;
        this.upgInnate = upgInnate;
        this.isInnate = baseInnate;
    }
    protected void setSelfRetain(boolean baseRetain, boolean upgRetain)
    {
        this.baseRetain = baseRetain;
        this.upgRetain = upgRetain;
        this.selfRetain = baseRetain;
    }


    // ---------- 复制卡牌逻辑 ----------
    // 当游戏需要复制这张卡（例如双发、各种事件）时调用
    // 必须确保自定义变量也被正确复制
    @Override
    public AbstractCard makeStatEquivalentCopy() {
        AbstractCard candidate = super.makeStatEquivalentCopy();

        if (candidate instanceof BaseCard) {
            BaseCard card = (BaseCard) candidate;
            card.rawDescription = this.rawDescription;
            card.upgradesDescription = this.upgradesDescription;

            card.baseCost = this.baseCost;

            card.upgradeCost = this.upgradeCost;
            card.upgradeDamage = this.upgradeDamage;
            card.upgradeBlock = this.upgradeBlock;
            card.upgradeMagic = this.upgradeMagic;

            card.costUpgrade = this.costUpgrade;
            card.damageUpgrade = this.damageUpgrade;
            card.blockUpgrade = this.blockUpgrade;
            card.magicUpgrade = this.magicUpgrade;

            card.baseExhaust = this.baseExhaust;
            card.upgExhaust = this.upgExhaust;
            card.baseEthereal = this.baseEthereal;
            card.upgEthereal = this.upgEthereal;
            card.baseInnate = this.baseInnate;
            card.upgInnate = this.upgInnate;
            card.baseRetain = this.baseRetain;
            card.upgRetain = this.upgRetain;

            // 复制所有自定义变量的状态
            for (Map.Entry<String, LocalVarInfo> varEntry : cardVariables.entrySet()) {
                LocalVarInfo target = card.getCustomVar(varEntry.getKey()),
                        current = varEntry.getValue();
                if (target == null) {
                    card.setCustomVar(varEntry.getKey(), current.base, current.upgrade);
                    target = card.getCustomVar(varEntry.getKey());
                }
                target.base = current.base;
                target.value = current.value;
                target.aoeValue = current.aoeValue;
                target.upgrade = current.upgrade;
                target.calculation = current.calculation;
            }
        }

        return candidate;
    }

    // ---------- 升级逻辑 (Upgrade) ----------
    // 当你在休息点升级这张牌时调用
    @Override
    public void upgrade()
    {
        if (!upgraded)
        {
            this.upgradeName();

            // 如果有升级描述，更新描述文本
            if (this.upgradesDescription)
            {
                if (cardStrings.UPGRADE_DESCRIPTION == null)
                {
                    HoolheyakMod.logger.error("卡牌 " + cardID + " 启用了升级描述但 localization 里没写 UPGRADE_DESCRIPTION。");
                }
                else
                {
                    this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
                }
            }

            // 处理费用升级
            if (upgradeCost)
            {
                if (isCostModified && this.cost < this.baseCost && this.cost >= 0) {
                    int diff = this.costUpgrade - this.baseCost;
                    this.upgradeBaseCost(this.cost + diff);
                    if (this.cost < 0)
                        this.cost = 0;
                }
                else {
                    upgradeBaseCost(costUpgrade);
                }
            }

            // 处理数值升级
            if (upgradeDamage)
                this.upgradeDamage(damageUpgrade);

            if (upgradeBlock)
                this.upgradeBlock(blockUpgrade);

            if (upgradeMagic)
                this.upgradeMagicNumber(magicUpgrade);

            // 处理自定义变量升级
            for (LocalVarInfo var : cardVariables.values()) {
                upgradeCustomVar(var);
            }

            // 处理布尔属性变化（如升级后获得固有）
            if (baseExhaust ^ upgExhaust)
                this.exhaust = upgExhaust;

            if (baseInnate ^ upgInnate)
                this.isInnate = upgInnate;

            if (baseEthereal ^ upgEthereal)
                this.isEthereal = upgEthereal;

            if (baseRetain ^ upgRetain)
                this.selfRetain = upgRetain;


            this.initializeDescription();
        }
    }

    // 升级自定义变量的辅助方法
    protected void upgradeCustomVar(String key) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null) {
            throw new NullPointerException("在 " + getClass().getName() + " 中找不到键为 " + key + " 的自定义变量");
        }
        upgradeCustomVar(var, var.upgrade);
    }

    protected void upgradeCustomVar(String key, int amount) {
        LocalVarInfo var = cardVariables.get(key);
        if (var == null) {
            throw new NullPointerException("在 " + getClass().getName() + " 中找不到键为 " + key + " 的自定义变量");
        }
        upgradeCustomVar(var, amount);
    }

    protected void upgradeCustomVar(LocalVarInfo var) {
        upgradeCustomVar(var, var.upgrade);
    }

    protected void upgradeCustomVar(LocalVarInfo var, int amt) {
        if (amt != 0) {
            var.base += amt;
            var.value = var.base;
            var.upgraded = true;
        }
    }

    // ---------- 动态数值计算 ----------
    // 当角色获得力量、敏捷等Buff时，更新卡面显示的数值

    boolean inCalc = false;
    @Override
    public void applyPowers() {
        if (!inCalc) {
            inCalc = true;
            // 重新计算所有自定义变量
            for (LocalVarInfo var : cardVariables.values()) {
                var.value = var.calculation.apply(this, null, var.base);
            }
            // 处理AOE（全体攻击）时的多重数值
            if (isMultiDamage) {
                ArrayList<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
                AbstractMonster m;
                for (LocalVarInfo var : cardVariables.values()) {
                    if (var.aoeValue == null || var.aoeValue.length != monsters.size())
                        var.aoeValue = new int[monsters.size()];

                    for (int i = 0; i < monsters.size(); ++i) {
                        m = monsters.get(i);
                        var.aoeValue[i] = var.calculation.apply(this, m, var.base);
                    }
                }
            }
            inCalc = false;
        }

        super.applyPowers();
    }

    // 当选定目标时计算数值
    @Override
    public void calculateCardDamage(AbstractMonster m) {
        if (!inCalc) {
            inCalc = true;
            for (LocalVarInfo var : cardVariables.values()) {
                var.value = var.calculation.apply(this, m, var.base);
            }
            if (isMultiDamage) {
                ArrayList<AbstractMonster> monsters = AbstractDungeon.getCurrRoom().monsters.monsters;
                for (LocalVarInfo var : cardVariables.values()) {
                    if (var.aoeValue == null || var.aoeValue.length != monsters.size())
                        var.aoeValue = new int[monsters.size()];

                    for (int i = 0; i < monsters.size(); ++i) {
                        m = monsters.get(i);
                        var.aoeValue[i] = var.calculation.apply(this, m, var.base);
                    }
                }
            }
            inCalc = false;
        }

        super.calculateCardDamage(m);
    }

    @Override
    public void resetAttributes() {
        super.resetAttributes();

        for (LocalVarInfo var : cardVariables.values()) {
            var.value = var.base;
        }
    }

    // ---------- 内部类：动态变量实现 ----------
    // BaseMod 钩子，用于告诉游戏如何处理自定义的 !Key!
    private static class QuickDynamicVariable extends DynamicVariable {
        final String localKey, key;

        private BaseCard current = null;

        public QuickDynamicVariable(String key) {
            this.localKey = key;
            this.key = makeID(key);
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public void setIsModified(AbstractCard c, boolean v) {
            if (c instanceof BaseCard) {
                LocalVarInfo var = ((BaseCard) c).getCustomVar(localKey);
                if (var != null)
                    var.forceModified = v;
            }
        }

        @Override
        public boolean isModified(AbstractCard c) {
            return c instanceof BaseCard && (current = (BaseCard) c).isCustomVarModified(localKey);
        }

        @Override
        public int value(AbstractCard c) {
            return c instanceof BaseCard ? ((BaseCard) c).customVar(localKey) : 0;
        }

        @Override
        public int baseValue(AbstractCard c) {
            return c instanceof BaseCard ? ((BaseCard) c).customVarBase(localKey) : 0;
        }

        @Override
        public boolean upgraded(AbstractCard c) {
            return c instanceof BaseCard && ((BaseCard) c).customVarUpgraded(localKey);
        }

        public Color getNormalColor() {
            LocalVarInfo var;
            if (current == null || (var = current.getCustomVar(localKey)) == null)
                return Settings.CREAM_COLOR;

            return var.normalColor;
        }

        public Color getUpgradedColor() {
            LocalVarInfo var;
            if (current == null || (var = current.getCustomVar(localKey)) == null)
                return Settings.GREEN_TEXT_COLOR;

            return var.upgradedColor;
        }

        public Color getIncreasedValueColor() {
            LocalVarInfo var;
            if (current == null || (var = current.getCustomVar(localKey)) == null)
                return Settings.GREEN_TEXT_COLOR;

            return var.increasedColor;
        }

        public Color getDecreasedValueColor() {
            LocalVarInfo var;
            if (current == null || (var = current.getCustomVar(localKey)) == null)
                return Settings.RED_TEXT_COLOR;

            return var.decreasedColor;
        }
    }

    // 内部类：存储变量信息的容器
    protected static class LocalVarInfo {
        int base, value, upgrade;
        int[] aoeValue = null;
        boolean upgraded = false;
        boolean forceModified = false;
        Color normalColor = Settings.CREAM_COLOR;
        Color upgradedColor = Settings.GREEN_TEXT_COLOR;
        Color increasedColor = Settings.GREEN_TEXT_COLOR;
        Color decreasedColor = Settings.RED_TEXT_COLOR;

        TriFunction<BaseCard, AbstractMonster, Integer, Integer> calculation = LocalVarInfo::noCalc;

        public LocalVarInfo(int base, int upgrade) {
            this.base = this.value = base;
            this.upgrade = upgrade;
        }

        private static int noCalc(BaseCard c, AbstractMonster m, int base) {
            return base;
        }

        public boolean isModified() {
            return forceModified || base != value;
        }
    }

    // 轮播功能
    private ArrayList<AbstractCard> carouselList = null;
    private float carouselTimer = 0f;
    private int carouselIndex = 0;
    private static final float CAROUSEL_INTERVAL = 3.0f;

    protected void setCarousel(ArrayList<AbstractCard> cards) {
        this.carouselList = cards;
        if (cards != null && !cards.isEmpty()) {
            this.cardsToPreview = cards.get(0);
        }
    }

    protected void upgradeCarousel() {
        if (carouselList != null) {
            for (AbstractCard c : carouselList) {
                c.upgrade();
            }

            if (!carouselList.isEmpty()) {
                this.cardsToPreview = carouselList.get(carouselIndex);
            }
        }
    }

    public void updateCarouselLogic() {
        if (carouselList != null && !carouselList.isEmpty()) {
            carouselTimer -= Gdx.graphics.getDeltaTime();
            if (carouselTimer <= 0f) {
                carouselTimer = CAROUSEL_INTERVAL;
                carouselIndex = (carouselIndex + 1) % carouselList.size();
                this.cardsToPreview = carouselList.get(carouselIndex);
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (carouselList != null && !carouselList.isEmpty() && hb.hovered) {
            updateCarouselLogic();
            carouselTimer -= Gdx.graphics.getDeltaTime();
            if (carouselTimer <= 0f) {
                carouselTimer = CAROUSEL_INTERVAL;
                carouselIndex = (carouselIndex + 1) % carouselList.size();
                this.cardsToPreview = carouselList.get(carouselIndex);
            }
        }

    }
}