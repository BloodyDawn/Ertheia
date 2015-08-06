package dwo.scripts.ai.individual;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Splendor extends Quest
{
	private final TIntObjectHashMap<int[]> SplendorId = new TIntObjectHashMap<>();
	private boolean AlwaysSpawn;

	public Splendor(int id, String name, String descr)
	{
		super(id, name, descr);
		// NpcId, {NewNpcId, % for chance by shot, ModeSpawn}
		// ModeSpawn 1 => delete and spawn the news npc
		// ModeSpawn 2 => just add 1 spawn the news npc
		// if Quest_Drop = 5 => 25% by shot to change mob
		SplendorId.put(21521, new int[]{21522, 5, 1}); // Claw of Splendor
		SplendorId.put(21524, new int[]{21525, 5, 1}); // Blade of Splendor
		SplendorId.put(21527, new int[]{21528, 5, 1}); // Anger of Splendor
		SplendorId.put(21537, new int[]{21538, 5, 1}); // Fang of Splendor
		SplendorId.put(21539, new int[]{21540, 100, 2}); // Wailing of Splendor
		for(int i : SplendorId.keys())
		{
			addAttackId(i);
			addKillId(i);
		}
		// finally, don't forget to call the parent constructor to prepare the event triggering
		// mechanisms etc.
	}

	public static void main(String[] args)
	{
		new Splendor(-1, "Splendor", "ai");
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet, L2Skill skill)
	{
		int[] tmp = SplendorId.get(npc.getNpcId());
		if(Rnd.getChance(tmp[1] * Config.RATE_QUEST_DROP))
		{
			if(SplendorId.containsKey(npc.getNpcId()))
			{
				if(tmp[2] == 1)
				{
					npc.getLocationController().delete();
					L2Npc newNpc = addSpawn(tmp[0], npc);
					((L2Attackable) newNpc).addDamageHate(player, 0, 999);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
				else if(AlwaysSpawn)
				{
					return super.onAttack(npc, player, damage, isPet, skill);
				}
				else if(tmp[2] == 2)
				{
					AlwaysSpawn = true;
					L2Npc newNpc = addSpawn(tmp[0], npc);
					((L2Attackable) newNpc).addDamageHate(player, 0, 999);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet, skill);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int[] tmp = SplendorId.get(npc.getNpcId());
		if(SplendorId.containsKey(npc.getNpcId()))
		{
			if(tmp[2] == 2)
			{
				AlwaysSpawn = false;
			}
		}
		return super.onKill(npc, player, isPet);
	}
}