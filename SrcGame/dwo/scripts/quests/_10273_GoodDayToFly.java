package dwo.scripts.quests;

/**
 * @author ANZO
 * 08.04.2010
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;

public class _10273_GoodDayToFly extends Quest
{
	// НПЦшки
	private static final int LEKON = 32557;
	private static final int VULTURE_RIDER1 = 22614;
	private static final int VULTURE_RIDER2 = 22615;

	// Квестовые вещи
	private static final int MARK = 13856;

	public _10273_GoodDayToFly()
	{

		addStartNpc(LEKON);
		addTalkId(LEKON);
		addKillId(VULTURE_RIDER1, VULTURE_RIDER2);
		questItemIds = new int[]{MARK};
	}

	public static void main(String[] args)
	{
		new _10273_GoodDayToFly();
	}

	@Override
	public int getQuestId()
	{
		return 10273;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		switch(event)
		{
			case "quest_accept":
				if(!st.isStarted())
				{
					st.startQuest();
					return "engineer_recon_q10273_06.htm";
				}
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		switch(npc.getNpcId())
		{
			case LEKON:
				switch(reply)
				{
					case 1:
						return "engineer_recon_q10273_04.htm";
					case 2:
						return "engineer_recon_q10273_05.htm";
					case 3:
						return "engineer_recon_q10273_07.htm";
					case 4:
						if(player.getPets() == null || !player.getPets().isEmpty())
						{
							return "engineer_recon_q10273_09c.htm";
						}

						if(player.getTransformationId() != 0)
						{
							player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
							return null;
						}

						if(qs.getCond() == 1)
						{
							qs.set("transform", "1");
							SkillTable.getInstance().getInfo(5982, 1).getEffects(player, player);
							return "engineer_recon_q10273_09a.htm";
						}
						break;
					case 5:
						if(player.getPets() == null || !player.getPets().isEmpty())
						{
							return "engineer_recon_q10273_09c.htm";
						}

						if(player.getTransformationId() != 0)
						{
							player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
							return null;
						}

						if(qs.getCond() == 1)
						{
							qs.set("transform", "2");
							SkillTable.getInstance().getInfo(5983, 1).getEffects(player, player);
							return "engineer_recon_q10273_09b.htm";
						}
						break;
					case 6:
						return "engineer_recon_q10273_10.htm";
					case 7:
						if(qs.getCond() == 1)
						{
							if(qs.getInt("transform") == 1 || qs.getInt("transform") == 2)
							{
							}
						}
					case 8:
						return "engineer_recon_q10273_11b.htm";
				}
				break;
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null || st.getState() != STARTED)
		{
			return null;
		}

		int cond = st.getCond();
		long count = st.getQuestItemsCount(MARK);
		if(cond == 1 && count < 5)
		{
			st.giveItems(MARK, 1);
			if(count == 4)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestStateType id = st.getState();
		int transform = st.getInt("transform");

		if(id == COMPLETED)
		{
			return "engineer_recon_q10273_03.htm";
		}
		else if(id == CREATED)
		{
			return st.getPlayer().getLevel() < 75 ? "engineer_recon_q10273_02.htm" : "engineer_recon_q10273_01.htm";
		}
		else if(st.getQuestItemsCount(MARK) >= 5)
		{
			if(transform == 1)
			{
				st.giveItems(13553, 1);
			}
			else if(transform == 2)
			{
				st.giveItems(13554, 1);
			}

			st.takeItems(MARK, -1);
			st.giveItems(13857, 1);
			st.addExpAndSp(6660000, 7375000);
			st.exitQuest(QuestType.ONE_TIME);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			return "engineer_recon_q10273_12.htm";
		}
		else
		{
			return transform < 1 ? "engineer_recon_q10273_08.htm" : "engineer_recon_q10273_11.htm";
		}
	}
}