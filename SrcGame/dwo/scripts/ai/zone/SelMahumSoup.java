package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 ** Only one route, needs to be finished (!)
 */

public class SelMahumSoup extends Quest
{
	private static final int ID_CHEF = 18908;
	private static final int ID_FIRE = 18927;
	private static final int ID_FEED = 18933;
	private static final int FRUIT_SKILL = 9075;
	private static final L2Skill DOT_SKILL = SkillTable.getInstance().getInfo(6688, 1);
	private static final int[] EATING_MOBS = {22786, 22787, 22788};
	private static final int[][] FIRE_SPAWNS = {
		{79099, 63698, -3622}, {80122, 65029, -3315}, {81809, 63908, -3589}
	};
	private static final int[][] CHEF_SPAWNS = {
		{77840, 63387, -3641, 3463}
	};
	private static final int[][] ROUTES = {
		{77840, 63387, -3641, 1}, {79031, 63652, -3626, 2}, {79517, 63807, -3612, 3}, {79361, 64435, -3487, 4},
		{79560, 65079, -3317, 5}, {80128, 64965, -3313, 6}, {81118, 64929, -3470, 7}, {81248, 64062, -3559, 8},
		{81771, 63918, -3590, 9}, {81030, 63549, -3631, 10}, {79573, 63411, -3671, 11}, {78653, 62795, -3672, 12},
		{78842, 62841, -3688, 0}
	};
	private static final String[] TEXT = {
		"Ями, ями, ями...", "Давайте есть !", "Хмммм..."
	};
	private static FastList<L2Npc> list_chef;

	public SelMahumSoup()
	{

		list_chef = new FastList<>();

		for(int[] sp : FIRE_SPAWNS)
		{
			L2Npc fire = addSpawn(ID_FIRE, sp[0], sp[1], sp[2], 0, false, 0);
			fire.setTargetable(false);
			fire.setIsInvul(true);

			L2Npc feed = addSpawn(ID_FEED, sp[0], sp[1], sp[2], 0, false, 0);
			feed.setTargetable(false);
			feed.setIsInvul(true);
		}

		for(int[] chef_spawn : CHEF_SPAWNS)
		{
			L2Npc chef = addSpawn(ID_CHEF, chef_spawn[0], chef_spawn[1], chef_spawn[2], chef_spawn[3], false, 0);
			list_chef.add(chef);
			//
			chef.setIsNoRndWalk(true);
			// For now set just normal respawn
			L2Spawn spawn = chef.getSpawn();
			spawn.getLastSpawn().setSpawn(spawn);
			spawn.setRespawnDelay(60);
			spawn.setAmount(1);
			spawn.startRespawn();
		}
		addAttackId(EATING_MOBS);
		addSkillSeeId(EATING_MOBS);
		addFactionCallId(EATING_MOBS);

		addFirstTalkId(ID_FEED);
		addSkillSeeId(ID_FEED);

		startQuestTimer("chef_move", 20000, null, null);
	}

