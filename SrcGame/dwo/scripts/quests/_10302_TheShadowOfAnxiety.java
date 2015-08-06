package dwo.scripts.quests;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.02.12
 * Time: 1:47
 */

public class _10302_TheShadowOfAnxiety extends Quest
{
	private static final int _kantarubis = 32898;
	private static final int _izshael = 32894;
	private static final int _kes = 32901;
	private static final int _masterKei = 32903;
	private static final int _kotKik = 32902;

	public _10302_TheShadowOfAnxiety()
	{
		addStartNpc(_kantarubis);
		addTalkId(_kantarubis, _izshael, _kes, _masterKei, _kotKik);
		addEventId(HookType.ON_ENTER_WORLD);
	}

	public static void main(String[] args)
	{
		new _10302_TheShadowOfAnxiety();
	}

	@Override
	public int getQuestId()
	{
		return 10302;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "kantarubis_merchant_q10302_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case _kantarubis:
				if(reply == 1)
				{
					return "kantarubis_merchant_q10302_04.htm";
				}
				if(reply == 2)
				{
					return "kantarubis_merchant_q10302_08.htm";
				}
				if(reply == 3 && st.getCond() == 6)
				{
					st.showQuestionMark(10304);
					st.playSound(QuestSound.ITEMSOUND_QUEST_TUTORIAL);
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(530400), ExShowScreenMessage.TOP_CENTER, 2000));
					st.addExpAndSp(6728850, 755280);
					st.giveAdena(2177190, true);
					st.giveItem(34033); // Ветхий свиток
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
					return "kantarubis_merchant_q10302_09.htm";
				}
				break;
			case _izshael:
				if(reply == 1 && st.getCond() == 1)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "izshael_q10302_02.htm";
				}
				if(reply == 2)
				{
					return "izshael_q10302_05.htm";
				}
				if(reply == 3 && st.getCond() == 5)
				{
					st.setCond(6);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "izshael_q10302_06.htm";
				}
				break;
			case _kes:
				if(reply == 1 && st.getCond() == 2)
				{
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "cas_cat_q10302_02.htm";
				}
				break;
			case _masterKei:
				if(reply == 1 && st.getCond() == 3)
				{
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "mrk_cat_q10302_02.htm";
				}
				break;
			case _kotKik:
				if(reply == 1 && st.getCond() == 4)
				{
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "kittkat_cat_q10302_02.htm";
				}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == _kantarubis)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "kantarubis_merchant_q10302_02.htm";
				case CREATED:
					if(player.getLevel() >= 90)
					{
						QuestState prevst = player.getQuestState(_10301_TheShadowOfFear.class);
						if(prevst != null && prevst.isCompleted())
						{
							return "kantarubis_merchant_q10302_01.htm";
						}
						else
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "kantarubis_merchant_q10302_03.htm";
						}
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "kantarubis_merchant_q10302_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "kantarubis_merchant_q10302_06.htm";
					}
					if(st.getCond() == 6)
					{
						return "kantarubis_merchant_q10302_07.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == _izshael)
		{
			if(st.isStarted())
			{
				switch(st.getCond())
				{
					case 1:
						return "izshael_q10302_01.htm";
					case 2:
						return "izshael_q10302_03.htm";
					case 5:
						return "izshael_q10302_04.htm";
					case 6:
						return "izshael_q10302_07.htm";
				}
			}
		}
		else if(npc.getNpcId() == _kes)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 2)
				{
					return "cas_cat_q10302_01.htm";
				}
				else if(st.getCond() == 3)
				{
					return "cas_cat_q10302_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == _masterKei)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 3)
				{
					return "mrk_cat_q10302_01.htm";
				}
				else if(st.getCond() == 4)
				{
					return "mrk_cat_q10302_03.htm";
				}
			}
		}
		else if(npc.getNpcId() == _kotKik)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 4)
				{
					return "kittkat_cat_q10302_01.htm";
				}
				else if(st.getCond() == 5)
				{
					return "kittkat_cat_q10302_03.htm";
				}
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState nextSt = player.getQuestState(_10304_ForForgottenHeroes.class);
		if(st != null && st.isCompleted() && (nextSt == null || !nextSt.isStarted() || !nextSt.isCompleted()) && !player.isSubClassActive() && player.getItemsCount(34033) == 0)
		{
			st.showQuestionMark(10304);
			st.playSound(QuestSound.ITEMSOUND_QUEST_TUTORIAL);
			player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(530400), ExShowScreenMessage.TOP_CENTER, 2000));
			st.giveItem(34033);
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10301_TheShadowOfFear.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}
}