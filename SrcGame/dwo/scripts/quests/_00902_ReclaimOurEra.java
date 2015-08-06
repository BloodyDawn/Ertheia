package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.09.11
 * Time: 0:28
 */

public class _00902_ReclaimOurEra extends Quest
{
	private static final int Mathias = 31340;

	// Рейдбосы
	private static final int[] KetraVarkaMobs = {25299, 25302, 25305, 25309, 25312, 25315};
	private static final int StakatoCanibal = 25667;
	private static final int Anais = 25701;

	// Квестовые предметы
	private static final int ShatteredBones = 21997;
	private static final int CannibalisticClaw = 21998;
	private static final int AnaisScroll = 21999;
	private static final int BlueElmoreCoin = 21750;

	public _00902_ReclaimOurEra()
	{
		addStartNpc(Mathias);
		addTalkId(Mathias);
		addKillId(KetraVarkaMobs);
		addKillId(StakatoCanibal, Anais);
		questItemIds = new int[]{ShatteredBones, CannibalisticClaw, AnaisScroll};
	}

	public static void main(String[] args)
	{
		new _00902_ReclaimOurEra();
	}

	@Override
	public int getQuestId()
	{
		return 902;
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
			case "31340-02.htm":
				st.startQuest();
				break;
			case "31340-04.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32548-05.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32548-06.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(st.getState() == STARTED)
		{
			int npcId = npc.getNpcId();

			switch(st.getCond())
			{
				case 2:
					if(ArrayUtils.contains(KetraVarkaMobs, npcId))
					{
						st.giveItem(ShatteredBones);
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					break;
				case 3:
					if(npcId == StakatoCanibal)
					{
						st.giveItem(CannibalisticClaw);
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					break;
				case 4:
					if(npcId == Anais)
					{
						st.giveItem(AnaisScroll);
						st.setCond(5);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestStateType id = st.getState();
		int cond = st.getCond();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		switch(id)
		{
			case COMPLETED:
				return "31340-noday.htm";
			case CREATED:
				return player.getLevel() < 80 ? "31340-nolvl.htm" : "31340-00.htm";
			case STARTED:
				switch(cond)
				{
					case 1:
						return "31340-03.htm";
					case 2:
						return "31340-04.htm";
					case 3:
						return "31340-05.htm";
					case 4:
						return "31340-06.htm";
					case 5:
						if(st.hasQuestItems(ShatteredBones))
						{
							st.takeItems(ShatteredBones, -1);
							st.giveItem(BlueElmoreCoin);
							st.giveAdena(134038, true);
						}
						if(st.hasQuestItems(CannibalisticClaw))
						{
							st.takeItems(CannibalisticClaw, -1);
							st.giveItems(BlueElmoreCoin, 3);
							st.giveAdena(210119, true);
						}
						if(st.hasQuestItems(AnaisScroll))
						{
							st.takeItems(AnaisScroll, -1);
							st.giveItems(BlueElmoreCoin, 3);
							st.giveAdena(348155, true);
						}
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						// TODO: Верный диалог при награждении (возможно для каждого варианта РБ разный)
						return "31340-reward.htm";
				}
				break;
		}
		return null;
	}
}