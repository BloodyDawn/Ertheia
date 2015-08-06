package dwo.scripts.ai.zone;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.stream.Collectors;

public class MonasteryOfSilence extends Quest
{
	static final int[] mobs1 = {22129, 22789, 22790, 22791, 22792, 22793};
	static final int[] mobs2 = {22134, 22135};
	static final NpcStringId[] text = {
		NpcStringId.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION,
		NpcStringId.S1_WHY_WOULD_YOU_CHOOSE_THE_PATH_OF_DARKNESS, NpcStringId.S1_HOW_DARE_YOU_DEFY_THE_WILL_OF_EINHASAD
	};

	public MonasteryOfSilence()
	{
		registerMobs(mobs1, QuestEventType.ON_AGGRO_RANGE_ENTER, QuestEventType.ON_SPAWN, QuestEventType.ON_SPELL_FINISHED);
		registerMobs(mobs2, QuestEventType.ON_SKILL_SEE);
	}

	public static void main(String[] args)
	{
		new MonasteryOfSilence();
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(ArrayUtils.contains(mobs2, npc.getNpcId()))
		{
			if(skill.getSkillType() == L2SkillType.AGGDAMAGE && targets.length != 0)
			{
				for(L2Object obj : targets)
				{
					if(obj.equals(npc))
					{
						npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), text[Rnd.get(2) + 1]).addStringParameter(caster.getName()));
						((L2Attackable) npc).addDamageHate(caster, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster);
						break;
					}
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(ArrayUtils.contains(mobs1, npc.getNpcId()) && skill.getId() == 4589)
		{
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(ArrayUtils.contains(mobs1, npc.getNpcId()))
		{
			List<L2Playable> result = WorldManager.getInstance().getVisiblePlayable(npc).stream().filter(pl -> pl instanceof L2PcInstance || pl instanceof L2PetInstance).collect(Collectors.toList());
			if(!result.isEmpty())
			{
				Object[] characters = result.toArray();
				for(Object obj : characters)
				{
					L2Playable target = (L2Playable) (obj instanceof L2PcInstance ? obj : ((L2Summon) obj).getOwner());
					if(target.getActiveWeaponInstance() != null && !npc.isInCombat() && npc.getTarget() == null)
					{
						npc.setTarget(target);
						npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), text[0]));
						switch(npc.getNpcId())
						{
							case 22789:
							case 22790:
							case 22791:
							case 22792:
							case 22793:
								L2Skill skill = SkillTable.getInstance().getInfo(4589, 8);
								npc.doCast(skill);
								break;
							default:
								npc.setIsRunning(true);
								((L2Attackable) npc).addDamageHate(target, 0, 999);
								npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								break;
						}
					}
				}
			}
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(ArrayUtils.contains(mobs1, npc.getNpcId()) && !npc.isInCombat() && npc.getTarget() == null)
		{
			if(player.getActiveWeaponInstance() != null)
			{
				npc.setTarget(player);
				npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, text[0]));
				switch(npc.getNpcId())
				{
					case 22789:
					case 22790:
					case 22791:
					case 22792:
					case 22793:
						L2Skill skill = SkillTable.getInstance().getInfo(4589, 8);
						npc.doCast(skill);
						break;
					default:
						npc.setIsRunning(true);
						((L2Attackable) npc).addDamageHate(player, 0, 999);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						break;
				}
			}
			else if(((L2Attackable) npc).getMostHated() == null)
			{
				return null;
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
}