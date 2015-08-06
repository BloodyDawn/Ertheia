package dwo.gameserver.model.world.quest;

import dwo.gameserver.model.actor.instance.L2NpcInstance;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 16.07.12
 * Time: 11:19
 */

public class DeSpawnTask implements Runnable
{
	private final L2NpcInstance _npc;
	private final QuestState _st;

	public DeSpawnTask(L2NpcInstance npc, QuestState st)
	{
		_npc = npc;
		_st = st;
	}

	@Override
	public void run()
	{
		_st.removeSpawn(_npc);
	}
}
