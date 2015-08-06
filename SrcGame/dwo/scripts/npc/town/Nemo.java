package dwo.scripts.npc.town;

import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2EventMonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.scripts.ai.group_template.Maguen;

public class Nemo extends Quest
{
	private static final int NEMO = 32735;
	private static final int MAGUEN = 18839;

	private static final int COLLECTOR = 15487;

	private static int SPAWN_COUNT;

	public Nemo()
	{
		addAskId(NEMO, -415);
	}

	public static void main(String[] args)
	{
		new Nemo();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equals("despawn"))
		{
			SPAWN_COUNT--;

			if(npc != null)
			{
				npc.getLocationController().delete();
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -415)
		{
			switch(reply)
			{
				case 1:
					if(player.getItemsCount(COLLECTOR) > 0)
					{
						return "nimo003b.htm";
					}
					else if(!player.isInventoryUnder90(false))
					{
						return "nimo003c.htm";
					}
					else
					{
						player.addItem(ProcessType.NPC, COLLECTOR, 1, npc, true);
						return "nimo003a.htm";
					}
				case 2:
					if(SPAWN_COUNT < 10)
					{
						L2EventMonsterInstance mag = (L2EventMonsterInstance) addSpawn(MAGUEN, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0, false);
						mag.eventSetSpawner(player);
						// Mark as 'test' Maguen
						mag.setCustomInt(1);

						Quest AI = QuestManager.getInstance().getQuest(Maguen.class);
						if(AI != null)
						{
							AI.notifyEvent("spawn", mag, player);
						}

						startQuestTimer("despawn", 120000, mag, null);
						SPAWN_COUNT++;
						return "nimo004a.htm";
					}
					else
					{
						return "nimo004b.htm";
					}
			}
		}
		return null;
	}
}