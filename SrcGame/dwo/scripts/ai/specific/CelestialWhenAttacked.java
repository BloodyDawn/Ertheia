package dwo.scripts.ai.specific;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

import java.util.List;

public class CelestialWhenAttacked extends Quest
{
	private final SkillHolder CELES = new SkillHolder(5044, 3);

	public CelestialWhenAttacked()
	{

		for(int level = 21; level < 76; level++)
		{
			List<L2NpcTemplate> templates = NpcTable.getInstance().getAllOfLevel(level);
			if(templates != null && !templates.isEmpty())
			{
				templates.stream().filter(t -> t.getType().equals("L2Monster") && (t.getNpcId() < 22139 || t.getNpcId() > 21255)).forEach(t -> addAttackId(t.getNpcId()));
			}
		}
	}

	public static void main(String[] args)
	{
		new CelestialWhenAttacked();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		L2Playable att = isPet && !attacker.getPets().isEmpty() ? attacker.getPets().getFirst() : attacker;
		double distance = Util.calculateDistance(att, npc, true);
		// Все мобы накидывают на себя целестиал при атаке с ренджем > 150, если рендж < 150, то целестиал снимается
		if(Rnd.getChance(1) && distance > 150 && npc.getFirstEffect(CELES.getSkill()) == null)
		{
			npc.setTarget(npc);
			npc.doCast(CELES.getSkill());
		}
		else if(distance < 150)
		{
			L2Effect celestial = npc.getFirstEffect(CELES.getSkill());
			if(celestial != null)
			{
				celestial.exit();
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}
}