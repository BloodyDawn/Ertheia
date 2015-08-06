package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

public class _10334_WindmillHillStatusReport extends Quest
{
	// Нпц
	private static final int SHUNAIN = 33508;
	private static final int BATHIS = 30332;
	// Награды
	private static final int ATUBA_MACE = 190;
	private static final int ELVEN_LONG_SWORD = 2499;
	private static final int GLAIVE = 297;
	private static final int LIGHT_CROSSBOW = 280;
	private static final int SCALLOP_JAMADHR = 262;
	private static final int MITHRIL_DAGGER = 225;
	private static final int BONEBREAKER = 159;
	private static final int CLAYMORE = 70;

	public _10334_WindmillHillStatusReport()
	{
		addStartNpc(SHUNAIN);
		addTalkId(SHUNAIN, BATHIS);
	}

	public static void main(String[] args)
	{
		new _10334_WindmillHillStatusReport();
	}

	@Override
	public int getQuestId()
	{
		return 10334;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "gludio_shunine_q10334_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == SHUNAIN)
		{
			if(reply == 1)
			{
				return "gludio_shunine_q10334_04.htm";
			}
		}
		else if(npc.getNpcId() == BATHIS)
		{
			if(reply == 1)
			{
				return "captain_bathia_q10334_04.htm";
			}
			else if(reply == 2)
			{
				ClassId playerClass = player.getClassId();
				int reward;
				if(playerClass.isMage())
				{
					reward = ATUBA_MACE;
				}
				else
				{
					if(playerClass == ClassId.warrior)
					{
						reward = GLAIVE;
					}
					else if(playerClass == ClassId.palusKnight || playerClass == ClassId.elvenKnight || playerClass == ClassId.knight)
					{
						reward = ELVEN_LONG_SWORD;
					}
					else if(playerClass == ClassId.scavenger || playerClass == ClassId.artisan)
					{
						reward = BONEBREAKER;
					}
					else if(playerClass == ClassId.orcMonk)
					{
						reward = SCALLOP_JAMADHR;
					}
					else
					{
						reward = playerClass == ClassId.orcRaider ? CLAYMORE : MITHRIL_DAGGER;
					}
				}

				st.giveAdena(85000, true);
				st.giveItems(reward, 1);
				st.addExpAndSp(200000, 48);
				player.sendPacket(new ExShowScreenMessage(NpcStringId.WEAPONS_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 5000));
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "captain_bathia_q10334_05.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == SHUNAIN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "gludio_shunine_q10334_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "gludio_shunine_q10334_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "gludio_shunine_q10334_06.htm";
					}
					break;
				case COMPLETED:
					return "gludio_shunine_q10334_03.htm";
			}
		}
		else if(npc.getNpcId() == BATHIS)
		{
			if(st.getCond() == 1)
			{
				return "captain_bathia_q10334_03.htm";
			}
			else if(st.isCompleted())
			{
				return "captain_bathia_q10334_02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10333_DisappearedSakum.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 22 && player.getLevel() <= 40 && player.getClassId().level() == 1;
	}
} 