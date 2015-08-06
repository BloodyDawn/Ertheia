package dwo.scripts.ai.fantasy_isle;

import dwo.gameserver.instancemanager.QuestManager;

public class StartMCShow implements Runnable
{
	@Override
	public void run()
	{
		QuestManager.getInstance().getQuest("MC_Show").notifyEvent("Start", null, null);
	}
}
