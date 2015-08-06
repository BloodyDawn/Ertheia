package dwo.gameserver.model.holders;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2ActionType;
import dwo.gameserver.model.items.base.type.L2ItemType;

import java.sql.ResultSet;
import java.sql.SQLException;

import static dwo.gameserver.model.items.base.type.L2ArmorType.SIGIL;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.ANCIENT_CRYSTAL_ENCHANT_AM;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.ANCIENT_CRYSTAL_ENCHANT_WP;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.BLESS_SCRL_ENCHANT_AM;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.BLESS_SCRL_ENCHANT_WP;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.DYE;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.MATERIAL;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.PET_COLLAR;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.SCRL_ENCHANT_AM;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.SCRL_ENCHANT_WP;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.SCRL_INC_ENCHANT_PROP_AM;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.SCRL_INC_ENCHANT_PROP_WP;
import static dwo.gameserver.model.items.base.type.L2EtcItemType.SCROLL;
import static dwo.gameserver.model.items.base.type.L2WeaponType.ANCIENTSWORD;
import static dwo.gameserver.model.items.base.type.L2WeaponType.BIGBLUNT;
import static dwo.gameserver.model.items.base.type.L2WeaponType.BIGSWORD;
import static dwo.gameserver.model.items.base.type.L2WeaponType.BLUNT;
import static dwo.gameserver.model.items.base.type.L2WeaponType.BOW;
import static dwo.gameserver.model.items.base.type.L2WeaponType.CROSSBOW;
import static dwo.gameserver.model.items.base.type.L2WeaponType.DAGGER;
import static dwo.gameserver.model.items.base.type.L2WeaponType.DUAL;
import static dwo.gameserver.model.items.base.type.L2WeaponType.DUALBLUNT;
import static dwo.gameserver.model.items.base.type.L2WeaponType.DUALDAGGER;
import static dwo.gameserver.model.items.base.type.L2WeaponType.DUALFIST;
import static dwo.gameserver.model.items.base.type.L2WeaponType.POLE;
import static dwo.gameserver.model.items.base.type.L2WeaponType.RAPIER;
import static dwo.gameserver.model.items.base.type.L2WeaponType.SWORD;
import static dwo.gameserver.model.items.base.type.L2WeaponType.TWOHANDCROSSBOW;

/**
 * L2GOD Team
 * User: Bacek, Yorie
 * Date: 22.07.11
 * Time: 11:38
 */

public class CommissionItemHolder
{
	private final long _idLot;
	private int _itemObjectId;
	private int _ownerId;
	private long _price;
	private long _locationSlot;
	private int _type;
	private long _timeEnd;
	private byte _days;
	private String _charName;
	private String _itemName;
	private int _itemId;
	private long _count;
	private int _type2;
	private long _bodyPart;
	private int _enchantLevel;
	private int _attackElementType;
	private int _attackElementPower;
	private int[] _elementDefAttr = new int[6];
	private int[] _enchanteffect = {0, 0, 0};
	private int _skin;
	private int _grade;

	public CommissionItemHolder(long IdLot, long timeEnd, String CharName, long price, long count, byte days, L2ItemInstance item, String ItemName)
	{
		_idLot = IdLot;
		_charName = CharName;
		_itemName = ItemName;
		_price = price;
		_count = count;
		_days = days;
		_timeEnd = timeEnd;
		getInfoFromItem(item);
	}

	public CommissionItemHolder(ResultSet rset) throws SQLException
	{
		_idLot = rset.getLong("id_lot");
		_charName = rset.getString("char_name");
		_itemName = rset.getString("item_name");
		_price = rset.getLong("sell_price");
		_timeEnd = rset.getLong("end_date");
		_count = rset.getLong("count");
		_days = rset.getByte("days");

		L2ItemInstance item = L2ItemInstance.restoreFromDb(rset.getInt("owner_id"), rset);
		getInfoFromItem(item);
	}

