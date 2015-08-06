package dwo.gameserver.model.player;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.01.13
 * Time: 16:38
 */

public enum PlayerPunishLevel
{
	NONE(0, ""),
	CHAT(1, "chat banned"),
	JAIL(2, "jailed"),
	CHAR(3, "banned"),
	ACC(4, "banned");

	private final int punValue;
	private final String punString;

	PlayerPunishLevel(int value, String string)
	{
		punValue = value;
		punString = string;
	}

	public int value()
	{
		return punValue;
	}

	public String string()
	{
		return punString;
	}
}