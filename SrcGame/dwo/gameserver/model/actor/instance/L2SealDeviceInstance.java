package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.quest.Quest;

import java.util.List;

public class L2SealDeviceInstance extends L2MonsterInstance
{
	private boolean killed;

	public L2SealDeviceInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		killed = false;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!killed)
		{
			L2PcInstance player = null;
			if(killer != null)
			{
				player = killer.getActingPlayer();
				if(player != null)
				{
					List<Quest> quests = getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL);
					if(quests != null)
					{
						for(Quest quest : quests)
						{
							quest.notifyKill(this, player, killer instanceof L2Summon);
						}
					}
				}
			}
			killed = true;
		}
		return false;
	}
}