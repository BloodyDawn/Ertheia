package dwo.scripts.ai.zone;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

public class PaganTemplate extends Quest
{
	private static final int[] NPCS = {32034, 32035, 32036, 32037};
	private static final int[] TeleportTriols = {32039, 32040};

	private static final int Pass = 8067;
	private static final int Pass1 = 8064;
	private static final int Pass2 = 8065;

	public PaganTemplate()
	{
		addAskId(NPCS, 502);
		addSpawnId(32035);
		addEventId(HookType.ON_SEE_PLAYER);
		addFirstTalkId(TeleportTriols);
	}

	public static void main(String[] args)
	{
		new PaganTemplate();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("Close_Door1"))
		{
			DoorGeoEngine.getInstance().getDoor(19160001).closeMe();
		}

		else if(event.equalsIgnoreCase("Close_Door2"))
		{
			DoorGeoEngine.getInstance().getDoor(19160010).closeMe();
			DoorGeoEngine.getInstance().getDoor(19160011).closeMe();
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 502 && reply == 1)
		{
			if(player.getItemsCount(Pass1) >= 1 || player.getItemsCount(Pass2) >= 1 || player.getItemsCount(Pass) >= 1)
			{
				DoorGeoEngine.getInstance().getDoor(19160001).openMe();
				startQuestTimer("Close_Door1", 10000, null, null);
				return "sanctuary_door_guard002.htm";
			}
			else
			{
				return "sanctuary_door_guard003.htm";
			}
		}
		if(ask == 502 && reply == 2)
		{
			DoorGeoEngine.getInstance().getDoor(19160001).openMe();
			startQuestTimer("Close_Door1", 10000, null, null);
			return "sanctuary_outta_guard002.htm";
		}
		if(ask == 502 && reply == 3)
		{
			if(player.getItemsCount(Pass) >= 1)
			{
				DoorGeoEngine.getInstance().getDoor(19160010).openMe();
				DoorGeoEngine.getInstance().getDoor(19160011).openMe();
				startQuestTimer("Close_Door2", 10000, null, null);
				return "chapel_door_guard002.htm";
			}
			else
			{
				return "chapel_door_guard003.htm";
			}
		}
		if(ask == 502 && reply == 4)
		{
			DoorGeoEngine.getInstance().getDoor(19160010).openMe();
			DoorGeoEngine.getInstance().getDoor(19160011).openMe();
			startQuestTimer("Close_Door2", 10000, null, null);
			return "chapel_outta_guard002.htm";
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == 32039)
		{
			player.teleToLocation(-12766, -35840, -10856); // {[PosX]=35079};{[PosY]=-49758};{[PosZ]=-760}
		}
		else if(npc.getNpcId() == 32040)
		{
			player.teleToLocation(36640, -51218, 718); // {[PosX]=-12766};{[PosY]=-35840};{[PosZ]=-10855}
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.getKnownList().setDistanceToWatch(300); // TODO: Unhardcode
		npc.startWatcherTask(300);
		return super.onSpawn(npc);
	}

	@Override
	public void onSeePlayer(L2Npc npc, L2PcInstance player)
	{
		if(npc == null || npc.isDead() || player == null)
		{
			return;
		}
		if(npc.getNpcId() == 32035)
		{
			if(player.getItemsCount(Pass1) >= 1)
			{
				player.destroyItemByItemId(ProcessType.NPC, Pass1, 1, npc, true);
				player.addItem(ProcessType.NPC, Pass2, 1, npc, true);
			}
		}
	}
}