	public static FiltredPreparedStatement getStatement(CommissionItemHolder pack, ThreadConnection con) throws SQLException
	{
		FiltredPreparedStatement stmt = con.prepareStatement("INSERT INTO commission_list (id_lot, char_name, item_name, sell_price, item_object_id, end_date, days) VALUES (?, ?, ?, ?, ?, ?, ?)");
		stmt.setLong(1, pack._idLot);
		stmt.setString(2, pack._charName);
		stmt.setString(3, pack._itemName);
		stmt.setLong(4, pack._price);
		stmt.setInt(5, pack._itemObjectId);
		stmt.setLong(6, pack._timeEnd);
		stmt.setByte(7, pack._days);
		return stmt;
	}

	private void getInfoFromItem(L2ItemInstance item)
	{
		_ownerId = item.getOwnerId();
		_itemObjectId = item.getObjectId();
		_itemId = item.getItemId();
		_type = getTypeId(item);
		_type2 = item.getItem().getType2();
		_bodyPart = item.getItem().getBodyPart();
		_locationSlot = item.getLocationSlot();
		_enchantLevel = item.getEnchantLevel();
		_attackElementType = item.getAttackElementType();
		_attackElementPower = item.getAttackElementPower();
		for(int i = 0; i < 6; i++)
		{
			_elementDefAttr[i] = item.getElementDefAttr((byte) i);
		}
		_enchanteffect = item.getEnchantEffect();
		_skin = item.getSkin();
		_grade = item.getItem() instanceof L2Armor ? item.getArmorItem().getCrystalType().ordinal() : item.getItem() instanceof L2Weapon ? item.getWeaponItem().getCrystalType().ordinal() : -1;
	}

	public long getLotId()
	{
		return _idLot;
	}

	public long getPrice()
	{
		return _price;
	}

	public int getType()
	{
		return _type;
	}

	public long getLocationSlot()
	{
		return _locationSlot;
	}

	public long getTimeEnd()
	{
		return _timeEnd;
	}

	public String getCharName()
	{
		return _charName;
	}

