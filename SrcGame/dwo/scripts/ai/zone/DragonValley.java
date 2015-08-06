package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.type.L2ScriptZone;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.HashMap;
import java.util.Map;

public class DragonValley extends Quest
{
	private static final int ZONE_ID = 46000;

	private static final L2ScriptZone ZONE = (L2ScriptZone) ZoneManager.getInstance().getZoneById(ZONE_ID);

	private static final int[] DRAGON_VALLEY_MOBS = {
		22822, 22823, 22824, 22825, 22826, 22827, 22828, 22829, 22830, 22831, 22832, 22833, 22834, 22860, 22861, 22862,
		25718, 25719, 25720, 25721, 25722, 25723, 25724, 25730, 25731,
	};

	private static final int MORALE_BOOST = 6885;
	private static final int ENERGY_ABUNDANCE = 6883;

	private static final Map<ClassId, Double> weight = new HashMap<>();

	static
	{
		weight.put(ClassId.duelist, 0.2);
		weight.put(ClassId.dreadnought, 0.7);
		weight.put(ClassId.phoenixKnight, 0.5);
		weight.put(ClassId.hellKnight, 0.5);
		weight.put(ClassId.sagittarius, 0.3);
		weight.put(ClassId.adventurer, 0.4);
		weight.put(ClassId.archmage, 0.3);
		weight.put(ClassId.soultaker, 0.3);
		weight.put(ClassId.arcanaLord, 1.0);
		weight.put(ClassId.cardinal, -0.6);
		weight.put(ClassId.hierophant, 0.0);
		weight.put(ClassId.evaTemplar, 0.8);
		weight.put(ClassId.swordMuse, 0.5);
		weight.put(ClassId.windRider, 0.4);
		weight.put(ClassId.moonlightSentinel, 0.3);
		weight.put(ClassId.mysticMuse, 0.3);
		weight.put(ClassId.elementalMaster, 1.0);
		weight.put(ClassId.evaSaint, -0.6);
		weight.put(ClassId.shillienTemplar, 0.8);
		weight.put(ClassId.spectralDancer, 0.5);
		weight.put(ClassId.ghostHunter, 0.4);
		weight.put(ClassId.ghostSentinel, 0.3);
		weight.put(ClassId.stormScreamer, 0.3);
		weight.put(ClassId.spectralMaster, 1.0);
		weight.put(ClassId.shillienSaint, -0.6);
		weight.put(ClassId.titan, 0.3);
		weight.put(ClassId.dominator, 0.1);
		weight.put(ClassId.grandKhauatari, 0.2);
		weight.put(ClassId.doomcryer, 0.1);
		weight.put(ClassId.fortuneSeeker, 0.9);
		weight.put(ClassId.maestro, 0.7);
		weight.put(ClassId.doombringer, 0.2);
		weight.put(ClassId.trickster, 0.5);
		weight.put(ClassId.judicator, 0.1);
		weight.put(ClassId.maleSoulhound, 0.3);
		weight.put(ClassId.femaleSoulhound, 0.3);
	}

	public DragonValley()
	{
		addSpoilId(DRAGON_VALLEY_MOBS);
		ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new BuffTask(), 60000, 60000);
	}

	public static void main(String[] args)
	{
		new DragonValley();
	}

	@Override
	public String onSuccessSpoil(L2Attackable target, L2Character activeChar)
	{
		int rand = Rnd.get(20);
		switch(rand)
		{
			case 5:
			case 6:
				L2Skill abudance = SkillTable.getInstance().getInfo(ENERGY_ABUNDANCE, 1);
				if(abudance != null)
				{
					if(activeChar.isInParty())
					{
						activeChar.getParty().getMembers().stream().filter(member -> Util.checkIfInRange(1400, activeChar, member, false)).forEach(member -> abudance.getEffects(target, member));
					}
					else
					{
						abudance.getEffects(target, activeChar);
					}
				}
				break;
			case 9:
			case 14:
			case 19:
				if(activeChar.getActingPlayer() != null)
				{
					target.dropItem(activeChar.getActingPlayer(), Rnd.get(8603, 8605), 1);
				}
				break;
		}
		return super.onSuccessSpoil(target, activeChar);
	}

	private int getBuffLevel(L2PcInstance player)
	{
		if(player.getParty() == null)
		{
			return 0;
		}
		L2Party party = player.getParty();
		// Small party check
		if(party.getMemberCount() < 5)    // toCheck
		{
			return 0;
		}
		// Newbie party or Not in zone member check
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(ZONE_ID);

		for(L2PcInstance p : party.getMembers())
		{
			if(p.getLevel() < 80 || !zone.isCharacterInZone(p))
			{
				return 0;
			}
		}

		double points = 0;
		int count = party.getMemberCount();

		for(L2PcInstance p : party.getMembers())
		{
			if(p != null && weight.containsKey(p.getClassId()))
			{
				points += weight.get(p.getClassId());
			}
		}
		return (int) Math.max(0, Math.min(3, Math.round(points * getCoefficient(count))));  // Brutally custom
	}

	private double getCoefficient(int count)
	{
		double cf;
		switch(count)
		{
			case 4:
				cf = 0.7;
				break;
			case 5:
				cf = 0.75;
				break;
			case 6:
				cf = 0.8;
				break;
			case 7:
				cf = 0.85;
				break;
			case 8:
				cf = 0.9;
				break;
			case 9:
				cf = 0.95;
				break;
			default:
				cf = 1;
		}
		return cf;
	}

	private class BuffTask implements Runnable
	{
		public BuffTask()
		{
		}

		@Override
		public void run()
		{
			try
			{
				ZONE.getCharactersInside().stream().filter(cha -> cha != null && cha instanceof L2PcInstance && !cha.isDead()).forEach(cha -> {
					int SkillLevel = getBuffLevel((L2PcInstance) cha);
					if(SkillLevel > 0)
					{
						L2Skill sk = SkillTable.getInstance().getInfo(MORALE_BOOST, SkillLevel);
						if(sk != null)
						{
							sk.getEffects(cha, cha);
						}
					}
				});
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception in buff task: " + e.getMessage(), e);
			}
		}
	}
}
