package dwo.scripts.ai.zone;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 06.11.12
 * Time: 20:48
 * Info: Не ржать гугл перевел печь как SeedOfHellfire... Аи сделал именно таким, мобы не возвращаются к печам если их очень далеко увести от них, мобы лишь постепенно уходят к своему
 * спавну (это на оффе у всех мобов), если моб приближается к печи он подойдет к печи и будет там тусить пока мобов снова не сагрить (я сделал чтобы мобы распределялись по печам рядом со своим спавном,
 * возвращая их на точку своего спавна). Модель поведения немного иная, но схожая с тем которую я наблюдал на оффе, чуть что поправить или доделать.
 */
public class SeedOfHellfire extends Quest
{
	private static final int ИнженерСофа = 23233;
	int[] oven = {
		// Seed Of Destruction
		19259, 19279, 19280, 19281
	};

	public SeedOfHellfire()
	{
		for(int npcId : oven)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				spawn.getLastSpawn().setIsNoRndWalk(true);
				spawn.getLastSpawn().setIsImmobilized(true);
				spawn.getLastSpawn().setIsNoAnimation(true);
				spawn.getLastSpawn().setIsNoAttackingBack(true);
			}
		}
		addSpawnId(oven);
		addAttackId(oven);
		addKillId(oven);
	}

	public static void main(String[] args)
	{
		new SeedOfHellfire();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		npc.getKnownList().getKnownCharactersInRadius(3000).stream().filter(mobs -> mobs.isMonster() && attacker != null).forEach(mobs -> {
			mobs.setTarget(attacker);
			mobs.setRunning();
			((L2Attackable) mobs).addDamageHate(attacker, 0, 999);
			mobs.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		});
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc == null)
		{
			return null;
		}

		// После спавна мобы который стоят рядом с печами пускай идут к ним
		if(event.equalsIgnoreCase("gotooven"))
		{
			//System.out.println("mobs: " + mobs.getName());
			//System.out.println("npc: " + npc.getName());
			npc.getKnownList().getKnownCharactersInRadius(3000).stream().filter(mobs -> mobs.isMonster() && !mobs.equals(player) && (mobs.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK || mobs.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)).forEach(mobs -> {
				//System.out.println("mobs: " + mobs.getName());
				//System.out.println("npc: " + npc.getName());
				mobs.setTarget(npc);
				mobs.setRunning();
				mobs.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(npc.getX() + Rnd.get(400), npc.getY() + Rnd.get(400), npc.getZ()));
			});

			startQuestTimer("gotooven", 200000, npc, null);
			startQuestTimer("returntospawn", 100000, npc, null);
		}
		else if(event.equalsIgnoreCase("returntospawn"))
		{
			// Если мобы собрались не у своей печи пускай возвращаются на точку своего спавна, оттуда они уже придут к ближайшей печи.
			// Печть дает команду рядом стоящему мобу "Вали к своей печи, ты тут не свой!!!!", если он вдруг прибежал погреться не к своей. :D
			npc.getKnownList().getKnownCharactersInRadius(3000).stream().filter(mobs -> mobs.isMonster() && !mobs.equals(player) && (mobs.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK || mobs.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST)).forEach(mobs -> {
				mobs.setRunning();
				mobs.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, mobs.getNpcInstance().getSpawn().getLoc());
			});

		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc == null)
		{
			return null;
		}

		if(ArrayUtils.contains(oven, npc.getNpcId()))
		{
			if(npc.isDead())
			{
				for(int i = 0; i < Rnd.get(8); i++)
				{
					L2Character attacker = isPet ? killer.getPets().getFirst() : killer;
					L2Attackable is = (L2Attackable) addSpawn(ИнженерСофа, npc.getX() + 50, npc.getY() + 50, npc.getZ() + 10, npc.getHeading(), false, 0, false);

					is.attackCharacter(attacker);
				}
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(ArrayUtils.contains(oven, npc.getNpcId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
			npc.setIsNoAnimation(true);
			npc.setIsNoAttackingBack(true);

			startQuestTimer("gotooven", 3000, npc, null);
		}
		return super.onSpawn(npc);
	}
}