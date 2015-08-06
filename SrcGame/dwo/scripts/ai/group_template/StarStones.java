package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class StarStones extends Quest
{
	private static final int[] MOBs = {18684, 18685, 18686, 18687, 18688, 18689, 18690, 18691, 18692};
	private static final int RATE = 1;

	public StarStones()
	{
		addSkillSeeId(MOBs);
	}

	public static void main(String[] args)
	{
		new StarStones();
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(ArrayUtils.contains(targets, npc) && skill.getId() == 932)
		{
			int itemId;

			switch(npc.getNpcId())
			{
				case 18684:
				case 18685:
				case 18686:
					// give Red item
					itemId = 14009;
					break;
				case 18687:
				case 18688:
				case 18689:
					// give Blue item
					itemId = 14010;
					break;
				case 18690:
				case 18691:
				case 18692:
					// give Green item
					itemId = 14011;
					break;
				default:
					// unknown npc!
					return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			if(Rnd.getChance(33))
			{
				caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
				caster.addItem(ProcessType.SKILL, itemId, Rnd.get(RATE + 1, 2 * RATE), null, true);
			}
			else if(skill.getLevel() == 1 && Rnd.getChance(15) ||
				skill.getLevel() == 2 && Rnd.getChance(50) ||
				skill.getLevel() == 3 && Rnd.getChance(75))
			{
				caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
				caster.addItem(ProcessType.SKILL, itemId, Rnd.get(1, RATE), null, true);
			}
			else
			{
				caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_COLLECTION_HAS_FAILED));
			}
			npc.getLocationController().delete();
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
}