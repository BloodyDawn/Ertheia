package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.NS;

public class Gargos extends Quest
{
	private static final int GARGOS = 18607;
	private boolean _isStarted;

	public Gargos()
	{

		addAttackId(GARGOS);
		addKillId(GARGOS);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Gargos();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == GARGOS)
		{
			if(!_isStarted)
			{
				startQuestTimer("TimeToFire", 60000, npc, player);
				_isStarted = true;
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("TimeToFire"))
		{
			_isStarted = false;
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.ALL, npc.getNpcId(), "Oooo...ooo..."));
			npc.doCast(SkillTable.getInstance().getInfo(5705, 1));
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == GARGOS)
		{
			cancelQuestTimer("TimeToFire", npc, player);
		}
		return null;
	}
}