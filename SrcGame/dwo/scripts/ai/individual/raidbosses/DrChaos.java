package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;

/**
 * DrChaos AI
 * @author Kerberos
 */

public class DrChaos extends Quest
{
	private static final int DOCTOR_CHAOS = 32033;
	private static final int STRANGE_MACHINE = 32032;
	private static final int CHAOS_GOLEM = 25512;
	private static boolean _IsGolemSpawned;

	private DrChaos()
	{
		addFirstTalkId(DOCTOR_CHAOS);
		_IsGolemSpawned = false;
	}

	public static void main(String[] args)
	{
		new DrChaos();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("1"))
		{
			L2Npc machine = null;
			for(L2Spawn spawn : SpawnTable.getInstance().getSpawns(STRANGE_MACHINE))
			{
				if(spawn != null)
				{
					machine = spawn.getLastSpawn();
				}
			}
			if(machine != null)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, machine);
				machine.broadcastPacket(new SpecialCamera(machine.getObjectId(), 1, -200, 15, 10000, 20000, 0, 0, 1, 0));
			}
			else
			{
				startQuestTimer("2", 2000, npc, player);
			}
			startQuestTimer("3", 10000, npc, player);
		}
		else if(event.equalsIgnoreCase("2"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
		}
		else if(event.equalsIgnoreCase("3"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1, -150, 10, 3000, 20000, 0, 0, 1, 0));
			startQuestTimer("4", 2500, npc, player);
		}
		else if(event.equalsIgnoreCase("4"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96055, -110759, -3312, 0));
			startQuestTimer("5", 2000, npc, player);
		}
		else if(event.equalsIgnoreCase("5"))
		{
			player.teleToLocation(94832, -112624, -3304);
			npc.teleToLocation(-113091, -243942, -15536);
			if(!_IsGolemSpawned)
			{
				L2Npc golem = addSpawn(CHAOS_GOLEM, 94640, -112496, -3336, 0, false, 0);
				_IsGolemSpawned = true;
				startQuestTimer("6", 1000, golem, player);
				player.sendPacket(new PlaySound(1, "Rm03_A", 0, 0, 0, 0, 0));
			}
		}
		else if(event.equalsIgnoreCase("6"))
		{
			npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 30, -200, 20, 6000, 8000, 0, 0, 1, 0));
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getNpcId() == DOCTOR_CHAOS)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(96323, -110914, -3328, 0));
			startQuestTimer("1", 3000, npc, player);
		}
		return "";
	}
}