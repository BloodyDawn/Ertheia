package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.01.13
 * Time: 23:46
 */

public class IopRaceGuards extends Quest
{
	private static final int stamp_monster = 18367;
	private static final int outrance_guard = 18368;

	private static final SkillHolder TimerBuff = new SkillHolder(5239, 5);
	private static final SkillHolder PetrificationDebuff = new SkillHolder(4578, 1);
	private static final SkillHolder SilenceDebuff = new SkillHolder(4098, 9);

	private static final int stamp_item = 10013;

	public IopRaceGuards()
	{
		addAttackId(stamp_monster, outrance_guard);
		addSkillSeeId(outrance_guard);
		addSpawnId(stamp_monster, outrance_guard);
	}

	public static void main(String[] args)
	{
		new IopRaceGuards();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		L2Character attacker = isPet ? player.getPets().getFirst() : player;
		if(npc.getNpcId() == stamp_monster)
		{
			if(attacker.getFirstEffect(TimerBuff.getSkillId()) != null)
			{
				if(Util.checkIfInRange(100, npc, attacker, true) && Rnd.getChance(10))
				{
					if(player.getItemsCount(stamp_item) == 0 && npc.getCustomInt() == 0)
					{
						player.addItem(ProcessType.NPC, stamp_item, 1, npc, true);
						npc.setCustomInt(1);
						startQuestTimer("2001", 30 * 60 * 1000, npc, player);
					}
					else if(player.getItemsCount(stamp_item) == 1 && npc.getCustomInt() == 0)
					{
						player.addItem(ProcessType.NPC, stamp_item, 1, npc, true);
						npc.setCustomInt(2);
						startQuestTimer("2001", 30 * 60 * 1000, npc, player);
					}
					else if(player.getItemsCount(stamp_item) == 2 && npc.getCustomInt() == 0)
					{
						player.addItem(ProcessType.NPC, stamp_item, 1, npc, true);
						npc.setCustomInt(3);
						startQuestTimer("2001", 30 * 60 * 1000, npc, player);
					}
					else if(player.getItemsCount(stamp_item) == 3 && npc.getCustomInt() == 0)
					{
						player.addItem(ProcessType.NPC, stamp_item, 1, npc, true);
						npc.setCustomInt(4);
						startQuestTimer("2001", 30 * 60 * 1000, npc, player);
					}
				}
			}
			else
			{
				npc.setTarget(attacker);
				npc.doCast(PetrificationDebuff.getSkill());
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.ITS_NOT_EASY_TO_OBTAIN));
			}
		}
		else if(npc.getNpcId() == outrance_guard)
		{
			if(attacker.getFirstEffect(TimerBuff.getSkillId()) == null)
			{
				npc.setTarget(attacker);
				npc.doCast(PetrificationDebuff.getSkill());
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.YOURE_OUT_OF_YOUR_MIND_COMING_HERE));
			}
		}

		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("2001"))
		{
			if(npc != null)
			{
				npc.setCustomInt(0);
			}
		}
		return null;
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		L2Character caster = isPet ? player.getPets().getFirst() : player;
		if(npc.getNpcId() == outrance_guard && Util.checkIfInRange(100, npc, caster, true))
		{
			npc.setTarget(caster);
			npc.doCast(SilenceDebuff.getSkill());
		}
		return super.onSkillSee(npc, player, skill, targets, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == stamp_monster)
		{
			npc.setIsMortal(false);
			npc.disableCoreAI(true);
			npc.setIsNoAttackingBack(true);
		}
		else if(npc.getNpcId() == outrance_guard)
		{
			npc.setIsNoRndWalk(true);
		}
		return super.onSpawn(npc);
	}
}