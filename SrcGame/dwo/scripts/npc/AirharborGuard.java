package dwo.scripts.npc;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.04.13
 * Time: 15:31
 */
public class AirharborGuard extends Quest
{
	// NPCs
	private static final int ZEALOT = 18782;
	private static final int[] GUARDS = {
		32628, 32629
	};

	private AirharborGuard()
	{
		addSpawnId(ZEALOT);
		addFirstTalkId(GUARDS);

		for(int npcId : GUARDS)
		{
			for(L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
			{
				L2Npc guard = spawn.getLastSpawn();
				guard.setIsInvul(true);
				((L2Attackable) guard).setCanReturnToSpawnPoint(false);
				startQuestTimer("WATCHING", 10000, guard, null, true);
			}
		}
		for(L2Spawn spawn : SpawnTable.getInstance().getSpawns(ZEALOT))
		{
			spawn.getLastSpawn().setIsNoRndWalk(true);
		}
	}

	public static void main(String[] args)
	{
		new AirharborGuard();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc instanceof L2GuardInstance)
		{
			L2GuardInstance guard = (L2GuardInstance) npc;
			if(event.equalsIgnoreCase("WATCHING") && !guard.isAttackingNow())
			{
				guard.getKnownList().getKnownCharacters().stream().filter(L2Object::isMonster).forEach(character -> {
					L2MonsterInstance zealot = (L2MonsterInstance) character;
					if(zealot.getNpcId() == ZEALOT && !zealot.isDead() && !zealot.isDecayed())
					{
						guard.attackCharacter(zealot);
					}
				});
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.isAttackingNow() ? "guard_of_airharbor2002.htm" : "guard_of_airharbor2001.htm";
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
}