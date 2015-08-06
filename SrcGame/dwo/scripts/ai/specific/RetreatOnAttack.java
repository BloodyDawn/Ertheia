package dwo.scripts.ai.specific;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

public class RetreatOnAttack extends Quest
{
	private static final TIntObjectHashMap<int[]> spawns = new TIntObjectHashMap<>();

	static
	{
		spawns.put(20058, new int[]{50, 10});  // Ol Mahum Guard
		spawns.put(20207, new int[]{30, 1});   // Ol Mahum Guerilla
		spawns.put(20208, new int[]{50, 1});   // Ol Mahum Raider
		spawns.put(22126, new int[]{1, 100});  // Silent Seeker
		spawns.put(22127, new int[]{1, 100});  // Silent Brother
		spawns.put(21508, new int[]{30, 100}); // Splinter Stakat
		spawns.put(21509, new int[]{30, 100}); // Splinter Stakato Walker
		spawns.put(21510, new int[]{30, 100}); // Splinter Stakato Soldier
		spawns.put(21511, new int[]{30, 100}); // Splinter Stakato Drone
		spawns.put(21512, new int[]{30, 100}); // Splinter Stakato Drone
		spawns.put(21513, new int[]{30, 100}); // Needle Stakato
		spawns.put(21514, new int[]{30, 100}); // Needle Stakato Worker
		spawns.put(21515, new int[]{30, 100}); // Needle Stakato Soldier
		spawns.put(20158, new int[]{30, 1});   //  Medusa
		spawns.put(20497, new int[]{30, 80});  // Turek Orc Skirmisher
		spawns.put(20500, new int[]{30, 70});  // Turek Orc Sentinel
	}

	private static final TIntObjectHashMap<NpcStringId[]> texts = new TIntObjectHashMap<>();

	static
	{
		texts.put(20058, new NpcStringId[]{
			NpcStringId.ILL_BE_BACK, NpcStringId.YOU_ARE_STRONGER_THAN_I_THOUGHT_BUT_IM_NO_WEAKLING
		});
		texts.put(20207, new NpcStringId[]{
			NpcStringId.INVADER, NpcStringId.ILL_GIVE_YOU_TEN_MILLION_ARENA_IF_YOU_LET_ME_LIVE,
			NpcStringId.I_WILL_DEFINITELY_RECLAIM_MY_HONOR_WHICH_HAS_BEEN_TARNISHED,
			NpcStringId.THERE_IS_NO_REASON_FOR_YOU_TO_KILL_ME_I_HAVE_NOTHING_YOU_NEED, NpcStringId.TACTICAL_RETREAT,
			NpcStringId.SOMEDAY_YOU_WILL_PAY, NpcStringId.STOP_HITTING, NpcStringId.WE_SHALL_SEE_ABOUT_THAT,
			NpcStringId.ILL_KILL_YOU_NEXT_TIME, NpcStringId.ILL_DEFINITELY_KILL_YOU_NEXT_TIME
		});
		texts.put(20208, new NpcStringId[]{
			NpcStringId.RETREAT, NpcStringId.MASS_FLEEING, NpcStringId.SURRENDER, NpcStringId.ILL_BE_BACK,
			NpcStringId.OH_HOW_STRONG
		});
		texts.put(22126, new NpcStringId[]{
			NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION
		});
		texts.put(22127, new NpcStringId[]{
			NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION
		});
		texts.put(20158, new NpcStringId[]{
			NpcStringId.ITS_A_GOOD_DAY_TO_DIE_WELCOME_TO_HELL_MAGGOT, NpcStringId.SOMEDAY_YOU_WILL_PAY,
			NpcStringId.LOST_SORRY_LORD
		});
		texts.put(20497, new NpcStringId[]{
			NpcStringId.OH_HOW_STRONG, NpcStringId.WE_SHALL_SEE_ABOUT_THAT,
			NpcStringId.ILL_DEFINITELY_KILL_YOU_NEXT_TIME
		});
		texts.put(20500, new NpcStringId[]{
			NpcStringId.OH_HOW_STRONG, NpcStringId.WE_SHALL_SEE_ABOUT_THAT,
			NpcStringId.ILL_DEFINITELY_KILL_YOU_NEXT_TIME
		});
	}

	public RetreatOnAttack()
	{

		for(int id : spawns.keys())
		{
			addAttackId(id);
		}
	}

	public static void main(String[] args)
	{
		new RetreatOnAttack();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc == null)
		{
			return null;
		}

		double currentHp = npc.getCurrentHp();
		int maxHp = npc.getMaxHp();
		int npcId = npc.getNpcId();

		if(spawns.containsKey(npcId))
		{
			int[] tmp = spawns.get(npcId);
			if(currentHp <= maxHp * tmp[0] / 100 && Rnd.getChance(tmp[1]))
			{
				NpcStringId[] tmptxt = texts.get(npcId);

				if(texts.containsKey(npcId))
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), tmptxt[Rnd.get(tmptxt.length)]));
				}

				int posX = npc.getX();
				int posY = npc.getY();
				int posZ = npc.getZ();
				int signX = -500;
				int signY = -500;

				if(posX > attacker.getX())
				{
					signX = 500;
				}
				if(posY > attacker.getY())
				{
					signY = 500;
				}

				posX += signX;
				posY += signY;
				npc.startFear();
				npc.setRunning();
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ, 0));
				startQuestTimer("Retreat", 10000, npc, attacker);
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("Retreat"))
		{
			npc.stopFear(true);
			((L2Attackable) npc).addDamageHate(player, 0, 100);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return null;
	}
}