package dwo.scripts.ai.zone;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;

/**
 ** @author Gnacik
 */

public class PlainsOfLizardman extends Quest
{
	private static final int[] _MOBS = {18864, 18865, 18866, 18868};

	private static final int FANTASY_MUSHROOM = 18864;
	private static final int FANTASY_MUSHROOM_SKILL = 6427;

	private static final int RAINBOW_FROG = 18866;
	private static final int RAINBOW_FROG_SKILL = 6429;

	private static final int STICKY_MUSHROOM = 18865;
	private static final int STICKY_MUSHROOM_SKILL = 6428;

	private static final int ENERGY_PLANT = 18868;
	private static final int ENERGY_PLANT_SKILL = 6430;

	public PlainsOfLizardman()
	{
		addAttackId(_MOBS);
	}

	public static void main(String[] args)
	{
		new PlainsOfLizardman();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.isDead())
		{
			return null;
		}

		if(npc.getNpcId() == RAINBOW_FROG)
		{
			if(isPet)
			{
				startQuestTimer("rainbow_frog_pet", 2000, npc, attacker);
			}
			else
			{
				startQuestTimer("rainbow_frog", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		}
		else if(npc.getNpcId() == STICKY_MUSHROOM)
		{
			if(isPet)
			{
				startQuestTimer("sticky_mushroom_pet", 2000, npc, attacker);
			}
			else
			{
				startQuestTimer("sticky_mushroom", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		}
		else if(npc.getNpcId() == ENERGY_PLANT)
		{
			if(isPet)
			{
				startQuestTimer("energy_plant_pet", 2000, npc, attacker);
			}
			else
			{
				startQuestTimer("energy_plant", 2000, npc, attacker);
			}
			npc.doDie(attacker);
		}
		else if(npc.getNpcId() == FANTASY_MUSHROOM)
		{
			npc.getKnownList().getKnownCharactersInRadius(1000).stream().filter(target -> target != null && target instanceof L2Attackable && target.getAI() != null).forEach(target -> {
				target.setIsRunning(true);
				target.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX(), npc.getY(), npc.getZ(), 0));
			});
			if(isPet)
			{
				startQuestTimer("fantasy_mushroom_pet", 3000, npc, attacker);
			}
			else
			{
				startQuestTimer("fantasy_mushroom", 3000, npc, attacker);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(player != null && !player.isAlikeDead())
		{
			boolean isPet = false;
			if(event.endsWith("_pet") && !player.getPets().isEmpty())
			{
				isPet = true;
			}

			if(event.startsWith("rainbow_frog"))
			{
				triggerSkill(npc, isPet ? player.getPets().getFirst() : player, RAINBOW_FROG_SKILL, 1);
			}
			else if(event.startsWith("energy_plant"))
			{
				triggerSkill(npc, isPet ? player.getPets().getFirst() : player, ENERGY_PLANT_SKILL, 1);
			}
			else if(event.startsWith("sticky_mushroom"))
			{
				triggerSkill(npc, isPet ? player.getPets().getFirst() : player, STICKY_MUSHROOM_SKILL, 1);
			}
			else if(event.startsWith("fantasy_mushroom"))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(FANTASY_MUSHROOM_SKILL, 1);
				npc.doCast(skill);
				for(L2Character target : npc.getKnownList().getKnownCharactersInRadius(200))
				{
					if(target != null && target instanceof L2Attackable && target.getAI() != null)
					{
						skill.getEffects(npc, target);
						attackPlayer((L2Attackable) target, isPet ? player.getPets().getFirst() : player);
					}
				}
				npc.doDie(player);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	private void triggerSkill(L2Character caster, L2Playable playable, int skill_id, int skill_level)
	{
		L2Character[] targets = new L2Character[1];
		targets[0] = playable;

		L2Skill trigger = SkillTable.getInstance().getInfo(skill_id, skill_level);

		if(trigger != null && playable.isInsideRadius(caster, trigger.getCastRange(), true, false) && playable.getInstanceId() == caster.getInstanceId())
		{
			playable.broadcastPacket(new MagicSkillUse(playable, playable, skill_id, skill_level, 0, 0));

			ISkillHandler handler = SkillHandler.getInstance().getHandler(trigger.getSkillType());
			if(handler != null)
			{
				handler.useSkill(playable, trigger, targets);
			}
			else
			{
				trigger.useSkill(playable, targets);
			}
		}
	}

	private void attackPlayer(L2Attackable npc, L2Playable playable)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}
}