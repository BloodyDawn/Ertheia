package dwo.gameserver.util;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.xml.ObsceneFilterTable;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterCreateFail;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * General Utility functions related to Gameserver
 */
public class Util
{
	protected static final Logger _log = LogManager.getLogger(Util.class);
	private static NumberFormat ADENA_FORMATTER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

	public static void handleIllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new IllegalPlayerAction(actor, message, punishment), 5000);
	}

	public static String getRelativePath(File base, File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}

	/**
	 * Return degree value of object 2 to the horizontal line with object 1
	 * being the origin
	 */
	public static double calculateAngleFrom(L2Object obj1, L2Object obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	/**
	 * Return degree value of object 2 to the horizontal line with object 1
	 * being the origin
	 */
	public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return angleTarget;
	}

	public static double convertHeadingToDegree(int clientHeading)
	{
		return clientHeading / 182.044444444;
	}

	public static int convertDegreeToClientHeading(double degree)
	{
		if(degree < 0)
		{
			degree = 360 + degree;
		}
		return (int) (degree * 182.044444444);
	}

	public static int calculateHeadingFrom(L2Object obj1, L2Object obj2)
	{
		return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}

	public static int calculateHeadingFrom(double dx, double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if(angleTarget < 0)
		{
			angleTarget = 360 + angleTarget;
		}
		return (int) (angleTarget * 182.044444444);
	}

	/**
	 * @return the distance between the two coordinates in 2D plane
	 */
	public static double calculateDistance(int x1, int y1, int x2, int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}

	/**
	 * @param includeZAxis
	 *            - if true, includes also the Z axis in the calculation
	 * @return the distance between the two coordinates
	 */
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
	{
		double dx = (double) x1 - x2;
		double dy = (double) y1 - y2;

		if(includeZAxis)
		{
			double dz = z1 - z2;
			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		else
		{
			return Math.sqrt(dx * dx + dy * dy);
		}
	}

	/**
	 * @param includeZAxis
	 *            - if true, includes also the Z axis in the calculation
	 * @return the distance between the two objects
	 */
	public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
		{
			return 1000000;
		}

		return calculateDistance(obj1.getLocationController().getX(), obj1.getLocationController().getY(), obj1.getLocationController().getZ(), obj2.getLocationController().getX(), obj2.getLocationController().getY(), obj2.getLocationController().getZ(), includeZAxis);
	}

	/**
	 * (Based on ucfirst() function of PHP)
	 *
	 * @param str
	 *            - the string whose first letter to capitalize
	 * @return a string with the first letter of the {@code str} capitalized
	 */
	public static String capitalizeFirst(String str)
	{
		if(str == null || str.isEmpty())
		{
			return str;
		}
		char[] arr = str.toCharArray();
		char c = arr[0];

		if(Character.isLetter(c))
		{
			arr[0] = Character.toUpperCase(c);
		}
		return new String(arr);
	}

	/**
	 * (Based on ucwords() function of PHP)
	 *
	 * @param str
	 *            - the string to capitalize
	 * @return a string with the first letter of every word in {@code str} capitalized
	 */
	public static String capitalizeWords(String str)
	{
		char[] charArray = str.toCharArray();
		String result = "";

		// Capitalize the first letter in the given string!
		charArray[0] = Character.toUpperCase(charArray[0]);

		for(int i = 0; i < charArray.length; i++)
		{
			if(Character.isWhitespace(charArray[i]))
			{
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			}

			result += Character.toString(charArray[i]);
		}

		return result;
	}

	/**
	 * @return {@code true} if the two objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
		{
			return false;
		}
		if(obj1.getInstanceId() != obj2.getInstanceId())
		{
			return false;
		}
		if(range == -1)
		{
			return true; // not limited
		}

		int rad = 0;
		if(obj1 instanceof L2Character)
		{
			rad += ((L2Character) obj1).getTemplate().getCollisionRadius((L2Character) obj1);
		}
		if(obj2 instanceof L2Character)
		{
			rad += ((L2Character) obj2).getTemplate().getCollisionRadius((L2Character) obj2);
		}

		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();

		if(includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = dx * dx + dy * dy + dz * dz;

			return d <= range * range + 2 * range * rad + rad * rad;
		}
		else
		{
			double d = dx * dx + dy * dy;

			return d <= range * range + 2 * range * rad + rad * rad;
		}
	}

	/**
	 * Checks if object is within short (sqrt(int.max_value)) radius, not using collisionRadius.
	 * Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed.
	 * Not for long distance checks (potential teleports, far away castles etc).
	 *
	 * @param radius
	 *            - the maximum range between the two objects
	 * @param includeZAxis
	 *            - if true, check also Z axis (3-dimensional check), otherwise only 2D
	 * @return {@code true} if objects are within specified range between each other, {@code false} otherwise
	 */
	public static boolean checkIfInShortRadius(int radius, L2Object obj1, L2Object obj2, boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
		{
			return false;
		}
		if(radius == -1)
		{
			return true; // not limited
		}

		int dx = obj1.getX() - obj2.getX();
		int dy = obj1.getY() - obj2.getY();

		if(includeZAxis)
		{
			int dz = obj1.getZ() - obj2.getZ();
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
		else
		{
			return dx * dx + dy * dy <= radius * radius;
		}
	}

	/**
	 * @param str
	 *            - the String to count
	 * @return the number of "words" in a given string.
	 */
	public static int countWords(String str)
	{
		return str.trim().split("\\s+").length;
	}

	/**
	 * (Based on implode() in PHP)
	 *
	 * @param strArray
	 *            - an array of strings to concatenate
	 * @param strDelim
	 *            - the delimiter to put between the strings
	 * @return a delimited string for a given array of string elements.
	 */
	public static String implodeString(String[] strArray, String strDelim)
	{
		StringBuilder result = new StringBuilder();

		for(String strValue : strArray)
		{
			result.append(strValue).append(strDelim);
		}

		return result.toString();
	}

	/**
	 * @param strCollection
	 *            - a collection of strings to concatenate
	 * @param strDelim
	 *            - the delimiter to put between the strings
	 * @see #implodeString(String[] strArray, String strDelim)
	 */
	public static String implodeString(Collection<String> strCollection, String strDelim)
	{
		return implodeString(strCollection.toArray(new String[strCollection.size()]), strDelim);
	}

	/**
	 * (Based on round() in PHP)
	 *
	 * @param number
	 *            - the number to round
	 * @param numPlaces
	 *            - how many digits after decimal point to leave intact
	 * @return the value of {@code number} rounded to specified number of digits after the decimal point.
	 */
	public static float roundTo(float number, int numPlaces)
	{
		if(numPlaces <= 1)
		{
			return Math.round(number);
		}

		float exponent = (float) Math.pow(10, numPlaces);

		return Math.round(number * exponent) / exponent;
	}

	/**
	 * @param text
	 *            - the text to check
	 * @return {@code true} if {@code text} contains only numbers, {@code false} otherwise
	 */
	public static boolean isDigit(String text)
	{
		return text != null && text.matches("[0-9]+");
	}

	/**
	 * @param text
	 *            - the text to check
	 * @return {@code true} if {@code text} contains only letters and/or numbers, {@code false} otherwise
	 */
	public static boolean isAlphaNumeric(String text)
	{
		if(text == null || text.isEmpty())
		{
			return false;
		}
		for(char c : text.toCharArray())
		{
			if(!Character.isLetterOrDigit(c))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean isValidName(String text)
	{
		boolean result = true;
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CNAME_TEMPLATE);
		}
		catch(PatternSyntaxException e) // case of illegal pattern
		{
			_log.log(Level.ERROR, "ERROR : Character name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(text);
		if(!regexp.matches())
		{
			result = false;
		}
		return result;
	}

	/***
	 * Полная проверка ника персонажа на валидность
	 * @param newName проверяемое имя
	 * @return {@link CharacterCreateFail.CharacterCreateFailReason}
	 */
	public static CharacterCreateFail.CharacterCreateFailReason isValidNameEx(String newName)
	{
		if(!isAlphaNumeric(newName) || !isValidName(newName) || ObsceneFilterTable.getInstance().isObsceneWord(newName))
		{
			return CharacterCreateFail.CharacterCreateFailReason.REASON_INCORRECT_NAME;
		}
		if(newName.length() < 1 || newName.length() > 16)
		{
			return CharacterCreateFail.CharacterCreateFailReason.REASON_16_ENG_CHARS;
		}
		if(CharNameTable.getInstance().doesCharNameExist(newName))
		{
			return CharacterCreateFail.CharacterCreateFailReason.REASON_NAME_ALREADY_EXISTS;
		}
		return null;
	}

	public static boolean isValidClanName(String name)
	{
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.CLAN_NAME_TEMPLATE);
		}
		catch(PatternSyntaxException e)
		{
			_log.log(Level.ERROR, "ERROR: Wrong pattern for clan name!");
			pattern = Pattern.compile(".*");
		}
		return pattern.matcher(name).matches();
	}

	/**
	 * Format the specified digit using the digit grouping symbol "," (comma).
	 * For example, 123456789 becomes 123,456,789.
	 *
	 * @param amount
	 *            - the amount of adena
	 * @return the formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return ADENA_FORMATTER.format(amount);
	}

	/**
	 * @param classId текущий ID профессии
	 * @return возвращает ID класса при пробуждении указанной текущей 3-ей профы
	 */
	public static int getAwakenedClassForId(int classId)
	{
		switch(classId)
		{
			// Sigel Knight
			case 90:
				return 148;
			case 91:
				return 149;
			case 99:
				return 150;
			case 106:
				return 151;
			// Tyrr Warrior
			case 88:
				return 152;
			case 89:
				return 153;
			case 113:
				return 154;
			case 114:
				return 154;
			case 118:
				return 156;
			case 131:
				return 157;
			// Othell Rogue
			case 93:
				return 158;
			case 101:
				return 159;
			case 108:
				return 160;
			case 117:
				return 161;
			// Yul Archer
			case 92:
				return 162;
			case 102:
				return 163;
			case 109:
				return 164;
			case 134:
				return 165;
			// Feoh Wizard
			case 94:
				return 166;
			case 95:
				return 167;
			case 103:
				return 168;
			case 110:
				return 169;
			case 132:
			case 133:
				return 170;
			// Iss Enchanter
			case 98:
				return 171;
			case 100:
				return 172;
			case 107:
				return 173;
			case 115:
				return 174;
			case 116:
				return 175;
			/*case 136:  // Инспектор перерождается в Iss Enchanter.
				result = 144;
				break;*/
			// Wynn Summoner
			case 96:
				return 176;
			case 104:
				return 177;
			case 111:
				return 178;
			// Aeore Healer
			case 97:
				return 179;
			case 105:
				return 180;
			case 112:
				return 181;
		}
		return -1;
	}

	/**
	 * @param classId ID класса текущей профессии
	 * @return профессию 3-его класса дял указанного ID професиии
	 */
	public static int getThirdClassForId(int classId)
	{
		int result = -1;
		switch(classId)
		{
			// Elder -> Evas Saint
			case 30:
				result = 105;
				break;
			// Temple Knight -> Evas Templar
			case 20:
				result = 99;
				break;
			// Treasure Hunter -> Adventurer
			case 8:
				result = 93;
				break;
			// Warlock -> Arcana Lord
			case 14:
				result = 96;
				break;
			// Sorcerer -> Archmage
			case 12:
				result = 94;
				break;
			// Bishop -> Cardinal
			case 16:
				result = 97;
				break;
			// Overlord -> Dominator
			case 51:
				result = 115;
				break;
			// Berserker -> Doombringer
			case 127:
				result = 131;
				break;
			// Warcryer -> Doomcryer
			case 52:
				result = 116;
				break;
			// Warlord -> Dreadnought
			case 3:
				result = 89;
				break;
			// Gladiator -> Duelist
			case 2:
				result = 88;
				break;
			// Elemental Summoner -> Elemental Master
			case 28:
				result = 104;
				break;
			// Bounty Hunter -> Fortune Seeker
			case 55:
				result = 117;
				break;
			// Abyss Walker -> GhostHunter
			case 36:
				result = 108;
				break;
			// Phantom Ranger -> Ghost Sentinel
			case 37:
				result = 109;
				break;
			// Tyrant -> Grand Khavatari
			case 48:
				result = 114;
				break;
			// Dark Avenger -> Hell Knight
			case 6:
				result = 91;
				break;
			// Prophet -> Hierophant
			case 17:
				result = 98;
				break;
			// Warsmith -> Maestro
			case 57:
				result = 118;
				break;
			// Silver Ranger -> Moonlight Sentinel
			case 24:
				result = 102;
				break;
			// Spellsinger -> Mystic Muse
			case 27:
				result = 103;
				break;
			// Paladin -> Phoenix Knight
			case 5:
				result = 90;
				break;
			// Hawkeye -> Saggitarius
			case 9:
				result = 92;
				break;
			// Shillien Elder -> Shillen Saint
			case 43:
				result = 112;
				break;
			// Shillien Knight -> Shillien Templar
			case 33:
				result = 106;
				break;
			// Soulbreaker -> Soulhound
			case 128:
				result = 132;
				break;
			case 129:
				result = 133;
				break;
			// Necromancer -> Soultaker
			case 13:
				result = 95;
				break;
			// Blade Dancer -> Spectral Dancer
			case 34:
				result = 107;
				break;
			// Phantom Summoner -> Spectral Master
			case 41:
				result = 111;
				break;
			// Spell Howler -> Storm Screamer
			case 40:
				result = 110;
				break;
			// Sword Singer -> Sword Muse
			case 21:
				result = 100;
				break;
			// Destroyer -> Titan
			case 46:
				result = 113;
				break;
			// Arbalester -> Trickster
			case 130:
				result = 134;
				break;
			// Plains Walker -> Wind Rider
			case 23:
				result = 101;
				break;
			// Judicator -> Inspector
			case 135:
				result = 136;
				break;
		}
		return result;
	}

	/***
	 * Служит для удобства проверки на однотипные классы
	 * @return "родительский" ClassID для новых профессий
	 */
	public static int getGeneralIdForAwaken(int id)
	{
		int ClassId = -1;
		switch(id)
		{
			case 148:
			case 149:
			case 150:
			case 151:
				ClassId = 139;
				break;
			case 152:
			case 153:
			case 154:
			case 155:
			case 156:
			case 157:
				ClassId = 140;
				break;
			case 158:
			case 159:
			case 160:
			case 161:
				ClassId = 141;
				break;
			case 162:
			case 163:
			case 164:
			case 165:
				ClassId = 142;
				break;
			case 166:
			case 167:
			case 168:
			case 169:
			case 170:
				ClassId = 143;
				break;
			case 171:
			case 172:
			case 173:
			case 174:
			case 175:
				ClassId = 144;
				break;
			case 176:
			case 177:
			case 178:
				ClassId = 145;
				break;
			case 179:
			case 180:
			case 181:
				ClassId = 146;
				break;
		}
		return ClassId;
	}

    /***
     * Служит для вывода названий класса
     * @return ClassID для вывода name класса

     */
    public static String getGeneralAwakenName(int id)
    {
        String name = null;
        switch(id)
        {
            case 148:
                name = "Рыцарь Феникса Сигеля";
                break;
            case 149:
                name = "Рыцарь Ада Сигеля";
                break;
            case 150:
                name = "Храмовник Евы Сигеля";
                break;
            case 151:
                name = "Храмовник Шиллен Сигеля";
                break;
            case 152:
                name = "Дуэлист Тира";
                break;
            case 153:
                name = "Полководец Тира";
                break;
            case 154:
                name = "Титан Тира";
                break;
            case 155:
                name = "Аватар Тира";
                break;
            case 156:
                name = "Мастер Тира";
                break;
            case 157:
                name = "Каратель Тира";
                break;
            case 158:
                name = "Авантюрист Одала";
                break;
            case 159:
                name = "Странник Ветра Одала";
                break;
            case 160:
                name = "Призрачный Охотник Одала";
                break;
            case 161:
                name = "Кладоискатель Одала";
                break;
            case 162:
                name = "Снайпер Эура";
                break;
            case 163:
                name = "Страж Лунного Света Эура";
                break;
            case 164:
                name = "Страж Теней Эура";
                break;
            case 165:
                name = "Диверсант Эура";
                break;
            case 166:
                name = "Архимаг Фео";
                break;
            case 167:
                name = "Пожиратель Душ Фео";
                break;
            case 168:
                name = "Магистр Магии Фео";
                break;
            case 169:
                name = "Повелитель Бури Фео";
                break;
            case 170:
                name = "Инквизитор Фео";
                break;
            case 171:
                name = "Апостол Иса";
                break;
            case 172:
                name = "Виртуоз Иса";
                break;
            case 173:
                name = "Призразчный Танцор Иса";
                break;
            case 174:
                name = "Деспот Иса";
                break;
            case 175:
                name = "Глаз Судьбы Иса";
                break;
            case 176:
                name = "Чернокнижник Веньо";
                break;
            case 177:
                name = "Мастер Стихий Веньо";
                break;
            case 178:
                name = "Владыка Теней Веньо";
                break;
            case 179:
                name = "Кардинал Альгиза";
                break;
            case 180:
                name = "Жрец Евы Альгиза";
                break;
            case 181:
                name = "Жрец Шиллен Альгиза";
                break;
        }
        return name;
    }

	/***
	 * @param classId текущий ID класса персонажа
	 * @return ID класса, в который должен перерождаться указанный класс
	 */
	public static int getAwakenRelativeClass(int classId)
	{
		// Если уже переражден
		if(classId >= 139)
		{
			return classId;
		}

		int awakeClassId = -1;

		// По-скольку maleSoulhound и femaleSoulhound перерождаются в один и тот же класс, то хардкод
		if(classId == 132 || classId == 133)
		{
			return 170;
		}

		for(ClassId clsId : ClassId.values())
		{
			if(clsId.getParent() != null && clsId.getParent().getId() == classId)
			{
				awakeClassId = clsId.getId();
			}
		}
		return awakeClassId;
	}

	public static String[] getMemoryInfo()
	{
		double max = Runtime.getRuntime().maxMemory() / 1024; // maxMemory is the upper limit the jvm can use
		double allocated = Runtime.getRuntime().totalMemory() / 1024; // totalMemory the size of the current allocation pool
		double nonAllocated = max - allocated; // non allocated memory till jvm limit
		double cached = Runtime.getRuntime().freeMemory() / 1024; // freeMemory the unused memory in the allocation pool
		double used = allocated - cached; // really used memory
		double useable = max - used; // allocated, but non-used and non-allocated memory
		DecimalFormat df = new DecimalFormat(" (0.0000'%')");
		DecimalFormat df2 = new DecimalFormat(" # 'KB'");

		return new String[]{
			"+----", "| Global Memory Informations at " + getRealTime() + ':', "|    |",
			"| Allowed Memory:" + df2.format(max),
			"|    |= Allocated Memory:" + df2.format(allocated) + df.format(allocated / max * 100),
			"|    |= Non-Allocated Memory:" + df2.format(nonAllocated) + df.format(nonAllocated / max * 100),
			"| Allocated Memory:" + df2.format(allocated),
			"|    |= Used Memory:" + df2.format(used) + df.format(used / max * 100),
			"|    |= Unused (cached) Memory:" + df2.format(cached) + df.format(cached / max * 100),
			"| Useable Memory:" + df2.format(useable) + df.format(useable / max * 100), "+----"
		};
	}

	public static String getRealTime()
	{
		SimpleDateFormat String = new SimpleDateFormat("H:mm:ss");
		return String.format(new Date());
	}

	public static void printMemoryInfo()
	{
		Tools.printSection("Memory");
		for(String line : getMemoryInfo())
		{
			_log.log(Level.INFO, line);
		}
	}

	public static void printCpuInfo()
	{
		Tools.printSection("CPU");
		_log.log(Level.INFO, "Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		_log.log(Level.INFO, "Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
	}

	public static void printOSInfo()
	{
		Tools.printSection("OS");
		_log.log(Level.INFO, "OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		_log.log(Level.INFO, "OS Arch: " + System.getProperty("os.arch"));
	}

	public static boolean isInternalIP(String ipAddress)
	{
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(ipAddress);
            if (addr != null) {
                return addr.isSiteLocalAddress() || addr.isLoopbackAddress();
            }
        }
		catch(UnknownHostException e)
		{
			_log.log(Level.ERROR, "", e);
		}
		return false;
	}

	public static String printData(byte[] data, int len)
	{
		StringBuilder result = new StringBuilder(len << 2);

		int counter = 0;

		for(int i = 0; i < len; i++)
		{
			if(counter % 16 == 0)
			{
				result.append(fillHex(i, 4)).append(": ");
			}

			result.append(fillHex(data[i] & 0xff, 2)).append(' ');
			counter++;
			if(counter == 16)
			{
				result.append("   ");

				int charpoint = i - 15;
				for(int a = 0; a < 16; a++)
				{
					int t1 = 0xFF & data[charpoint++];
					if(t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}

				result.append('\n');
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if(rest > 0)
		{
			for(int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}

			int charpoint = data.length - rest;
			for(int a = 0; a < rest; a++)
			{
				int t1 = 0xFF & data[charpoint++];
				if(t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			result.append('\n');
		}
		return result.toString();
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for(int i = number.length(); i < digits; i++)
		{
			number = '0' + number;
		}
		return number;
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String printData(byte[] raw)
	{
		return printData(raw, raw.length);
	}

	public static String printData(ByteBuffer buf)
	{
		byte[] data = new byte[buf.remaining()];
		buf.get(data);
		String hex = printData(data, data.length);
		buf.position(buf.position() - data.length);
		return hex;
	}

	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		return array;
	}

	public static String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}

	public static String getDateString(Date date)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date.getTime());
	}

	/**
	 * Метод для получения файлов из под папок
	 * @param dir начальная папка
	 * @param extension расширение файла
	 * @return список файлов
	 */
	public static List<File> getAllFileList(File dir, String extension)
	{
		List<File> list = new ArrayList<>();
		if(!(!dir.toString().isEmpty() && dir.toString().charAt(dir.toString().length() - 1) == '/') && !(!dir.toString().isEmpty() && dir.toString().charAt(dir.toString().length() - 1) == '\\'))
		{
			dir = new File(dir.toString() + '/');
		}
		if(!dir.exists())
		{
			_log.log(Level.ERROR, " Folder " + dir.getAbsolutePath() + " doesn't exist!");
		}
		for(File file : dir.listFiles())
		{
			if(file.isDirectory())
			{
				list.addAll(getAllFileList(file, extension).stream().filter(fileName -> fileName.toString().endsWith(extension)).collect(Collectors.toList()));
			}
			else
			{
				if(file.toString().endsWith(extension))
				{
					list.add(file);
				}
			}
		}
		return list;
	}

	/**
	 * Грузит игрока в мир для операций сериализации
	 * в XML-RPC сервере
	 * @param playerName имя игрока
	 * @return инстанс загруженного игрока
	 */
	public static L2PcInstance loadPlayer(String playerName, boolean onlineStatus)
	{
		L2PcInstance player;
		L2GameClient client = new L2GameClient(null);
		client.setDetached(true);

		player = WorldManager.getInstance().getPlayer(playerName);

		if(player != null)
		{
			return player;
		}

		player = L2PcInstance.load(CharNameTable.getInstance().getIdByName(playerName));

		if(player == null)
		{
			return null;
		}

		client.setActiveChar(player);
		player.setOnlineStatus(onlineStatus, false);
		client.setAccountName(player.getAccountNamePlayer());
		client.setState(L2GameClient.GameClientState.IN_GAME);
		player.setClient(client);
		return player;
	}

	public static boolean StringEquals(String s, String[] m)
	{
		for(String n : m)
		{
			if(s.equalsIgnoreCase(n))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Return PenaltyModifier (can use in all cases)
	 *
	 * @param count    - how many times <percents> will be substructed
	 * @param percents - percents to substruct
	 *
	 * @author Styx
	 */

	/*
	 *  This is for fine view only ;)
	 *
	 *	public final static double penaltyModifier(int count, int percents)
	 *	{
	 *		int allPercents = 100;
	 *		int allSubstructedPercents = count * percents;
	 *		int penaltyInPercents = allPercents - allSubstructedPercents;
	 *		double penalty = penaltyInPercents / 100.0;
	 *		return penalty;
	 *	}
	 */
	public static double penaltyModifier(long count, double percents)
	{
		return Math.max(1.0 - count * percents / 100, 0);
	}

	/**
	 * Принадлежность к профессиям определенной рассы
	 *
	 * @param charClass игрок
	 * @param race расса (любая кроме рассы игрока)
	 * @return
	 */
	public static boolean hasChildClassWithRace(ClassId charClass, Race race)
	{
		for(ClassId cid : ClassId.values())
		{
			if(cid.level() == ClassLevel.THIRD.ordinal() && cid.getRace() == race && cid.getParent() == charClass)
			{
				return true;
			}
		}
		return false;
	}

    public static int[] createRangeArray(final int x, final int y)
    {
        final int[] rangeArray = new int[y - x  + 1];

        for(int i = x, j = 0; i <= y; i++, j++)
        {
            rangeArray[j] = i;
        }

        return rangeArray;
    }

    public static int generateHashCode(int id, int level) {
        return id * 1000 + level;
    }
}