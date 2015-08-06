package dwo.gameserver.model.actor.appearance;

import dwo.gameserver.model.actor.instance.L2PcInstance;

public class PcAppearance
{
	public static final int DEFAULT_NAME_COLOR = 0xffffff;
	public static final int DEFAULT_TITLE_COLOR = 0xffff77;

	private L2PcInstance _owner;

	private int _face;
	private int _customFace;

	private int _hairColor;
	private int _customHairColor;

	private int _hairStyle;
	private int _customHairStyle;

	private boolean _sex; // Female true(1)

	/**
	 * true if the player is invisible
	 */
	private boolean _invisible;
	private boolean _ghostmode;

	/**
	 * The current visible name of this player, not necessarily the real one
	 */
	private String _visibleName;

	/**
	 * The current visible title of this player, not necessarily the real one
	 */
	private String _visibleTitle;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _nameColor = 0xFFFFFF;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _titleColor = 0xFFFF77;

	/**
	 * Отключает цвет имени.
	 */
	private boolean _disableNameColor;
	/**
	 * Отключает цвет титула.
	 */
	private boolean _disableTitleColor;
	/**
	 * Если включено, то сам игрок будет видеть свое настоящее имя.
	 */
	private boolean _ownerSeesRealName;

	public PcAppearance(int face, int hColor, int hStyle, boolean sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}

	/**
	 * @return the visibleName.
	 */
	public String getVisibleName()
	{
		if(_visibleName == null)
		{
			_visibleName = _owner.getName();
		}
		return _visibleName;
	}

	/**
	 * @param visibleName The visibleName to set.
	 */
	public void setVisibleName(String visibleName)
	{
		_visibleName = visibleName;
	}

	/**
	 * @return the visibleTitle.
	 */
	public String getVisibleTitle()
	{
		if(_visibleTitle == null)
		{
			_visibleTitle = _owner.getTitle();
		}
		return _visibleTitle;
	}

	/**
	 * @param visibleTitle The visibleTitle to set.
	 */
	public void setVisibleTitle(String visibleTitle)
	{
		_visibleTitle = visibleTitle;
	}

	public int getCustomFace()
	{
		return _customFace;
	}

	public void setCustomFace(int face)
	{
		_customFace = face;
	}

	public int getCustomHairColor()
	{
		return _customHairColor;
	}

	public void setCustomHairColor(int customHairColor)
	{
		_customHairColor = customHairColor;
	}

	public int getCustomHairStyle()
	{
		return _customHairStyle;
	}

	public void setCustomHairStyle(int customHairStyle)
	{
		_customHairStyle = customHairStyle;
	}

	public int getOldFace()
	{
		return _face;
	}

	public int getOldHairColor()
	{
		return _hairColor;
	}

	public int getOldHairStyle()
	{
		return _hairStyle;
	}

	/**
	 * Возвращает внешний вид лица персонажа.
	 * Если задан приобретенный стиль - возвращается он, иначе - лицо по умолчанию (выбранное при создании персонажа).
	 *
	 * @return
	 */
	public int getFace()
	{
		return _customFace != 0 ? _customFace : _face;
	}

	/**
	 * @param value int
	 */
	public void setFace(int value)
	{
		_face = value;
	}

	/**
	 * Возвращает цвет волос персонажа для отображения внешнего вида.
	 * Если задан приобретенный стиль - возвращается он, иначе - цвет по умолчанию (выбранное при создании персонажа).
	 *
	 * @return
	 */
	public int getHairColor()
	{
		return _customHairColor != 0 ? _customHairColor : _hairColor;
	}

	/**
	 * @param value int
	 */
	public void setHairColor(int value)
	{
		_hairColor = value;
	}

	/**
	 * Возвращает внешний вид лица персонажа.
	 * Если задан приобретенный стиль - возвращается он, иначе - лицо по умолчанию (выбранное при создании персонажа).
	 *
	 * @return
	 */
	public int getHairStyle()
	{
		return _customHairStyle != 0 ? _customHairStyle : _hairStyle;
	}

	/**
	 * @param value int
	 */
	public void setHairStyle(int value)
	{
		_hairStyle = value;
	}

	/**
	 * @return {@code true} if char is female
	 */
	public boolean getSex()
	{
		return _sex;
	}

	/**
	 * @param isfemale boolean
	 */
	public void setSex(boolean isfemale)
	{
		_sex = isfemale;
	}

	public void setInvisible()
	{
		_invisible = true;
	}

	public void setVisible()
	{
		_invisible = false;
	}

	public boolean getInvisible()
	{
		return _invisible;
	}

	public void setGhostMode(boolean b)
	{
		_ghostmode = b;
	}

	public boolean isGhost()
	{
		return _ghostmode;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public void setNameColor(int nameColor)
	{
		if(nameColor < 0)
		{
			return;
		}

		_nameColor = nameColor;
	}

	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public int getTitleColor()
	{
		return _titleColor;
	}

	public void setTitleColor(int titleColor)
	{
		if(titleColor < 0)
		{
			return;
		}

		_titleColor = titleColor;
	}

	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public void disableNameColor()
	{
		_disableNameColor = true;
	}

	public void enableNameColor()
	{
		_disableNameColor = false;
	}

	public boolean isNameColorDisabled()
	{
		return _disableNameColor;
	}

	public void disableTitleColor()
	{
		_disableTitleColor = true;
	}

	public void enableTitleColor()
	{
		_disableTitleColor = false;
	}

	public boolean isTitleColorDisabled()
	{
		return _disableTitleColor;
	}

	public void ownerSeesRealName()
	{
		_ownerSeesRealName = true;
	}

	public void ownerSeesDisplayName()
	{
		_ownerSeesRealName = false;
	}

	/**
	 * @return the owner.
	 */
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(L2PcInstance owner)
	{
		_owner = owner;
	}
}