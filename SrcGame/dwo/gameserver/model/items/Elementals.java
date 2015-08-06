/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.items;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.funcs.FuncAdd;
import dwo.gameserver.model.skills.base.funcs.LambdaConst;
import dwo.gameserver.model.skills.stats.Stats;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Elementals
{
	public static final byte NONE = -1;
	private byte _element = NONE;
	public static final byte FIRE = 0;
	public static final byte WATER = 1;
	public static final byte WIND = 2;
	public static final byte EARTH = 3;
	public static final byte HOLY = 4;
	public static final byte DARK = 5;
	public static final int FIRST_WEAPON_BONUS = 20;
	public static final int NEXT_WEAPON_BONUS = 5;
	public static final int ARMOR_BONUS = 6;
	public static final int[] WEAPON_VALUES = {
		0,   // Level 0
		25,  // Level 1
		75,  // Level 2
		150, // Level 3
		175, // Level 4
		225, // Level 5
		300, // Level 6
		325, // Level 7
		375, // Level 8
		450, // Level 9
		475, // Level 10
		525, // Level 11
		600, // Level 12
		Integer.MAX_VALUE  // TODO: Higher stones
	};
	public static final int[] ARMOR_VALUES = {
		0,  // Level 0
		12, // Level 1
		30, // Level 2
		60, // Level 3
		72, // Level 4
		90, // Level 5
		120, // Level 6
		132, // Level 7
		150, // Level 8
		180, // Level 9
		192, // Level 10
		210, // Level 11
		240, // Level 12
		252, // Level 13
		270, // Level 14
		300, // Level 15
		Integer.MAX_VALUE  // TODO: Higher stones
	};
	private static final TIntObjectHashMap<ElementalItems> TABLE = new TIntObjectHashMap<>();

	static
	{
		for(ElementalItems item : ElementalItems.values())
		{
			TABLE.put(item._itemId, item);
		}
	}

	// non static:
	private ElementalStatBonus _boni;
	private int _value;

	public Elementals(byte type, int value)
	{
		_element = type;
		_value = value;
		_boni = new ElementalStatBonus(_element, _value);
	}

	public static byte getItemElement(int itemId)
	{
		ElementalItems item = TABLE.get(itemId);
		if(item != null)
		{
			return item._element;
		}
		return NONE;
	}

	public static ElementalItems getItemElemental(int itemId)
	{
		return TABLE.get(itemId);
	}

	public static int getMaxElementLevel(int itemId)
	{
		ElementalItems item = TABLE.get(itemId);
		if(item != null)
		{
			return item._type._maxLevel;
		}
		return -1;
	}

	public static String getElementName(byte element)
	{
		switch(element)
		{
			case FIRE:
				return "Fire";
			case WATER:
				return "Water";
			case WIND:
				return "Wind";
			case EARTH:
				return "Earth";
			case DARK:
				return "Dark";
			case HOLY:
				return "Holy";
		}
		return "None";
	}

	public static byte getElementId(String name)
	{
		String tmp = name.toLowerCase();
		if(tmp.equals("fire"))
		{
			return FIRE;
		}
		if(tmp.equals("water"))
		{
			return WATER;
		}
		if(tmp.equals("wind"))
		{
			return WIND;
		}
		if(tmp.equals("earth"))
		{
			return EARTH;
		}
		if(tmp.equals("dark"))
		{
			return DARK;
		}
		if(tmp.equals("holy"))
		{
			return HOLY;
		}
		return NONE;
	}

	public static byte getOppositeElement(byte element)
	{
		return (byte) (element % 2 == 0 ? element + 1 : element - 1);
	}

	public byte getElement()
	{
		return _element;
	}

	public void setElement(byte type)
	{
		_element = type;
		_boni.setElement(type);
	}

	public int getValue()
	{
		return _value;
	}

	public void setValue(int val)
	{
		_value = val;
		_boni.setValue(val);
	}

	@Override
	public String toString()
	{
		return getElementName(_element) + " +" + _value;
	}

	public void applyBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.applyBonus(player, isArmor);
	}

	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);
	}

	public void updateBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.removeBonus(player);
		_boni.applyBonus(player, isArmor);
	}

	public static enum ElementalItemType
	{
		Stone(3),
		StoneSuper(3),     // TODO сделать + 150 от уровня не выходит (
		StoneSuper60(3),   // TODO сделать + 60 от уровня не выходит (
		Roughore(3),
		Crystal(6),
		CrystalSuper(6),
		CrystalSuper3to6(6),
		Jewel(9),
		Energy(12);

		public final int _maxLevel;

		private ElementalItemType(int maxLvl)
		{
			_maxLevel = maxLvl;
		}
	}

	public static enum ElementalItems
	{
		fireStone(FIRE, 9546, ElementalItemType.Stone),
		waterStone(WATER, 9547, ElementalItemType.Stone),
		windStone(WIND, 9549, ElementalItemType.Stone),
		earthStone(EARTH, 9548, ElementalItemType.Stone),
		divineStone(HOLY, 9551, ElementalItemType.Stone),
		darkStone(DARK, 9550, ElementalItemType.Stone),

		// Руды Super 60
		fireStoneSuper60(FIRE, 36960, ElementalItemType.StoneSuper60),
		waterStoneSuper60(WATER, 36961, ElementalItemType.StoneSuper60),
		windStoneSuper60(WIND, 36962, ElementalItemType.StoneSuper60),
		earthStoneSuper60(EARTH, 36963, ElementalItemType.StoneSuper60),
		divineStoneSuper60(HOLY, 36964, ElementalItemType.StoneSuper60),
		darkStoneSuper60(DARK, 36965, ElementalItemType.StoneSuper60),

		// Руды Super 150
		fireStoneSuper150(FIRE, 36966, ElementalItemType.StoneSuper),
		waterStoneSuper150(WATER, 36967, ElementalItemType.StoneSuper),
		windStoneSuper150(WIND, 36968, ElementalItemType.StoneSuper),
		earthStoneSuper150(EARTH, 36969, ElementalItemType.StoneSuper),
		divineStoneSuper150(HOLY, 36970, ElementalItemType.StoneSuper),
		darkStoneSuper150(DARK, 36971, ElementalItemType.StoneSuper),

		// Кристаллы Super
		fireStoneSuperCristal(FIRE, 36972, ElementalItemType.CrystalSuper),
		waterStoneSuperCristal(WATER, 36973, ElementalItemType.CrystalSuper),
		windStoneSuperCristal(WIND, 36974, ElementalItemType.CrystalSuper),
		earthStoneSuperCristal(EARTH, 36975, ElementalItemType.CrystalSuper),
		divineStoneSuperCristal(HOLY, 36976, ElementalItemType.CrystalSuper),
		darkStoneSuperCristal(DARK, 36977, ElementalItemType.CrystalSuper),

		//Позволяет за один раз увеличить сопротивление на 3 уровня
		fireStoneSuper(FIRE, 33481, ElementalItemType.StoneSuper),
		waterStoneSuper(WATER, 33482, ElementalItemType.StoneSuper),
		windStoneSuper(WIND, 33484, ElementalItemType.StoneSuper),
		earthStoneSuper(EARTH, 33483, ElementalItemType.StoneSuper),
		divineStoneSuper(HOLY, 33486, ElementalItemType.StoneSuper),
		darkStoneSuper(DARK, 33485, ElementalItemType.StoneSuper),

		fireRoughtore(FIRE, 10521, ElementalItemType.Roughore),
		waterRoughtore(WATER, 10522, ElementalItemType.Roughore),
		windRoughtore(WIND, 10524, ElementalItemType.Roughore),
		earthRoughtore(EARTH, 10523, ElementalItemType.Roughore),
		divineRoughtore(HOLY, 10526, ElementalItemType.Roughore),
		darkRoughtore(DARK, 10525, ElementalItemType.Roughore),

		fireCrystal(FIRE, 9552, ElementalItemType.Crystal),
		waterCrystal(WATER, 9553, ElementalItemType.Crystal),
		windCrystal(WIND, 9555, ElementalItemType.Crystal),
		earthCrystal(EARTH, 9554, ElementalItemType.Crystal),
		divineCrystal(HOLY, 9557, ElementalItemType.Crystal),
		darkCrystal(DARK, 9556, ElementalItemType.Crystal),

		//Позволяет за один раз увеличить сопротивление c 3 до 6 уровня
		fireCrystalSuper(FIRE, 33487, ElementalItemType.CrystalSuper3to6),
		waterCrystalSuper(WATER, 33488, ElementalItemType.CrystalSuper3to6),
		earthCrystalSuper(EARTH, 33489, ElementalItemType.CrystalSuper3to6),
		windCrystalSuper(WIND, 33490, ElementalItemType.CrystalSuper3to6),
		darkCrystalSuper(DARK, 33491, ElementalItemType.CrystalSuper3to6),
		divineCrystalSuper(HOLY, 33492, ElementalItemType.CrystalSuper3to6),

		//Сопротивление до 3-го уровня
		fireCrystalSuperMentor(FIRE, 33869, ElementalItemType.StoneSuper),
		waterCrystalSuperMentor(WATER, 33870, ElementalItemType.StoneSuper),
		earthCrystalSuperMentor(EARTH, 33871, ElementalItemType.StoneSuper),
		windCrystalSuperMentor(WIND, 33872, ElementalItemType.StoneSuper),
		divineCrystalSuperMentor(HOLY, 33874, ElementalItemType.StoneSuper),
		darkCrystalSuperMentor(DARK, 33873, ElementalItemType.StoneSuper),

		// и +60
		fireStoneSuperMentor(FIRE, 33863, ElementalItemType.StoneSuper60),
		waterStoneSuperMentor(WATER, 33864, ElementalItemType.StoneSuper60),
		windStoneSuperMentor(WIND, 33866, ElementalItemType.StoneSuper60),
		earthStoneSuperMentor(EARTH, 33865, ElementalItemType.StoneSuper60),
		divineStoneSuperMentor(HOLY, 33868, ElementalItemType.StoneSuper60),
		darkStoneSuperMentor(DARK, 33867, ElementalItemType.StoneSuper60),

		// Super 60
		fireStoneSuper1(FIRE, 34661, ElementalItemType.StoneSuper60),
		waterStoneSuper1(WATER, 34662, ElementalItemType.StoneSuper60),
		windStoneSuper1(WIND, 34664, ElementalItemType.StoneSuper60),
		earthStoneSuper1(EARTH, 34663, ElementalItemType.StoneSuper60),
		divineStoneSuper1(HOLY, 34666, ElementalItemType.StoneSuper60),
		darkStoneSuper1(DARK, 34665, ElementalItemType.StoneSuper60),

		// Super 150
		fireStoneSuper2(FIRE, 34667, ElementalItemType.StoneSuper),
		waterStoneSuper2(WATER, 34668, ElementalItemType.StoneSuper),
		windStoneSuper2(WIND, 34670, ElementalItemType.StoneSuper),
		earthStoneSuper2(EARTH, 34669, ElementalItemType.StoneSuper),
		divineStoneSuper2(HOLY, 34672, ElementalItemType.StoneSuper),
		darkStoneSuper2(DARK, 34671, ElementalItemType.StoneSuper),

		// Super 60 Ивент
		fireStoneSuper3(FIRE, 35729, ElementalItemType.StoneSuper60),
		waterStoneSuper3(WATER, 35730, ElementalItemType.StoneSuper60),
		windStoneSuper3(WIND, 35732, ElementalItemType.StoneSuper60),
		earthStoneSuper3(EARTH, 35731, ElementalItemType.StoneSuper60),
		divineStoneSuper3(HOLY, 35734, ElementalItemType.StoneSuper60),
		darkStoneSuper3(DARK, 35733, ElementalItemType.StoneSuper60),

		// Super 150 Ивент
		fireStoneSuper4(FIRE, 35735, ElementalItemType.StoneSuper),
		waterStoneSuper4(WATER, 35736, ElementalItemType.StoneSuper),
		windStoneSuper4(WIND, 35738, ElementalItemType.StoneSuper),
		earthStoneSuper4(EARTH, 35737, ElementalItemType.StoneSuper),
		divineStoneSuper4(HOLY, 35740, ElementalItemType.StoneSuper),
		darkStoneSuper4(DARK, 35739, ElementalItemType.StoneSuper),

		fireJewel(FIRE, 9558, ElementalItemType.Jewel),
		waterJewel(WATER, 9559, ElementalItemType.Jewel),
		windJewel(WIND, 9561, ElementalItemType.Jewel),
		earthJewel(EARTH, 9560, ElementalItemType.Jewel),
		divineJewel(HOLY, 9563, ElementalItemType.Jewel),
		darkJewel(DARK, 9562, ElementalItemType.Jewel),

		// not yet supported by client (Freya pts)
		fireEnergy(FIRE, 9564, ElementalItemType.Energy),
		waterEnergy(WATER, 9565, ElementalItemType.Energy),
		windEnergy(WIND, 9567, ElementalItemType.Energy),
		earthEnergy(EARTH, 9566, ElementalItemType.Energy),
		divineEnergy(HOLY, 9569, ElementalItemType.Energy),
		darkEnergy(DARK, 9568, ElementalItemType.Energy);

		public final byte _element;
		public final int _itemId;
		public final ElementalItemType _type;

		private ElementalItems(byte element, int itemId, ElementalItemType type)
		{
			_element = element;
			_itemId = itemId;
			_type = type;
		}
	}

	public static class ElementalStatBonus
	{
		private byte _elementalType;
		private int _elementalValue;
		private boolean _active;

		public ElementalStatBonus(byte type, int value)
		{
			_elementalType = type;
			_elementalValue = value;
			_active = false;
		}

		public void applyBonus(L2PcInstance player, boolean isArmor)
		{
			// make sure the bonuses are not applied twice..
			if(_active)
			{
				return;
			}

			switch(_elementalType)
			{
				case FIRE:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.FIRE_RES : Stats.FIRE_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
				case WATER:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.WATER_RES : Stats.WATER_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
				case WIND:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.WIND_RES : Stats.WIND_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
				case EARTH:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.EARTH_RES : Stats.EARTH_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
				case DARK:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.DARK_RES : Stats.DARK_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
				case HOLY:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.HOLY_RES : Stats.HOLY_POWER, 0x40, this, new LambdaConst(_elementalValue)));
					break;
			}

			_active = true;
		}

		public void removeBonus(L2PcInstance player)
		{
			// make sure the bonuses are not removed twice
			if(!_active)
			{
				return;
			}

			player.removeStatsOwner(this);

			_active = false;
		}

		public void setValue(int val)
		{
			_elementalValue = val;
		}

		public void setElement(byte type)
		{
			_elementalType = type;
		}
	}
}