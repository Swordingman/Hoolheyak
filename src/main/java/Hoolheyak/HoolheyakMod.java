package Hoolheyak;

import Hoolheyak.events.FalseDomeEvent;
import Hoolheyak.events.IntersectionEvent;
import Hoolheyak.events.WaterLikeShapeEvent;
import Hoolheyak.monsters.Muelsyse;
import Hoolheyak.powers.GravityPower;
import Hoolheyak.relics.*;
import Hoolheyak.util.*;
import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.eventUtil.AddEventParams;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakSkinHelper;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.dungeons.Exordium;
import com.megacrit.cardcrawl.dungeons.TheBeyond;
import com.megacrit.cardcrawl.dungeons.TheCity;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.rewards.RewardSave;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.megacrit.cardcrawl.core.Settings.language;

@SpireInitializer
public class HoolheyakMod implements
        EditCharactersSubscriber, // 订阅角色编辑事件
        EditStringsSubscriber,    // 订阅文本（本地化）编辑事件
        EditKeywordsSubscriber,   // 订阅关键字编辑事件
        AddAudioSubscriber,       // 订阅音频添加事件
        PostInitializeSubscriber, // 订阅初始化后处理事件（用于添加徽章等）
        EditCardsSubscriber,      // 订阅卡牌编辑事件
        EditRelicsSubscriber{    // 订阅遗物编辑事件

    public static ModInfo info;
    public static String modID; // 修改你的 pom.xml 文件来改变这个ID
    static { loadModInfo(); }
    private static final String resourcesFolder = checkResourcesPath();
    public static final Logger logger = LogManager.getLogger(modID); // 用于向控制台输出日志
    public static SpireConfig hoolheyakConfig;

    // 这用于给卡牌、遗物等对象的ID添加前缀，
    // 以避免不同模组使用相同名称时发生冲突。
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    // 由于类顶部的 @SpireInitializer 注解，ModTheSpire 会自动调用此方法。
    public static void initialize() {
        new HoolheyakMod();

        // 注册角色的颜色（必须在早期完成）
        Hoolheyak.Meta.registerColor();
    }

    public HoolheyakMod() {
        BaseMod.subscribe(this); // 这会让 BaseMod 在适当的时候触发所有实现的接口方法。
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {
        // 加载游戏内模组菜单中使用的图标。
        Texture badgeTexture = TextureLoader.getTexture(imagePath("badge.png"));

        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);

        // --- 读取皮肤配置 ---
        try {
            Properties defaults = new Properties();
            defaults.setProperty("skinIndex", "0");
            defaults.setProperty("difficulty", "0");

            hoolheyakConfig = new SpireConfig("Hoolheyak", "HoolheyakConfig", defaults);
            hoolheyakConfig.load();

            // 将读取到的值赋给 SkinHelper
            HoolheyakSkinHelper.currentSkinIndex = hoolheyakConfig.getInt("skinIndex");

            // 安全检查：如果读取的索引超过了当前皮肤数量，重置为0
            if (HoolheyakSkinHelper.currentSkinIndex >= HoolheyakSkinHelper.SKINS.length || HoolheyakSkinHelper.currentSkinIndex < 0) {
                HoolheyakSkinHelper.currentSkinIndex = 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 注册任意颜色卡牌奖励
        BaseMod.registerCustomReward(
                ArchiveRewardEnums.ARCHIVE_ANY_COLOR,
                (rewardSave) -> {
                    return new ArchiveAnyColorReward();
                },
                (customReward) -> {
                    return new RewardSave(customReward.type.toString(), null, 0, 0);
                }
        );

        // 注册无色卡牌奖励
        BaseMod.registerCustomReward(
                ArchiveRewardEnums.ARCHIVE_COLORLESS,
                (rewardSave) -> {
                    return new ArchiveColorlessReward();
                },
                (customReward) -> {
                    return new RewardSave(customReward.type.toString(), null, 0, 0);
                }
        );

        BaseMod.registerCustomReward(
                ArchiveRewardEnums.RECURSIVE_EXPERIMENT,
                (rewardSave) -> {
                    AbstractCard.CardType savedType = AbstractCard.CardType.valueOf(rewardSave.id);
                    return new RecursiveExperimentReward(savedType);
                },
                (customReward) -> {
                    // 存档时：获取当前奖励的 CardType，以字符串形式存在 ID 位置
                    RecursiveExperimentReward myReward = (RecursiveExperimentReward) customReward;
                    // 参数: 类型字符串, ID字符串(存CardType), 数量, 额外金币
                    return new RewardSave(myReward.type.toString(), myReward.savedCardType.name(), 0, 0);
                }
        );

        BaseMod.addEvent(new AddEventParams.Builder(IntersectionEvent.ID, IntersectionEvent.class)
                .dungeonID(Exordium.ID)
                .playerClass(Hoolheyak.Meta.HOOLHEYAK)
                .create());

        BaseMod.addEvent(new AddEventParams.Builder(FalseDomeEvent.ID, FalseDomeEvent.class)
                .dungeonID(TheBeyond.ID)
                .playerClass(Hoolheyak.Meta.HOOLHEYAK)
                .create());

        BaseMod.addEvent(new AddEventParams.Builder(WaterLikeShapeEvent.ID, WaterLikeShapeEvent.class)
                .dungeonID(TheCity.ID)
                .playerClass(Hoolheyak.Meta.HOOLHEYAK)
                .create());

        BaseMod.addMonster(Muelsyse.ID + "_Encounter", Muelsyse.NAME, () -> new MonsterGroup(new Muelsyse(0.0F, 0.0F)));
    }

    /*---------- 本地化 (Localization) ----------*/

    // 这用于根据语言加载适当的本地化文件。
    private static String getLangString()
    {
        return language.name().toLowerCase();
    }
    private static final String defaultLanguage = "zhs"; // 默认语言为英语

    public static final Map<String, KeywordInfo> keywords = new HashMap<>();

    @Override
    public void receiveEditCards() {
        // AutoAdd 会自动扫描你的包，找到所有继承自 AbstractCard 的类并添加它们。
        // 这样你就不用手动一个个 new Card() 了。
        new AutoAdd(modID)
                .packageFilter(HoolheyakMod.class)
                .setDefaultSeen(true) // 默认在图鉴中可见
                .cards();
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelicToCustomPool(new Bibliotheca(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new FrenziedSundial(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new StarMapProjection(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new OldNotes(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new TimeMuseum(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new WeatherBalloon(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelicToCustomPool(new ZodiacModel(), Hoolheyak.Meta.CARD_COLOR);
        BaseMod.addRelic(new CocktailShaker(), RelicType.SHARED);
        BaseMod.addRelic(new TailCareKit(), RelicType.SHARED);
        BaseMod.addRelic(new BeautifulHistoryBook(), RelicType.SHARED);
        BaseMod.addRelic(new AstronomicalTelescope(), RelicType.SHARED);
        BaseMod.addRelic(new BottledCloud(), RelicType.SHARED);
    }

    @Override
    public void receiveEditStrings() {
        loadLocalization(defaultLanguage);
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        // 虽然这里加载了每种类型的本地化文件，但大多数文件只是框架，让你看看格式。
        // 如果你用不到某些文件，可以随意注释掉或删除。
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(MonsterStrings.class,
                localizationPath(lang, "MonstersStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    public static class Keyword {
        public String[] NAMES;       // 对应 JSON 中的 "NAMES"
        public String DESCRIPTION;   // 对应 JSON 中的 "DESCRIPTION"
    }

    @Override
    public void receiveEditKeywords() {
        Gson gson = new Gson();

        String lang = Settings.language == Settings.GameLanguage.ZHS ? "zhs" : "eng";
        String path = HoolheyakMod.localizationPath(lang, "Keywords.json");

        Keyword[] loaded = gson.fromJson(
                Gdx.files.internal(path).readString(StandardCharsets.UTF_8.name()),
                Keyword[].class
        );

        if (loaded != null) {
            for (Keyword k : loaded) {
                BaseMod.addKeyword(
                        HoolheyakMod.modID,
                        k.NAMES,
                        k.DESCRIPTION
                );
            }
        }
    }


    @Override
    public void receiveEditCharacters() {
        // 注册角色
        Hoolheyak.Meta.registerCharacter();
    }

    @Override
    public void receiveAddAudio() {
        // 自动加载音频
        loadAudio(Sounds.class);
    }

    private static final String[] AUDIO_EXTENSIONS = { ".ogg", ".wav", ".mp3" }; // 还有更多有效类型，但不值得在这里全部检查
    private void loadAudio(Class<?> cls) {
        try {
            Field[] fields = cls.getDeclaredFields();
            outer:
            for (Field f : fields) {
                int modifiers = f.getModifiers();
                // 检查字段是否为 public static String
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && f.getType().equals(String.class)) {
                    String s = (String) f.get(null);
                    if (s == null) { // 如果没有定义值（为null），则使用字段名确定路径
                        s = audioPath(f.getName());

                        for (String ext : AUDIO_EXTENSIONS) {
                            String testPath = s + ext;
                            if (Gdx.files.internal(testPath).exists()) {
                                s = testPath;
                                BaseMod.addAudio(s, s); // 注册音频
                                f.set(null, s); // 将生成的路径回写到字段中
                                continue outer;
                            }
                        }
                        throw new Exception("Failed to find an audio file \"" + f.getName() + "\" in " + resourcesFolder + "/audio; check to ensure the capitalization and filename are correct.");
                    }
                    else { // 否则，加载已定义的路径
                        if (Gdx.files.internal(s).exists()) {
                            BaseMod.addAudio(s, s);
                        }
                        else {
                            throw new Exception("Failed to find audio file \"" + s + "\"; check to ensure this is the correct filepath.");
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Exception occurred in loadAudio: ", e);
        }
    }

    // 这些方法用于生成资源文件夹中各个部分的正确文件路径。
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String audioPath(String file) {
        return resourcesFolder + "/audio/" + file;
    }
    public static String imagePath(String file) {
        return resourcesFolder + "/images/" + file;
    }
    public static String characterPath(String file) {
        return resourcesFolder + "/images/character/" + file;
    }
    public static String powerPath(String file) {
        return resourcesFolder + "/images/powers/" + file;
    }
    public static String relicPath(String file) {
        return resourcesFolder + "/images/relics/" + file;
    }

    /**
     * 根据包名检查预期的资源路径。
     */
    private static String checkResourcesPath() {
        String name = HoolheyakMod.class.getName(); // getPackage 在打补丁时可能不稳定，所以使用类名。
        int separator = name.indexOf('.');
        if (separator > 0)
            name = name.substring(0, separator);

        FileHandle resources = new LwjglFileHandle(name, Files.FileType.Internal);

        if (!resources.exists()) {
            throw new RuntimeException("\n\tFailed to find resources folder; expected it to be at  \"resources/" + name + "\"." +
                    " Either make sure the folder under resources has the same name as your mod's package, or change the line\n" +
                    "\t\"private static final String resourcesFolder = checkResourcesPath();\"\n" +
                    "\tat the top of the " + HoolheyakMod.class.getSimpleName() + " java file.");
        }
        if (!resources.child("images").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'images' folder in the mod's 'resources/" + name + "' folder; Make sure the " +
                    "images folder is in the correct location.");
        }
        if (!resources.child("localization").exists()) {
            throw new RuntimeException("\n\tFailed to find the 'localization' folder in the mod's 'resources/" + name + "' folder; Make sure the " +
                    "localization folder is in the correct location.");
        }

        return name;
    }

    /**
     * 这根据 ModTheSpire 存储的信息确定模组的 ID。
     */
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(HoolheyakMod.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }

    // 1. 这是一个“中间人”，把旧的调用转发给模板自带的 imagePath
    public static String makePath(String resourcePath) {
        return imagePath(resourcePath);
    }

    // 2. 专门给卡牌用的简便方法 (可选)
    public static String makeCardPath(String cardName) {
        return imagePath("cards/" + cardName);
    }

    // 用于接收 JSON 数据的简单类 (手机/电脑通用)
    private static class LocalKeyword {
        public String PROPER_NAME; // 增加这个字段以匹配 BaseMod 标准
        public String[] NAMES;
        public String DESCRIPTION;
    }
}



