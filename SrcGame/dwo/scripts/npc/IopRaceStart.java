package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 03.01.13
 * Time: 23:29
 */

public class IopRaceStart extends Quest
{
	private static final int NPC = 32349;

	private static final int stamp_finish_item = 9694;
	private static final int stamp_item = 10013;

	private static final SkillHolder BUFF = new SkillHolder(5239, 5);

	public IopRaceStart()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -1005);
	}

	public static void main(String[] args)
	{
		new IopRaceStart();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("2001"))
		{
			npc.setCustomInt(0);
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1005)
		{
			if(reply == 1)
			{
				if(npc.getCustomInt() == 0)
				{
					// Снимаем старый баф
					player.stopSkillEffects(BUFF.getSkillId());
					if(!player.getPets().isEmpty())
					{
						for(L2Summon pet : player.getPets())
						{
							pet.stopSkillEffects(BUFF.getSkillId());
						}
					}

					// Обновляем баф
					BUFF.getSkill().getEffects(npc, player);
					if(!player.getPets().isEmpty())
					{
						for(L2Summon pet : player.getPets())
						{
							BUFF.getSkill().getEffects(npc, pet);
						}
					}

					npc.setCustomInt(1);
					startQuestTimer("2001", 30 * 60 * 1000, npc, player);
					if(player.getItemsCount(stamp_item) >= 1)
					{
						long i0 = player.getItemsCount(stamp_item);
						player.destroyItemByItemId(ProcessType.NPC, stamp_item, i0, npc, true);
					}
				}
			}
			else if(reply == 2)
			{
				if(player.getItemsCount(stamp_item) >= 4)
				{
					long i0 = player.getItemsCount(stamp_item);
					player.exchangeItemsById(ProcessType.NPC, npc, stamp_item, i0, stamp_finish_item, 3, true);
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getItemsCount(stamp_item) >= 4)
		{
			return "race_start001a.htm";
		}
		else
		{
			return npc.getCustomInt() == 0 && player.getLevel() >= 78 ? "race_start001.htm" : "race_start002.htm";
		}
	}
}