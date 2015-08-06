package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

//@author ANZO
//FinalForm Team

public class _00310_OnlyWhatRemains extends Quest
{
	private static final int kitajin = 32640;

	private static final int[] mobs = {
		22617, 22618, 22619, 22620, 22621, 22622, 22623, 22624, 22625, 22626, 22627, 22628, 22629, 22630, 22631, 22632,
		22633
	};

	private static final int grow_acc = 14832;
	private static final int mc_jewel = 14835;
	private static final int dirty_bead = 14880;

	public _00310_OnlyWhatRemains()
	{
		addStartNpc(kitajin);
		addTalkId(kitajin);
		addKillId(mobs);
	}

	public static void main(String[] args)
	{
		new _00310_OnlyWhatRemains();
	}

	@Override
	public int getQuestId()
	{
		return 310;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		if(event.equals("32640-04.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("32640-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(QuestType.REPEATABLE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if(partyMember == null)
		{
			return super.onKill(npc, player, isPet);
		}
		QuestState st = partyMember.getQuestState(getClass());
		if(ArrayUtils.contains(mobs, npc.getNpcId()))
		{
			st.dropQuestItems(dirty_bead, -1, -1, 60, true);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		QuestState qs2 = player.getQuestState(_00240_ImtheOnlyOneYouCanTrust.class);
		if(qs2 != null && qs2.getState() == STARTED)
		{
			return "32640-00.htm";
		}
		if(st.getCond() == 0)
		{
			if(player.getLevel() >= 81)
			{
				return "32640-01.htm";
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "32640-00.htm";
			}
		}
		if(st.getCond() == 1)
		{
			long beads = st.getQuestItemsCount(dirty_bead);
			if(beads == 0)
			{
				return "32640-08.htm";
			}
			else if(beads < 500)
			{
				return "32640-09.htm";
			}
			else if(beads >= 500)
			{
				st.takeItems(dirty_bead, 500);
				st.giveItems(grow_acc, 1);
				st.giveItems(mc_jewel, 1);
				return "32640-10.htm";
			}
		}
		return null;
	}
}