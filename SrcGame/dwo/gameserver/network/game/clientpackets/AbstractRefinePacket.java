package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRefinePacket extends L2GameClientPacket
{
	public static final int GRADE_NONE = 0;
	public static final int GRADE_MID = 1;
	public static final int GRADE_HIGH = 2;
	public static final int GRADE_TOP = 3;
	public static final int GRADE_ACC = 4; // Accessory LS
	public static final int GRADE_FORGOTTEN = 5; // TODO: Forgotten ?

	protected static final int[] GEMSTONE_D = {2130};
	protected static final int[] GEMSTONE_C = {2131, 36719};
	protected static final int[] GEMSTONE_B = {2132};
	protected static final int[] GEMSTONE_A = {2133};

	private static final Map<Integer, LifeStone> _lifeStones = new HashMap<>();

	protected static LifeStone getLifeStone(int itemId)
	{
		return _lifeStones.get(itemId);
	}

	/**
	 * @param player проверяемый игрок
	 * @param item предмет для аугментации
	 * @param refinerItem камень жизни
	 * @param gemStones гемстоуны
	 * @return {@code true} если игрок, предмет, Камень Жжизни и гемстоун удоволетворяет условиям для аугментации
	 */
	protected static boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem, L2ItemInstance gemStones)
	{
		if(!isValid(player, item, refinerItem))
		{
			return false;
		}

		// Гемстоуны должны принадлежать игроку
		if(gemStones.getOwnerId() != player.getObjectId())
		{
			return false;
		}

		// и находится в его инвентаре
		if(gemStones.getItemLocation() != L2ItemInstance.ItemLocation.INVENTORY)
		{
			return false;
		}

		CrystalGrade grade = item.getItem().getItemGrade();
		LifeStone ls = _lifeStones.get(refinerItem.getItemId());

		// Проверяем принадлежность гемстоунов к грейду предмета
		boolean gemIdFinded = false;
		for(int id : getGemStoneId(grade))
		{
			if(gemStones.getItemId() == id)
			{
				gemIdFinded = true;
			}
		}
		if(!gemIdFinded)
		{
			return false;
		}

		// Проверяем нужное количество гемстоунов
		return getGemStoneCount(grade, ls.getGrade()) <= gemStones.getCount();

	}

	/**
	 *
	 * @param player проверяемый игрок
	 * @param item аугментируемый предмет
	 * @param refinerItem Камень Жизни
	 * @return {@code true} если предмет, игрок и Камень Жизни соответсвует условиям для аугментации
	 */
	protected static boolean isValid(L2PcInstance player, L2ItemInstance item, L2ItemInstance refinerItem)
	{
		if(!isValid(player, item))
		{
			return false;
		}

		// Камень Жизни должен принадлежать игроку
		if(refinerItem.getOwnerId() != player.getObjectId())
		{
			return false;
		}

		// Камень Жизни должен находится в инвентаре
		if(refinerItem.getItemLocation() != L2ItemInstance.ItemLocation.INVENTORY)
		{
			return false;
		}

		LifeStone ls = _lifeStones.get(refinerItem.getItemId());
		if(ls == null)
		{
			return false;
		}

		// Оружие не может быть аугментировано Камнями Жизни для бижутерии
		if(item.getItem() instanceof L2Weapon && ls.getGrade() == GRADE_ACC)
		{
			return false;
		}

		// И бижутерия не может быть аугментирована Камнями Жизни для оружия
		if(item.getItem() instanceof L2Armor && ls.getGrade() != GRADE_ACC)
		{
			return false;
		}

		// Проверяем уровень Камня Жизни и уровень игрока
		return player.getLevel() >= ls.getPlayerLevel();

	}

	/**
	 * @param player проверяемый игрок
	 * @param item аугментируемый предмет
	 * @return {@code true} если состояние игрока и предмета соответствует условиям
	 */
	protected static boolean isValid(L2PcInstance player, L2ItemInstance item)
	{
		if(!isValid(player))
		{
			return false;
		}

		// Item must belong to owner
		if(item.getOwnerId() != player.getObjectId())
		{
			return false;
		}
		if(item.isAugmented())
		{
			return false;
		}
		if(item.isHeroItem())
		{
			return false;
		}
		if(item.isShadowItem())
		{
			return false;
		}
		if(item.isCommonItem())
		{
			return false;
		}
		if(item.isEtcItem())
		{
			return false;
		}
		if(item.isTimeLimitedItem())
		{
			return false;
		}
		if(item.isPvp() && !Config.AUGMENTATION_WEAPONS_PVP)
		{
			return false;
		}
		if(item.getItem().getCrystalType().ordinal() < CrystalGrade.C.ordinal())
		{
			return false;
		}

		// Source item can be equipped or in inventory
		switch(item.getItemLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
				break;
			default:
				return false;
		}

		if(item.getItem() instanceof L2Weapon)
		{
			switch(((L2Weapon) item.getItem()).getItemType())
			{
				case NONE:
				case FISHINGROD:
					return false;
				default:
					break;
			}
		}
		else if(item.getItem() instanceof L2Armor)
		{
			// only accessories can be augmented
            if (item.getItem().getBodyPart() == L2Item.SLOT_LR_FINGER || item.getItem().getBodyPart() == L2Item.SLOT_LR_EAR || item.getItem().getBodyPart() == L2Item.SLOT_NECK) {
            } else {
                return false;
            }
		}
		else
		{
			return false; // neither weapon nor armor ?
		}

		// blacklist check
		return Arrays.binarySearch(Config.AUGMENTATION_BLACKLIST, item.getItemId()) < 0;

	}

	/**
	 * @param player инстанс игрока
	 * @return {@code true} если состояние игрока удоволетворяет условиям для процесса аугментации
	 */
	protected static boolean isValid(L2PcInstance player)
	{
		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION);
			return false;
		}
		if(player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_TRADING);
			return false;
		}
		if(player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD);
			return false;
		}
		if(player.isParalyzed())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED);
			return false;
		}
		if(player.isFishing())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING);
			return false;
		}
		if(player.isSitting())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN);
			return false;
		}
		if(player.isCursedWeaponEquipped())
		{
			return false;
		}
		return !(player.isEnchanting() || player.isProcessingTransaction());

	}

	/**
	 * @param itemGrade грейд предмета
	 * @return ID гемстоуна для аугментации заданного грейда
	 */
	protected static int[] getGemStoneId(CrystalGrade itemGrade)
	{
		switch(itemGrade)
		{
			case C:
			case B:
				return GEMSTONE_D;
			case A:
			case S:
				return GEMSTONE_C;
			case S80:
			case S84:
				return GEMSTONE_B;
			case R:
			case R95:
			case R99:
				return GEMSTONE_A;
			default:
				return null;
		}
	}

	/**
	 * @param itemGrade грейд предмета, который аугментируем
	 * @param lifeStoneGrade грейд Камня Жизни
	 * @return количество гемстоунов, нужных дял аугментации предмета
	 */
	protected static int getGemStoneCount(CrystalGrade itemGrade, int lifeStoneGrade)
	{
		switch(lifeStoneGrade)
		{
			case GRADE_ACC:
				switch(itemGrade)
				{
					case C:
						return 200;
					case B:
						return 300;
					case A:
						return 200;
					case S:
						return 250;
					case S80:
						return 360;
					case S84:
						return 480;
					// TODO: Верное количество
					case R:
						return 600;
					case R95:
						return 720;
					case R99:
						return 840;
					default:
						return 0;
				}
			default:
				switch(itemGrade)
				{
					case C:
						return 20;
					case B:
						return 30;
					case A:
						return 20;
					case S:
						return 25;
					case S80:
					case S84:
						return 36;
					// TODO: верное количество
					case R:
					case R95:
					case R99:
						return 60;
					default:
						return 0;
				}
		}
	}

	protected static class LifeStone
	{
		private static final int[] LEVELS = {46, 49, 52, 55, 58, 61, 64, 67, 70, 76, 80, 82, 84, 85, 95, 99};
		private final int _grade;
		private final int _level;

		public LifeStone(int grade, int level)
		{
			_grade = grade;
			_level = level;
		}

		public int getLevel()
		{
			return _level;
		}

		public int getGrade()
		{
			return _grade;
		}

		public int getPlayerLevel()
		{
			return LEVELS[_level];
		}
	}

	static
	{
		// itemId, (LS grade, LS level)
		// QUEST
		_lifeStones.put(36718, new LifeStone(GRADE_NONE, 0));

		// NO GRADE
		_lifeStones.put(8723, new LifeStone(GRADE_NONE, 0));
		_lifeStones.put(8724, new LifeStone(GRADE_NONE, 1));
		_lifeStones.put(8725, new LifeStone(GRADE_NONE, 2));
		_lifeStones.put(8726, new LifeStone(GRADE_NONE, 3));
		_lifeStones.put(8727, new LifeStone(GRADE_NONE, 4));
		_lifeStones.put(8728, new LifeStone(GRADE_NONE, 5));
		_lifeStones.put(8729, new LifeStone(GRADE_NONE, 6));
		_lifeStones.put(8730, new LifeStone(GRADE_NONE, 7));
		_lifeStones.put(8731, new LifeStone(GRADE_NONE, 8));
		_lifeStones.put(8732, new LifeStone(GRADE_NONE, 9));
		_lifeStones.put(9573, new LifeStone(GRADE_NONE, 10));
		_lifeStones.put(10483, new LifeStone(GRADE_NONE, 11));
		_lifeStones.put(14166, new LifeStone(GRADE_NONE, 12));
		_lifeStones.put(16160, new LifeStone(GRADE_NONE, 13));
		_lifeStones.put(16164, new LifeStone(GRADE_NONE, 13));
		_lifeStones.put(18563, new LifeStone(GRADE_NONE, 13));
		_lifeStones.put(18568, new LifeStone(GRADE_NONE, 14));
		_lifeStones.put(18573, new LifeStone(GRADE_NONE, 15));

		// MID GRADE
		_lifeStones.put(8733, new LifeStone(GRADE_MID, 0));
		_lifeStones.put(8734, new LifeStone(GRADE_MID, 1));
		_lifeStones.put(8735, new LifeStone(GRADE_MID, 2));
		_lifeStones.put(8736, new LifeStone(GRADE_MID, 3));
		_lifeStones.put(8737, new LifeStone(GRADE_MID, 4));
		_lifeStones.put(8738, new LifeStone(GRADE_MID, 5));
		_lifeStones.put(8739, new LifeStone(GRADE_MID, 6));
		_lifeStones.put(8740, new LifeStone(GRADE_MID, 7));
		_lifeStones.put(8741, new LifeStone(GRADE_MID, 8));
		_lifeStones.put(8742, new LifeStone(GRADE_MID, 9));
		_lifeStones.put(9574, new LifeStone(GRADE_MID, 10));
		_lifeStones.put(10484, new LifeStone(GRADE_MID, 11));
		_lifeStones.put(14167, new LifeStone(GRADE_MID, 12));
		_lifeStones.put(16161, new LifeStone(GRADE_MID, 13));
		_lifeStones.put(16165, new LifeStone(GRADE_MID, 13));
		_lifeStones.put(18564, new LifeStone(GRADE_MID, 13));
		_lifeStones.put(18569, new LifeStone(GRADE_MID, 14));
		_lifeStones.put(18574, new LifeStone(GRADE_MID, 15));

		// HIGH GRADE
		_lifeStones.put(8743, new LifeStone(GRADE_HIGH, 0));
		_lifeStones.put(8744, new LifeStone(GRADE_HIGH, 1));
		_lifeStones.put(8745, new LifeStone(GRADE_HIGH, 2));
		_lifeStones.put(8746, new LifeStone(GRADE_HIGH, 3));
		_lifeStones.put(8747, new LifeStone(GRADE_HIGH, 4));
		_lifeStones.put(8748, new LifeStone(GRADE_HIGH, 5));
		_lifeStones.put(8749, new LifeStone(GRADE_HIGH, 6));
		_lifeStones.put(8750, new LifeStone(GRADE_HIGH, 7));
		_lifeStones.put(8751, new LifeStone(GRADE_HIGH, 8));
		_lifeStones.put(8752, new LifeStone(GRADE_HIGH, 9));
		_lifeStones.put(9575, new LifeStone(GRADE_HIGH, 10));
		_lifeStones.put(10485, new LifeStone(GRADE_HIGH, 11));
		_lifeStones.put(14168, new LifeStone(GRADE_HIGH, 12));
		_lifeStones.put(16162, new LifeStone(GRADE_HIGH, 13));
		_lifeStones.put(16166, new LifeStone(GRADE_HIGH, 13));
		_lifeStones.put(18565, new LifeStone(GRADE_HIGH, 13));
		_lifeStones.put(18570, new LifeStone(GRADE_HIGH, 14));
		_lifeStones.put(18575, new LifeStone(GRADE_HIGH, 15));

		// TOP GRADE
		_lifeStones.put(8753, new LifeStone(GRADE_TOP, 0));
		_lifeStones.put(8754, new LifeStone(GRADE_TOP, 1));
		_lifeStones.put(8755, new LifeStone(GRADE_TOP, 2));
		_lifeStones.put(8756, new LifeStone(GRADE_TOP, 3));
		_lifeStones.put(8757, new LifeStone(GRADE_TOP, 4));
		_lifeStones.put(8758, new LifeStone(GRADE_TOP, 5));
		_lifeStones.put(8759, new LifeStone(GRADE_TOP, 6));
		_lifeStones.put(8760, new LifeStone(GRADE_TOP, 7));
		_lifeStones.put(8761, new LifeStone(GRADE_TOP, 8));
		_lifeStones.put(8762, new LifeStone(GRADE_TOP, 9));
		_lifeStones.put(9576, new LifeStone(GRADE_TOP, 10));
		_lifeStones.put(10486, new LifeStone(GRADE_TOP, 11));
		_lifeStones.put(14169, new LifeStone(GRADE_TOP, 12));
		_lifeStones.put(16163, new LifeStone(GRADE_TOP, 13));
		_lifeStones.put(16167, new LifeStone(GRADE_TOP, 13));
		_lifeStones.put(18566, new LifeStone(GRADE_TOP, 13));
		_lifeStones.put(18571, new LifeStone(GRADE_TOP, 14));
		_lifeStones.put(18576, new LifeStone(GRADE_TOP, 15));

		// ЛС в Бижу
		_lifeStones.put(12754, new LifeStone(GRADE_ACC, 0));
		_lifeStones.put(12755, new LifeStone(GRADE_ACC, 1));
		_lifeStones.put(12756, new LifeStone(GRADE_ACC, 2));
		_lifeStones.put(12757, new LifeStone(GRADE_ACC, 3));
		_lifeStones.put(12758, new LifeStone(GRADE_ACC, 4));
		_lifeStones.put(12759, new LifeStone(GRADE_ACC, 5));
		_lifeStones.put(12760, new LifeStone(GRADE_ACC, 6));
		_lifeStones.put(12761, new LifeStone(GRADE_ACC, 7));
		_lifeStones.put(12762, new LifeStone(GRADE_ACC, 8));
		_lifeStones.put(12763, new LifeStone(GRADE_ACC, 9));
		_lifeStones.put(12821, new LifeStone(GRADE_ACC, 10));
		_lifeStones.put(12822, new LifeStone(GRADE_ACC, 11));
		_lifeStones.put(19166, new LifeStone(GRADE_ACC, 13));
		_lifeStones.put(19167, new LifeStone(GRADE_ACC, 14));
		_lifeStones.put(19168, new LifeStone(GRADE_ACC, 15));

		_lifeStones.put(12840, new LifeStone(GRADE_ACC, 0));
		_lifeStones.put(12841, new LifeStone(GRADE_ACC, 1));
		_lifeStones.put(12842, new LifeStone(GRADE_ACC, 2));
		_lifeStones.put(12843, new LifeStone(GRADE_ACC, 3));
		_lifeStones.put(12844, new LifeStone(GRADE_ACC, 4));
		_lifeStones.put(12845, new LifeStone(GRADE_ACC, 5));
		_lifeStones.put(12846, new LifeStone(GRADE_ACC, 6));
		_lifeStones.put(12847, new LifeStone(GRADE_ACC, 7));
		_lifeStones.put(12848, new LifeStone(GRADE_ACC, 8));
		_lifeStones.put(12849, new LifeStone(GRADE_ACC, 9));
		_lifeStones.put(12850, new LifeStone(GRADE_ACC, 10));
		_lifeStones.put(12851, new LifeStone(GRADE_ACC, 11));
		_lifeStones.put(14008, new LifeStone(GRADE_ACC, 12));
		_lifeStones.put(16177, new LifeStone(GRADE_ACC, 13));
		_lifeStones.put(16178, new LifeStone(GRADE_ACC, 13));

		// FORGOTTEN GRADE
		_lifeStones.put(18567, new LifeStone(GRADE_FORGOTTEN, 0)); // R
		_lifeStones.put(18572, new LifeStone(GRADE_FORGOTTEN, 1)); // R95
		_lifeStones.put(18577, new LifeStone(GRADE_FORGOTTEN, 2)); // R99
	}
}