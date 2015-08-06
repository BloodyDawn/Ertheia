package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Hot Spring Disease AI
 *
 * @author devO
 */
public class HotSpringDisease extends Quest
{
	static final int[] disease1mobs = {
		21314, 21316, 21317, 21319, 21321, 21322
	}; // Monsters which cast Hot Spring Malaria (4554)
	static final int[] disease2mobs = {21317, 21322}; // Monsters which cast Hot Springs Flu (4553)
	static final int[] disease3mobs = {21316, 21319}; // Monsters which cast Hot Springs Cholera (4552)
	static final int[] disease4mobs = {21314, 21321}; // Monsters which cast Hot Springs Rheumatism (4551)

	// Chance to get infected by disease
	private static final int DISEASE_CHANCE = 5;

	public HotSpringDisease()
	{
		addAttackId(disease1mobs);
		addAttackId(disease2mobs);
		addAttackId(disease3mobs);
		addAttackId(disease4mobs);
	}

	public static void main(String[] args)
	{
		new HotSpringDisease();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(ArrayUtils.contains(disease1mobs, npc.getNpcId()))
		{
			if(Rnd.getChance(DISEASE_CHANCE))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4554, Rnd.get(10) + 1));
			}
		}
		if(ArrayUtils.contains(disease2mobs, npc.getNpcId()))
		{
			if(Rnd.getChance(DISEASE_CHANCE))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4553, Rnd.get(10) + 1));
			}
		}
		if(ArrayUtils.contains(disease3mobs, npc.getNpcId()))
		{
			if(Rnd.getChance(DISEASE_CHANCE))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4552, Rnd.get(10) + 1));
			}
		}
		if(ArrayUtils.contains(disease4mobs, npc.getNpcId()))
		{
			if(Rnd.getChance(DISEASE_CHANCE))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4551, Rnd.get(10) + 1));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
}