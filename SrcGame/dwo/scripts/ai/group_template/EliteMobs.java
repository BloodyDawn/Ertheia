package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 14.03.12
 * Time: 0:47
 */

public class EliteMobs extends Quest
{
	private static final int[] _mobs = {
		// Гробница Императоров
		23191, // Кошмар Смерти
		23192, // Кошмар Тьмы
		23197, // Кошмар Безумия
		23198, // Кошмар Безмолвия
	};

	private static final List<SkillHolder> _buffs = new ArrayList<>();

	private static final Map<Integer, Integer[]> _onAttackSay = new HashMap<>();
	private static final Map<Integer, Integer[]> _onKillSay = new HashMap<>();

	public EliteMobs()
	{

		/* Бафы-бонусы */
		_buffs.add(new SkillHolder(14975, 1)); // Тайная Боевая Сила
		_buffs.add(new SkillHolder(14976, 1)); // Тайная Боевая Сила
		_buffs.add(new SkillHolder(14977, 1)); // Тайная Сила Расслабления

		/* Фразы нпц */
		// Кошмар Смерти
		_onAttackSay.put(23191, new Integer[]{1801244, 1801245, 1801246, 1801247, 1801248, 1801249});
		_onKillSay.put(23191, new Integer[]{1801250, 1801251, 1801252});
		// Кошмар Тьмы
		_onAttackSay.put(23192, new Integer[]{1801253, 1801254, 1801255, 1801256, 1801257, 1801258});
		_onKillSay.put(23192, new Integer[]{1801259, 1801260, 1801261});
		// Кошмар Безумия
		_onAttackSay.put(23197, new Integer[]{1801262, 1801263, 1801264, 1801265, 1801266, 1801267});
		_onKillSay.put(23197, new Integer[]{1801268, 1801269, 1801270});
		// Кошмар Безмолвия
		_onAttackSay.put(23198, new Integer[]{1801271, 1801272, 1801273, 1801274, 1801275, 1801276});
		_onKillSay.put(23198, new Integer[]{1801277, 1801278, 1801279, 1801280});

		addKillId(_mobs);
		addAttackId(_mobs);
	}

	public static void main(String[] args)
	{
		new EliteMobs();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(ArrayUtils.contains(_mobs, npc.getNpcId()) && Rnd.getChance(7))
		{
			Integer npcStringId = _onAttackSay.get(npc.getNpcId())[Rnd.get(_onAttackSay.get(npc.getNpcId()).length)];
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), npcStringId));
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(ArrayUtils.contains(_mobs, npc.getNpcId()))
		{
			for(SkillHolder skill : _buffs)
			{
				killer.broadcastPacket(new MagicSkillUse(killer, killer, skill.getSkillId(), 1, 0, 0));
				skill.getSkill().getEffects(killer, killer);
			}
			Integer npcStringId = _onKillSay.get(npc.getNpcId())[Rnd.get(_onKillSay.get(npc.getNpcId()).length)];
			npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), npcStringId));
		}
		return super.onKill(npc, killer, isPet);
	}
}