	public String getItemName()
	{
		return _itemName;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getCount()
	{
		return _count;
	}

	public int getType2()
	{
		return _type2;
	}

	public long getBodyPart()
	{
		return _bodyPart;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public int getAttackElementType()
	{
		return _attackElementType;
	}

	public int getAttackElementPower()
	{
		return _attackElementPower;
	}

	public int getElementDefAttr(int i)
	{
		return _elementDefAttr[i];
	}

	public int[] getEnchantEffect()
	{
		return _enchanteffect;
	}

	public int getSkin()
	{
		return _skin;
	}

	public int getItemGrade()
	{
		return _grade;
	}

	/**
	 * Возвращает тип предмета для пакета
	 * @param item предмет
	 * @return тип предмета
	 */
	public int getTypeId(L2ItemInstance item)
	{
		L2ItemType ItemType = item.getItem().getItemType();

		if(item.getItem().getItemCommissionType() != CommissionCategoryType.NONE)
		{
			return item.getItem().getItemCommissionType().ordinal();
		}

        /*
          Оружие: Одноручные мечи, Магические одноручные мечи, Кинжалы, Рапиры, Двуручные мечи, Древние мечи, Парные мечи, Одноручные дубины, Магические одноручные дубины, Двуручные дубины, Магические двуручные дубины, Парные дубины, Луки, Арбалеты, Кастеты, Копья, прочее оружие
        */
		if(item.isWeapon())
		{
			if(ItemType == SWORD)
			{
				return !item.getWeaponItem().isMagicWeapon() ? CommissionCategoryType.WEAPON_1H_SWORD.ordinal() : CommissionCategoryType.WEAPON_1H_SWORD_MAGIC.ordinal();
			}
			else if(ItemType == DAGGER)
			{
				return CommissionCategoryType.WEAPON_DAGGER.ordinal();
			}
			else if(ItemType == RAPIER)
			{
				return CommissionCategoryType.WEAPON_RAPIER.ordinal();
			}
			else if(ItemType == BIGSWORD)
			{
				return CommissionCategoryType.WEAPON_BIGSWORD.ordinal();
			}
			else if(ItemType == ANCIENTSWORD)
			{
				return CommissionCategoryType.WEAPON_ANCIENTSWORD.ordinal();
			}
			else if(ItemType == DUAL)
			{
				return CommissionCategoryType.WEAPON_DUAL.ordinal();
			}
			else if(ItemType == DUALDAGGER)
			{
				return CommissionCategoryType.WEAPON_DUALDAGGER.ordinal();
			}
			else if(ItemType == BLUNT)
			{
				return !item.getWeaponItem().isMagicWeapon() ? CommissionCategoryType.WEAPON_BLUNT.ordinal() : CommissionCategoryType.WEAPON_BLUNT_MAGIC.ordinal();
			}
			else if(ItemType == BIGBLUNT)
			{
				return !item.getWeaponItem().isMagicWeapon() ? CommissionCategoryType.WEAPON_BIGBLUNT.ordinal() : CommissionCategoryType.WEAPON_BIGBLUNT_MAGIC.ordinal();
			}
			else if(ItemType == DUALBLUNT)
			{
				return CommissionCategoryType.WEAPON_DUALBLUNT.ordinal();
			}
			else if(ItemType == BOW)
			{
				return CommissionCategoryType.WEAPON_BOW.ordinal();
			}
			else if(ItemType == CROSSBOW || ItemType == TWOHANDCROSSBOW)
			{
				return CommissionCategoryType.WEAPON_CROSSBOW.ordinal();
			}
			else if(ItemType == DUALFIST)
			{
				return CommissionCategoryType.WEAPON_DUALFIST.ordinal();
			}
			else
			{
				return ItemType == POLE ? CommissionCategoryType.WEAPON_POLE.ordinal() : CommissionCategoryType.WEAPON_ETC.ordinal();
			}
		}

        /*
         Броня: Шлемы, Верх брони, Низ брони, Цельная броня, Перчатки, Сапоги, Щиты, Символы, Рубахи, Плащи.
        */
		if(item.isArmor())
		{
			if(ItemType == SIGIL) //Символы
			{
				return CommissionCategoryType.ARMOR_SIGIL.ordinal();
			}

            if (item.getItem().getBodyPart() == L2Item.SLOT_HEAD) {
                return CommissionCategoryType.ARMOR_HELMET.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_CHEST) {
                return CommissionCategoryType.ARMOR_CHEST.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_LEGS) {
                return CommissionCategoryType.ARMOR_LEGS.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) {
                return CommissionCategoryType.ARMOR_FULL_BODY.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_GLOVES) {
                return CommissionCategoryType.ARMOR_GLOVES.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_FEET) {
                return CommissionCategoryType.ARMOR_BOOTS.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_L_HAND) {
                return CommissionCategoryType.ARMOR_SHIELD.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_UNDERWEAR) {
                return CommissionCategoryType.ARMOR_UNDERWEAR.ordinal();
            } else if (item.getItem().getBodyPart() == L2Item.SLOT_BACK) {
                return CommissionCategoryType.ARMOR_CLOAK.ordinal();
            }
		}

        /*
         Аксессуары:
             29 Кольца
             30 Серьги
             31 Ожерелья
             32 Пояса
             33 Браслеты
             34 Украшения
        */
        if (item.getItem().getBodyPart() == L2Item.SLOT_R_FINGER || item.getItem().getBodyPart() == L2Item.SLOT_L_FINGER) {
            return CommissionCategoryType.ACCESSORY_RING.ordinal();
        } else if (item.getItem().getBodyPart() == L2Item.SLOT_R_EAR || item.getItem().getBodyPart() == L2Item.SLOT_L_EAR) {
            return CommissionCategoryType.ACCESSORY_EARRING.ordinal();
        } else if (item.getItem().getBodyPart() == L2Item.SLOT_NECK) {
            return CommissionCategoryType.ACCESSORY_NECKLACE.ordinal();
        } else if (item.getItem().getBodyPart() == L2Item.SLOT_BELT) {
            return CommissionCategoryType.ACCESSORY_BELT.ordinal();
        } else if (item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET || item.getItem().getBodyPart() == L2Item.SLOT_L_BRACELET) {
            return CommissionCategoryType.ACCESSORY_BRACELET.ordinal();
        } else if (item.getItem().getBodyPart() == L2Item.SLOT_HAIR || item.getItem().getBodyPart() == L2Item.SLOT_HAIR2 || item.getItem().getBodyPart() == L2Item.SLOT_HAIRALL) {
            return CommissionCategoryType.ACCESSORY_HAIR.ordinal(); //TODO запилить брошку.
        }

        /*
         Припасы:
             35 Зелья
             36 Свитки Модификации Оружия
             37 Свитки Модификации Брони
             38 другие свитки
             39 Заряды Душ
             40 Заряды Духа
            */
		if(item.isPotion() || item.isElixir())
		{
			return CommissionCategoryType.SUPPLIES_POTION.ordinal();
		}
		if(ItemType == SCRL_ENCHANT_WP || ItemType == BLESS_SCRL_ENCHANT_WP || ItemType == SCRL_INC_ENCHANT_PROP_WP || ItemType == ANCIENT_CRYSTAL_ENCHANT_WP)
		{
			return CommissionCategoryType.SUPPLIES_SCROLL_ENCHANT_WEAPON.ordinal();
		}
		if(ItemType == SCRL_ENCHANT_AM || ItemType == BLESS_SCRL_ENCHANT_AM || ItemType == SCRL_INC_ENCHANT_PROP_AM || ItemType == ANCIENT_CRYSTAL_ENCHANT_AM)
		{
			return CommissionCategoryType.SUPPLIES_SCROLL_ENCHANT_ARMOR.ordinal();
		}
		if(ItemType == SCROLL)
		{
			return CommissionCategoryType.SUPPLIES_SCROLL_OTHER.ordinal();
		}
		if(item.getItem().getDefaultAction() == L2ActionType.soulshot || item.getItem().getDefaultAction() == L2ActionType.fishingshot)
		{
			return CommissionCategoryType.SUPPLIES_SOULSHOT.ordinal();
		}
		if(item.getItem().getDefaultAction() == L2ActionType.spiritshot || item.getItem().getDefaultAction() == L2ActionType.summon_spiritshot)
		{
			return CommissionCategoryType.SUPPLIES_SPIRITSHOT.ordinal();
		}//TODO: SUPPLIES_ETC

			/*
			Предметы питомца:
				42  предметы для вызова питомцев,
				43  экипировка питомцев.
			*/
		if(ItemType == PET_COLLAR)
		{
			return CommissionCategoryType.PET_EQUIPMENT.ordinal();
		}
		/*
        else if (ItemType == ???) // экипировка питомцев
            return CommissionCategoryType.PET_SUPPLIES.ordinal();
         */

        /*
          Остальное:
          44    Кристалл
          45    Рецепт
          46    Материалы для создания предметов
          47    Камни Жизни
          48    Кристалл Души
          49    Кристалл Стихии
          50    Камень модифицировать Оружие
          51    Камень Модифицировать Доспех
          52    Книга Заклинаний
          53    Самоцветы
          54    Кошели
          55    Заколки
          56    Магические Заколки
          57    Магические украшения
          58    Краски
          59    Другие предметы
         */

		if(item.isCrystal())
		{
			return CommissionCategoryType.ETC_CRYSTAL.ordinal();
		}
		if(item.getItem().getDefaultAction() == L2ActionType.recipe)
		{
			return CommissionCategoryType.ETC_RECIPE.ordinal();
		}
		if(ItemType == MATERIAL)
		{
			return CommissionCategoryType.ETC_CRAFTING_INGREDIENTS.ordinal();
		}
		// TODO
	        /*
	       else if (ItemType == ???) // Камни Жизни
	           return CommissionCategoryType.ETC_LIFE_STONE.ordinal();
	       else if (ItemType == ???) // Кристаллы Души
	           return CommissionCategoryType.ETC_SOUL_CRYSTAL.ordinal();
	       else if (ItemType == ???) // Редкие Кристаллы Души
	           return CommissionCategoryType.ETC_ATTRIBUTE_STONE.ordinal();
	       else if (ItemType == ???) // книги
	           return CommissionCategoryType.ETC_SPELLBOOK.ordinal();
	       else if (ItemType == ???) // Кошели
	           return CommissionCategoryType.ETC_POUCH.ordinal();
	       else if (ItemType == ???) // Заклепки
	           return CommissionCategoryType.ETC_PIN.ordinal();
	       else if (ItemType == ???) // Магические украшения
	           return CommissionCategoryType.ETC_MAGIC_RUNE_CLIP.ordinal();
	       else if (ItemType == ???) // Магические заколки
	           return CommissionCategoryType.ETC_MAGIC_ORNAMENT.ordinal();
	           TODO ETC_WEAPON_ENCHANT_STONE  ETC_ARMOR_ENCHANT_STONE
	        */
		if(ItemType == DYE)
		{
			return CommissionCategoryType.ETC_DYES.ordinal();
		}

		return CommissionCategoryType.ETC_OTHER.ordinal();
	}

	public int getItemObjectId()
	{
		return _itemObjectId;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public byte getDays()
	{
		return _days;
	}

	public static enum CommissionCategoryType
	{
		NONE,

		WEAPON_1H_SWORD,
		WEAPON_1H_SWORD_MAGIC,
		WEAPON_DAGGER,
		WEAPON_RAPIER,
		WEAPON_BIGSWORD,
		WEAPON_ANCIENTSWORD,
		WEAPON_DUAL,
		WEAPON_DUALDAGGER,
		WEAPON_BLUNT,
		WEAPON_BLUNT_MAGIC,
		WEAPON_BIGBLUNT,
		WEAPON_BIGBLUNT_MAGIC,
		WEAPON_DUALBLUNT,
		WEAPON_BOW,
		WEAPON_CROSSBOW,  //  TWOHANDCROSSBOW
		WEAPON_DUALFIST,
		WEAPON_POLE,
		WEAPON_ETC,

		ARMOR_HELMET,
		ARMOR_CHEST,
		ARMOR_LEGS,
		ARMOR_FULL_BODY,
		ARMOR_GLOVES,
		ARMOR_BOOTS,
		ARMOR_SHIELD,
		ARMOR_SIGIL,
		ARMOR_UNDERWEAR,
		ARMOR_CLOAK,

		ACCESSORY_RING,
		ACCESSORY_EARRING,
		ACCESSORY_NECKLACE,
		ACCESSORY_BELT,
		ACCESSORY_BRACELET,
		ACCESSORY_HAIR,

		SUPPLIES_POTION,
		SUPPLIES_SCROLL_ENCHANT_WEAPON,
		SUPPLIES_SCROLL_ENCHANT_ARMOR,
		SUPPLIES_SCROLL_OTHER,
		SUPPLIES_SOULSHOT,
		SUPPLIES_SPIRITSHOT,
		SUPPLIES_ETC,

		PET_EQUIPMENT,
		PET_SUPPLIES,

		ETC_CRYSTAL,
		ETC_RECIPE,
		ETC_CRAFTING_INGREDIENTS,
		ETC_LIFE_STONE,
		ETC_SOUL_CRYSTAL,
		ETC_ATTRIBUTE_STONE,
		ETC_WEAPON_ENCHANT_STONE,
		ETC_ARMOR_ENCHANT_STONE,
		ETC_SPELLBOOK,
		ETC_GEMSTONE,
		ETC_POUCH,
		ETC_PIN,
		ETC_MAGIC_RUNE_CLIP,
		ETC_MAGIC_ORNAMENT,
		ETC_DYES,
		ETC_OTHER
	}
}
