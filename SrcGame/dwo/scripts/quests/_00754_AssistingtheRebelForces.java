package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2-GodWorld Team
 * @author: ANZO
 * Date: 03.09.12
 * Time: 16:10
 */

public class _00754_AssistingtheRebelForces extends Quest
{
	// Quest Npc
	private static final int SIZRAK = 33669;

	// Quest Items
	private static final int REBEL_SUPPLY_BOX = 35549;
	private static final int MARK_OF_THE_RESISTANCE = 34909;

	// Quest Mobs
	private static final int KUNDA_GUARDIAN = 23224;
	private static final int KUNDA_BERSERKER = 23225;
	private static final int KUNDA_EXECUTOR = 23226;

	public _00754_AssistingtheRebelForces()
	{
		addStartNpc(SIZRAK);
		addTalkId(SIZRAK);
		addKillId(KUNDA_GUARDIAN, KUNDA_BERSERKER, KUNDA_EXECUTOR);
	}

	public static void main(String[] args)
	{
		new _00754_AssistingtheRebelForces();
	}

	@Override
	public int getQuestId()
	{
		return 754;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.set("_1", "0");
			qs.set("_2", "0");
			qs.set("_3", "0");
			return "sofa_sizraku_q0754_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == SIZRAK)
		{
			if(reply == 1)
			{
				return "sofa_sizraku_q0754_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			switch(npc.getNpcId())
			{
				case KUNDA_GUARDIAN:
					if(st.getInt("_1") < 5)
					{
						st.set("_1", String.valueOf(st.getInt("_1") + 1));
					}
					break;
				case KUNDA_BERSERKER:
					if(st.getInt("_2") < 5)
					{
						st.set("_2", String.valueOf(st.getInt("_2") + 1));
					}
					break;
				case KUNDA_EXECUTOR:
					if(st.getInt("_3") < 5)
					{
						st.set("_3", String.valueOf(st.getInt("_3") + 1));
					}
					break;
			}
			sendNpcLogList(st.getPlayer());
			if(st.getInt("_1") + st.getInt("_2") + st.getInt("_3") >= 15)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npcId == SIZRAK)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "sofa_sizraku_q0754_06.htm";
				case CREATED:
					return st.getPlayer().getLevel() < 97 ? "sofa_sizraku_q0754_05.htm" : "sofa_sizraku_q0754_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "sofa_sizraku_q0754_07.htm";
					}
					if(cond == 2)
					{
						st.unset("_1");
						st.unset("_2");
						st.unset("_3");
						st.giveItem(MARK_OF_THE_RESISTANCE);
						st.giveItem(REBEL_SUPPLY_BOX);
						st.addExpAndSp(570676680, 136962);
						st.exitQuest(QuestType.DAILY);
						return "sofa_sizraku_q0754_08.htm";
					}
					break;
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 97;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");
			int _3 = st.getInt("_3");
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023224, _1);
			moblist.put(1023225, _2);
			moblist.put(1023226, _3);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}