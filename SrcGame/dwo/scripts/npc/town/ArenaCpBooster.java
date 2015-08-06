package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.09.12
 * Time: 9:03
 */

public class ArenaCpBooster extends Quest
{
	private static final int[] ArenaCpBoosters = {31225, 31226};
	private static final SkillHolder cpHeal = new SkillHolder(4380, 1);
	private static final SkillHolder hpHeal = new SkillHolder(6817, 1);
	private final int[][] _Buffs = {
		{ // Бафы дял воина
			6803, 6804, 6808, 6809, 6811, 6812
		}, { // Бафы для мага
		6804, 6805, 6806, 6807, 6812
	}
	};

	public ArenaCpBooster()
	{
		addAskId(ArenaCpBoosters, -1000);
		addAskId(ArenaCpBoosters, -1001);
		addAskId(ArenaCpBoosters, -1002);
	}

	public static void main(String[] args)
	{
		new ArenaCpBooster();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -1000:
				if(player.isInsideZone(L2Character.ZONE_PVP)) // Cannot be used while inside the pvp zone
				{
					return null;
				}
				if(!player.reduceAdena(ProcessType.NPC, 1000, player.getLastFolkNPC(), true))
				{
					return null;
				}

				npc.setTarget(player);
				npc.doCast(cpHeal.getSkill());
				return null;
			case -1001:
				if(player.isInsideZone(L2Character.ZONE_PVP)) // Cannot be used while inside the pvp zone
				{
					return null;
				}
				if(!player.reduceAdena(ProcessType.NPC, 1000, player.getLastFolkNPC(), true))
				{
					return null;
				}
				npc.setTarget(player);
				npc.doCast(hpHeal.getSkill());
				return null;
			case -1002:
				if(!player.reduceAdena(ProcessType.NPC, 2000, player.getLastFolkNPC(), true))
				{
					return null;
				}

				for(int skillId : _Buffs[player.isMageClass() ? 1 : 0])
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);

					if(skill != null)
					{
						npc.setTarget(player);
						npc.doCast(skill);
					}
				}
				return null;
		}
		return null;
	}
}
