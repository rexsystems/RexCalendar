package me.rexsystems.rexCalendar.config;

import me.rexsystems.rexCalendar.claim.DayKeys;
import me.rexsystems.rexCalendar.claim.MissedDayPolicy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class LoadedConfig {

    public static final int MAX_DAY_SLOT = 539;
    public static final int MAX_ITEM_AMOUNT = 2304;

    private final FileConfiguration config;
    private final ZoneId zone;
    private final MissedDayPolicy missedDays;
    private final int ipLimit;
    private final int guiSlots;
    private final StorageSettings storage;
    private final Map<Integer, DayDefinition> days;
    private final Map<Integer, DayDefinition> daysBySlot;
    private final List<DisplayItem> decorations;
    private final DisplayItem previous;
    private final DisplayItem next;
    private final DisplayItem close;

    private LoadedConfig(
            FileConfiguration config,
            ZoneId zone,
            MissedDayPolicy missedDays,
            int ipLimit,
            int guiSlots,
            StorageSettings storage,
            Map<Integer, DayDefinition> days,
            Map<Integer, DayDefinition> daysBySlot,
            List<DisplayItem> decorations,
            DisplayItem previous,
            DisplayItem next,
            DisplayItem close
    ) {
        this.config = config;
        this.zone = zone;
        this.missedDays = missedDays;
        this.ipLimit = ipLimit;
        this.guiSlots = guiSlots;
        this.storage = storage;
        this.days = days;
        this.daysBySlot = daysBySlot;
        this.decorations = decorations;
        this.previous = previous;
        this.next = next;
        this.close = close;
    }

    public static LoadedConfig read(FileConfiguration config, Logger logger) {
        Logger log = logger == null ? Logger.getLogger("RexCalendar") : logger;
        ZoneId zone = readZone(config, log);
        MissedDayPolicy missed = readMissed(config, log);
        int ipLimit = Math.max(0, config.getInt("settings.ip-limit", 0));
        int guiSlots = readSlots(config, log);
        StorageSettings storage = readStorage(config, log);
        Map<Integer, DayDefinition> days = new LinkedHashMap<>();
        Map<Integer, DayDefinition> bySlot = new HashMap<>();
        readDays(config, log, days, bySlot);
        List<DisplayItem> decorations = readDecorations(config);
        DisplayItem previous = readNav(config, "previous", 45);
        DisplayItem next = readNav(config, "next", 53);
        DisplayItem close = readNav(config, "close", 49);
        warnNav(log, guiSlots, previous, "previous");
        warnNav(log, guiSlots, next, "next");
        warnNav(log, guiSlots, close, "close");
        return new LoadedConfig(config, zone, missed, ipLimit, guiSlots, storage, days, bySlot, decorations, previous, next, close);
    }

    public ZoneId zone() {
        return zone;
    }

    public MissedDayPolicy missedDays() {
        return missedDays;
    }

    public int ipLimit() {
        return ipLimit;
    }

    public int guiSlots() {
        return guiSlots;
    }

    public int pages() {
        int max = 0;
        boolean any = false;
        for (DayDefinition day : days.values()) {
            if (day.slot() < 0) {
                continue;
            }
            any = true;
            max = Math.max(max, day.slot());
        }
        if (!any) {
            return 1;
        }
        return max / guiSlots + 1;
    }

    public StorageSettings storage() {
        return storage;
    }

    public Map<Integer, DayDefinition> days() {
        return days;
    }

    public DayDefinition day(int day) {
        return days.get(day);
    }

    public DayDefinition dayAt(int page, int localSlot) {
        if (localSlot < 0) {
            return null;
        }
        return daysBySlot.get(page * guiSlots + localSlot);
    }

    public List<DisplayItem> decorations() {
        return decorations;
    }

    public DisplayItem previous() {
        return previous;
    }

    public DisplayItem next() {
        return next;
    }

    public DisplayItem closeButton() {
        return close;
    }

    public boolean worldAllowed(String worldName) {
        if (worldName == null) {
            return false;
        }
        List<String> enabled = config.getStringList("worlds.enabled");
        if (enabled != null && !enabled.isEmpty()) {
            return enabled.stream().anyMatch(world -> world.equalsIgnoreCase(worldName));
        }
        List<String> disabled = config.getStringList("worlds.disabled");
        if (disabled == null || disabled.isEmpty()) {
            return true;
        }
        return disabled.stream().noneMatch(world -> world.equalsIgnoreCase(worldName));
    }

    public String message(String path) {
        return config.getString("messages." + path, "");
    }

    public String prefix() {
        String prefix = message("prefix");
        return prefix == null ? "" : prefix;
    }

    public String prefixed(String path) {
        String raw = message(path);
        if (raw == null) {
            return "";
        }
        return raw.replace("<prefix>", prefix()).replace("{prefix}", prefix());
    }

    public List<String> helpLines() {
        List<String> lines = config.getStringList("messages.help");
        return lines == null ? List.of() : lines;
    }

    public String guiTitle() {
        return config.getString("gui.title", "<white>Calendar");
    }

    public String fillerMaterial() {
        return config.getString("gui.filler.material", "GRAY_STAINED_GLASS_PANE");
    }

    public String fillerName() {
        return config.getString("gui.filler.name", " ");
    }

    public String dayName() {
        return config.getString("gui.day.name", "<gold>Day {day}");
    }

    public List<String> dayLore() {
        List<String> lore = config.getStringList("gui.day.lore");
        if (lore == null || lore.isEmpty()) {
            return List.of("{status}");
        }
        return lore;
    }

    public String availableMaterial() {
        return config.getString("gui.day.available-material", "CHEST");
    }

    public String claimedMaterial() {
        return config.getString("gui.day.claimed-material", "LIME_DYE");
    }

    public String missedMaterial() {
        return config.getString("gui.day.missed-material", "GRAY_DYE");
    }

    public String lockedMaterial() {
        return config.getString("gui.day.locked-material", "BLACK_DYE");
    }

    public String status(String key, String fallback) {
        String value = config.getString("gui.status." + key, fallback);
        return value == null ? "" : value;
    }

    public boolean soundEnabled() {
        return config.getBoolean("sounds.enabled", false);
    }

    public String soundName() {
        return config.getString("sounds.sound", "");
    }

    public float soundVolume() {
        double volume = config.getDouble("sounds.volume", 1.0);
        if (volume < 0) {
            return 0f;
        }
        return (float) Math.min(volume, 10.0);
    }

    public float soundPitch() {
        double pitch = config.getDouble("sounds.pitch", 1.0);
        if (pitch <= 0) {
            return 1f;
        }
        return (float) Math.min(pitch, 2.0);
    }

    public boolean titleEnabled() {
        return config.getBoolean("title.enabled", false);
    }

    public String titleText() {
        return config.getString("title.text", "");
    }

    public String subtitleText() {
        return config.getString("title.subtitle", "");
    }

    public int titleFadeIn() {
        return clamp(config.getInt("title.fade-in", 10), 0, 1200);
    }

    public int titleStay() {
        return clamp(config.getInt("title.stay", 40), 0, 1200);
    }

    public int titleFadeOut() {
        return clamp(config.getInt("title.fade-out", 10), 0, 1200);
    }

    public boolean actionbarEnabled() {
        return config.getBoolean("actionbar.enabled", false);
    }

    public String actionbarText() {
        return config.getString("actionbar.text", "");
    }

    public int actionbarTicks() {
        return clamp(config.getInt("actionbar.ticks", 60), 0, 600);
    }

    public boolean bossbarEnabled() {
        return config.getBoolean("bossbar.enabled", false);
    }

    public String bossbarText() {
        return config.getString("bossbar.text", "");
    }

    public String bossbarColor() {
        return config.getString("bossbar.color", "YELLOW");
    }

    public String bossbarStyle() {
        return config.getString("bossbar.style", "SOLID");
    }

    public int bossbarSeconds() {
        return clamp(config.getInt("bossbar.seconds", 5), 1, 60);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ZoneId readZone(FileConfiguration config, Logger logger) {
        String raw = config.getString("settings.timezone", "Europe/Bucharest");
        if (raw == null || raw.isBlank()) {
            logger.warning("settings.timezone is blank. Using Europe/Bucharest.");
            return DayKeys.DEFAULT_ZONE;
        }
        try {
            return ZoneId.of(raw.trim());
        } catch (DateTimeException ex) {
            logger.warning("Unknown settings.timezone '" + raw + "'. Using Europe/Bucharest.");
            return DayKeys.DEFAULT_ZONE;
        }
    }

    private static MissedDayPolicy readMissed(FileConfiguration config, Logger logger) {
        String raw = config.getString("settings.missed-days", "today-only");
        if (raw == null || raw.isBlank() || raw.trim().equalsIgnoreCase("today-only") || raw.trim().equalsIgnoreCase("claim-past")) {
            return MissedDayPolicy.parse(raw);
        }
        logger.warning("Unknown settings.missed-days '" + raw + "'. Using today-only.");
        return MissedDayPolicy.TODAY_ONLY;
    }

    private static int readSlots(FileConfiguration config, Logger logger) {
        int rows = config.getInt("gui.rows", 6);
        int clamped = clamp(rows, 1, 6);
        if (clamped != rows) {
            logger.warning("gui.rows " + rows + " is outside 1-6. Using " + clamped + ".");
        }
        return clamped * 9;
    }

    private static StorageSettings readStorage(FileConfiguration config, Logger logger) {
        String type = config.getString("storage.type", "sqlite");
        boolean mysql = type != null && type.trim().equalsIgnoreCase("mysql");
        String host = config.getString("storage.mysql.host", "127.0.0.1");
        int port = clamp(config.getInt("storage.mysql.port", 3306), 1, 65535);
        String database = config.getString("storage.mysql.database", "rexcalendar");
        String username = config.getString("storage.mysql.username", "calendar");
        String password = config.getString("storage.mysql.password", "");
        int pool = config.getInt("storage.mysql.pool-size", 4);
        int poolSize = clamp(pool, 1, 20);
        if (poolSize != pool) {
            logger.warning("storage.mysql.pool-size " + pool + " is outside 1-20. Using " + poolSize + ".");
        }
        boolean ssl = config.getBoolean("storage.mysql.ssl", false);
        if (mysql) {
            if (host == null || !host.matches("[A-Za-z0-9._:-]+")) {
                logger.warning("storage.mysql.host is not a hostname. SQLite will be used.");
                mysql = false;
            } else if (database == null || !database.matches("[A-Za-z0-9_]+")) {
                logger.warning("storage.mysql.database may only use letters, numbers, and underscore. SQLite will be used.");
                mysql = false;
            }
        }
        return new StorageSettings(mysql, host, port, database == null ? "" : database, username, password == null ? "" : password, poolSize, ssl);
    }

    private static void readDays(FileConfiguration config, Logger logger, Map<Integer, DayDefinition> days, Map<Integer, DayDefinition> bySlot) {
        ConfigurationSection section = config.getConfigurationSection("days");
        if (section == null) {
            return;
        }
        List<Integer> numbers = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            try {
                numbers.add(Integer.parseInt(key));
            } catch (NumberFormatException ex) {
                logger.warning("Ignoring days." + key + " because the key is not a day number.");
            }
        }
        Collections.sort(numbers);
        for (int number : numbers) {
            ConfigurationSection day = section.getConfigurationSection(String.valueOf(number));
            if (day == null || number < 1 || number > 31) {
                logger.warning("Ignoring days." + number + ".");
                continue;
            }
            int slot = day.getInt("slot", -1);
            if (slot > MAX_DAY_SLOT) {
                logger.warning("Day " + number + " slot " + slot + " is above " + MAX_DAY_SLOT + " and was hidden.");
                slot = -1;
            }
            List<RewardDefinition> rewards = readRewards(day, number, logger);
            DayDefinition definition = new DayDefinition(
                    number,
                    slot,
                    day.getString("material", ""),
                    day.getString("name", ""),
                    day.getStringList("lore"),
                    rewards
            );
            days.put(number, definition);
            if (slot < 0) {
                continue;
            }
            DayDefinition previous = bySlot.put(slot, definition);
            if (previous != null) {
                logger.warning("Day " + number + " uses slot " + slot + ", replacing day " + previous.day() + ".");
            }
        }
    }

    private static List<RewardDefinition> readRewards(ConfigurationSection day, int number, Logger logger) {
        List<RewardDefinition> rewards = new ArrayList<>();
        for (Map<?, ?> map : day.getMapList("rewards")) {
            double chance = number(map.get("chance"), 100.0);
            double clamped = Math.max(0.0, Math.min(100.0, chance));
            if (clamped != chance) {
                logger.warning("Day " + number + " chance " + chance + " was clamped to " + clamped + ".");
            }
            List<String> commands = strings(map.get("commands"));
            List<ItemReward> items = readItems(map.get("items"), number, logger);
            RewardDefinition reward = new RewardDefinition(clamped, commands, items);
            if (reward.grantsAnything()) {
                rewards.add(reward);
            }
        }
        return List.copyOf(rewards);
    }

    private static List<ItemReward> readItems(Object raw, int day, Logger logger) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<ItemReward> items = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> map)) {
                continue;
            }
            String material = string(map.get("material"));
            if (material.isBlank()) {
                continue;
            }
            int amount = (int) number(map.get("amount"), 1);
            if (amount < 1) {
                amount = 1;
            }
            if (amount > MAX_ITEM_AMOUNT) {
                logger.warning("Day " + day + " item amount " + amount + " was clamped to " + MAX_ITEM_AMOUNT + ".");
                amount = MAX_ITEM_AMOUNT;
            }
            int model = (int) number(map.get("custom-model-data"), 0);
            if (model < 0) {
                model = 0;
            }
            items.add(new ItemReward(material, amount, string(map.get("name")), strings(map.get("lore")), model));
        }
        return List.copyOf(items);
    }

    private static List<DisplayItem> readDecorations(FileConfiguration config) {
        List<DisplayItem> items = new ArrayList<>();
        for (Map<?, ?> map : config.getMapList("gui.decoration")) {
            int slot = (int) number(map.get("slot"), -1);
            if (slot > MAX_DAY_SLOT) {
                slot = -1;
            }
            int model = (int) number(map.get("custom-model-data"), 0);
            items.add(new DisplayItem(slot, string(map.get("material")), string(map.get("name")), strings(map.get("lore")), Math.max(0, model)));
        }
        return List.copyOf(items);
    }

    private static DisplayItem readNav(FileConfiguration config, String key, int fallbackSlot) {
        ConfigurationSection section = config.getConfigurationSection("gui.nav." + key);
        if (section == null) {
            return new DisplayItem(-1, "ARROW", "", List.of(), 0);
        }
        int slot = section.getInt("slot", fallbackSlot);
        int model = section.getInt("custom-model-data", 0);
        return new DisplayItem(slot, section.getString("material", "ARROW"), section.getString("name", ""), section.getStringList("lore"), Math.max(0, model));
    }

    private static void warnNav(Logger logger, int slots, DisplayItem button, String name) {
        if (button.slot() >= slots) {
            logger.warning("Nav button " + name + " slot " + button.slot() + " is outside this chest. Use -1 to hide it.");
        }
    }

    private static double number(Object raw, double fallback) {
        if (raw instanceof Number value) {
            return value.doubleValue();
        }
        if (raw instanceof String text) {
            try {
                return Double.parseDouble(text.trim());
            } catch (NumberFormatException ex) {
                return fallback;
            }
        }
        return fallback;
    }

    private static String string(Object raw) {
        return raw == null ? "" : String.valueOf(raw);
    }

    private static List<String> strings(Object raw) {
        if (raw instanceof String text) {
            if (text.isBlank()) {
                return List.of();
            }
            return List.of(text);
        }
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object entry : list) {
            if (entry == null) {
                continue;
            }
            String text = String.valueOf(entry);
            if (!text.isBlank()) {
                out.add(text);
            }
        }
        return List.copyOf(out);
    }
}
