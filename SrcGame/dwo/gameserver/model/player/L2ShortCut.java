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
package dwo.gameserver.model.player;

public class L2ShortCut
{
	private final int _slot;
	private final int _page;
	private final ShortcutType _type;
	private final int _id;
	private final int _characterType;
	private int _level;
	private int _sharedReuseGroup = -1;

	public L2ShortCut(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int characterType)
	{
		_slot = slotId;
		_page = pageId;
		_type = ShortcutType.values()[shortcutType];
		_id = shortcutId;
		_level = shortcutLevel;
		_characterType = characterType;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public int getPage()
	{
		return _page;
	}

	public int getSlot()
	{
		return _slot;
	}

	public ShortcutType getType()
	{
		return _type;
	}

	public int getCharacterType()
	{
		return _characterType;
	}

	public int getSharedReuseGroup()
	{
		return _sharedReuseGroup;
	}

	public void setSharedReuseGroup(int g)
	{
		_sharedReuseGroup = g;
	}

	public static enum ShortcutType
	{
		NO_TYPE,
		ITEM,
		SKILL,
		ACTION,
		MACRO,
		RECIPE,
		TPBOOKMARK
	}
}