	public static void main(String[] args)
	{
		new SelMahumSoup();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(ArrayUtils.contains(EATING_MOBS, npc.getNpcId()) && npc instanceof L2Attackable)
		{
			// If someone Attack sitting mob, we should unImobbilize him
			if(npc.isImmobilized())
			{
				standUp((L2Attackable) npc);
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("chef_move"))
		{
			for(L2Npc chef : list_chef)
			{
				if(chef == null || chef.getAI() == null)
				{
					continue;
				}

				if(chef.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				{
					chef.getKnownList().getKnownCharactersInRadius(100).stream().filter(cha -> cha instanceof L2Npc && ((L2Npc) cha).getNpcId() == ID_FIRE).forEach(cha -> giveSoup((L2Npc) cha));
					boolean send = false;
					for(int[] route : ROUTES)
					{
						int n = route[3];
						if(chef.isInsideRadius(route[0], route[1], 200, false))
						{
							send = true;
							chef.setIsRunning(false);
							chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(ROUTES[n][0], ROUTES[n][1], ROUTES[n][2], 0));
							if(chef.getSpawn() != null)
							{
								chef.getSpawn().setLocx(ROUTES[n][0]);
								chef.getSpawn().setLocy(ROUTES[n][1]);
								chef.getSpawn().setLocz(ROUTES[n][2]);
							}
						}
					}
					if(!send)
					{
						chef.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(chef.getSpawn().getLocx(), chef.getSpawn().getLocy(), chef.getSpawn().getLocz(), 0));
					}
				}
			}
			startQuestTimer("chef_move", 5000, null, null);
		}
		else if(event.equalsIgnoreCase("sit_and_eat") && npc != null)
		{
			npc.getKnownList().getKnownCharactersInRadius(500).stream().filter(cha -> cha instanceof L2Attackable && ArrayUtils.contains(EATING_MOBS, ((L2Attackable) cha).getNpcId())).forEach(cha -> {
				((L2Attackable) cha).setDisplayEffect(1);
				((L2Attackable) cha).setRHandId(15280);
				cha.setIsImmobilized(true);
				cha.getAI().stopFollow();
			});
		}
		else if(event.equalsIgnoreCase("turn_off_fire") && npc != null)
		{
			// Turn off fire
			npc.setDisplayEffect(2);
			// Release eating mobs
			// Eating mobs are immobilized, so display standup only
			// for that mobs
			npc.getKnownList().getKnownCharactersInRadius(1000).stream().filter(cha -> cha instanceof L2Attackable && ArrayUtils.contains(EATING_MOBS, ((L2Attackable) cha).getNpcId())).forEach(cha -> {
				// Eating mobs are immobilized, so display standup only
				// for that mobs
				if(cha.isImmobilized())
				{
					standUp((L2Attackable) cha);
					cha.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(cha.getX() + Rnd.get(-300, 300), cha.getY() + Rnd.get(-300, 300), cha.getZ() - 20, 0));
				}
			});
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(ArrayUtils.contains(targets, npc) && ArrayUtils.contains(EATING_MOBS, npc.getNpcId()) && npc instanceof L2Attackable)
		{
			// If someone Attack sitting mob, we should unImobbilize him
			if(npc.isImmobilized())
			{
				standUp((L2Attackable) npc);
			}
		}
		else if(ArrayUtils.contains(targets, npc) && npc.getNpcId() == ID_FEED && skill.getId() == FRUIT_SKILL)
		{
			List<L2Character> targetList = new FastList<>();

			for(L2Character cha : npc.getKnownList().getKnownCharactersInRadius(350))
			{
				if(cha instanceof L2Attackable)
				{
					if(((L2Attackable) cha).getNpcId() == ID_FEED || ((L2Attackable) cha).getNpcId() == ID_FIRE)
					{
						continue;
					}

					// Apply effects
					DOT_SKILL.getEffects(npc, cha);

					// Add to target list
					targetList.add(cha);
				}
			}

			// Broadcast skill usage
			if(!targetList.isEmpty())
			{
				L2Character firstTarget = (L2Character) targets[0];
				L2Object[] affected = targetList.toArray(new L2Character[targetList.size()]);

				npc.broadcastPacket(new MagicSkillUse(npc, firstTarget, 6688, 1, 100, 0));
				npc.broadcastPacket(new MagicSkillLaunched(npc, 6688, 1, affected));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		if(ArrayUtils.contains(EATING_MOBS, npc.getNpcId()) && npc instanceof L2Attackable)
		{
			// If someone Attack mob, and he call other, mob we should unImobbilize him
			if(npc.isImmobilized())
			{
				standUp((L2Attackable) npc);
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}

	private void giveSoup(L2Npc npc)
	{
		// Set Fire
		npc.setDisplayEffect(1);

		// Spawn feed
		L2Npc feed = addSpawn(ID_FEED, npc.getX(), npc.getY(), npc.getZ(), 0, false, 12000);
		feed.setTargetable(true);

		npc.getKnownList().getKnownCharactersInRadius(500).stream().filter(cha -> cha instanceof L2Attackable && ArrayUtils.contains(EATING_MOBS, ((L2Attackable) cha).getNpcId())).forEach(cha -> {
			if(cha.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			{
				if(Rnd.get(5) == 1)
				{
					cha.broadcastPacket(new NS(cha.getObjectId(), ChatType.ALL, ((L2Npc) cha).getNpcId(), TEXT[Rnd.get(0, TEXT.length - 1)]));
				}

				cha.setIsRunning(true);
				cha.getAI().startFollow(npc, 200);
			}
		});
		startQuestTimer("sit_and_eat", 1000, npc, null);
		startQuestTimer("turn_off_fire", 12000, npc, null);
	}

	private void standUp(L2Attackable mob)
	{
		mob.setIsImmobilized(false);
		mob.setIsRunning(false);
		mob.setRHandId(15281);
		mob.setDisplayEffect(3);
	}
}