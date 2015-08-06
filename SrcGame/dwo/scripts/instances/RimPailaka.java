package dwo.scripts.instances;

import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.10.12
 * Time: 22:49
 */

public class RimPailaka extends Quest
{
	private static RimPailaka _scriptInstance;

	public static void main(String[] args)
	{
		_scriptInstance = new RimPailaka();
	}

	public static RimPailaka getInstance()
	{
		return _scriptInstance;
	}

	/***
	 * @param residenceId ID форта или замка
	 * @param partyLeaderName имя лидера группы
	 * @return {@code true} если сейчас в указанном замке\форте проходит Rim Pailaka
	 */
	public boolean isInstanceInProgress(int residenceId, String partyLeaderName)
	{
		return false;
	}
}