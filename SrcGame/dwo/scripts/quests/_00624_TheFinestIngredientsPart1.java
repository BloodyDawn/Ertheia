package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GO Team
 * User: ANZO
 * Date: 26.12.12
 * Time: 4:56
 */

public class _00624_TheFinestIngredientsPart1 extends Quest
{
	// Квестовые персонажи
	private static int JEREMY = 31521;

	// Квестовые монстры
	private static int HOT_SPRINGS_ATROX = 21321;
	private static int HOT_SPRINGS_NEPENTHES = 21319;
	private static int HOT_SPRINGS_ATROXSPAWN = 21317;
	private static int HOT_SPRINGS_BANDERSNATCHLING = 21314;

	// Квестовые предметы
	private static int SECRET_SPICE = 7204;
	private static int TRUNK_OF_NEPENTHES = 7202;
	private static int FOOT_OF_BANDERSNATCHLING = 7203;
	private static int CRYOLITE = 7080;
	private static int SAUCE = 7205;

	public _00624_TheFinestIngredientsPart1()
	{
		addStartNpc(JEREMY);
		addKillId(HOT_SPRINGS_ATROX, HOT_SPRINGS_NEPENTHES, HOT_SPRINGS_ATROXSPAWN, HOT_SPRINGS_BANDERSNATCHLING);
		questItemIds = new int[]{TRUNK_OF_NEPENTHES, FOOT_OF_BANDERSNATCHLING, SECRET_SPICE};
	}

	public static void main(String[] args)
	{
		new _00624_TheFinestIngredientsPart1();
	}

	@Override
	public int getQuestId()
	{
		return 624;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			qs.startQuest();
			qs.setMemoState(11);
			return "jeremy_q0624_0104.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(reply == 3)
		{
			if(player.getItemsCount(SECRET_SPICE) >= 50 && player.getItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 && player.getItemsCount(TRUNK_OF_NEPENTHES) >= 50)
			{
				st.giveItem(SAUCE);
				st.giveItem(CRYOLITE);
				st.exitQuest(QuestType.REPEATABLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "jeremy_q0624_0201.htm";
			}
			else
			{
				return "jeremy_q0624_0202.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2Party party = st.getPlayer().getParty();
		if(party != null)
		{
			st = party.getRandomPartyMember().getQuestState(getClass());
		}

		if(st != null && st.isStarted() && st.getCond() == 1)
		{
			if(npc.getNpcId() == HOT_SPRINGS_NEPENTHES)
			{
				st.giveItem(TRUNK_OF_NEPENTHES);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(npc.getNpcId() == HOT_SPRINGS_BANDERSNATCHLING)
			{
				st.giveItem(FOOT_OF_BANDERSNATCHLING);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if(npc.getNpcId() == HOT_SPRINGS_ATROX || npc.getNpcId() == HOT_SPRINGS_ATROXSPAWN)
			{
				st.giveItem(SECRET_SPICE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}

			if(st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setMemoState(12);
				st.setCond(3);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == JEREMY)
		{
			switch(st.getState())
			{
				case CREATED:
					return player.getLevel() < 73 ? "jeremy_q0624_0103.htm" : "jeremy_q0624_0101.htm";
				case STARTED:
					if(st.getMemoState() == 11 || st.getMemoState() == 12)
					{
						return st.getMemoState() == 12 && player.getItemsCount(SECRET_SPICE) >= 50 && player.getItemsCount(TRUNK_OF_NEPENTHES) >= 50 && player.getItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 ? "jeremy_q0624_0105.htm" : "jeremy_q0624_0106.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 73;
	}
}