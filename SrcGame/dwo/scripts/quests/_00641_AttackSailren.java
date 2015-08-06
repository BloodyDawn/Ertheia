package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00641_AttackSailren extends Quest
{
	// NPCs
	private static final int STATUE = 32109;

	// MOBs
	private static final int[] MOBS = {22199, 22196, 22197, 22198, 22218, 22223};
	// Quest Item
	private static final int GAZKH_FRAGMENT = 8782;
	private static final int GAZKH = 8784;

	// Chance
	private static final int CHANCE = 100;

	public _00641_AttackSailren()
	{

		addStartNpc(STATUE);
		addTalkId(STATUE);
		addKillId(MOBS);
		questItemIds = new int[]{GAZKH_FRAGMENT};
	}

	public static void main(String[] args)
	{
		new _00641_AttackSailren();
	}

	@Override
	public int getQuestId()
	{
		return 641;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		if(event.equalsIgnoreCase("32109-02.htm"))
		{
			htmltext = "32109-02.htm";
		}
		else if(event.equalsIgnoreCase("32109-03.htm"))
		{
			st.startQuest();
			htmltext = "32109-03.htm";
		}
		else if(event.equalsIgnoreCase("32109-04.htm"))
		{
			st.takeItems(GAZKH_FRAGMENT, 30);
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			htmltext = "32109-04.htm";
		}
		else if(event.equalsIgnoreCase("32109-05.htm"))
		{
			npc.broadcastPacket(new MagicSkillUse(npc, player, 5089, 1, 3000, 0));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
			sm.addSkillName(5089);
			player.sendPacket(sm);
			st.addExpAndSp(15000000, 16500000);
			st.giveItems(GAZKH, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.unset("cond");
			htmltext = "32109-05.htm";
			st.exitQuest(QuestType.REPEATABLE);
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}

		if(ArrayUtils.contains(MOBS, npc.getNpcId()) && Rnd.getChance(CHANCE) && st.getCond() == 1 && st.getQuestItemsCount(GAZKH_FRAGMENT) < 30)
		{
			st.giveItems(GAZKH_FRAGMENT, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		if(npc.getNpcId() == STATUE)
		{
			int cond = st.getCond();
			switch(cond)
			{
				case 0:
					// Check if player has completed the quest The Name of Evil 2
					QuestState first = player.getQuestState(_00126_TheNameOfEvil2.class);
					if(first != null && !first.isCompleted() || first == null)
					{
						return htmltext;
					}
					if(player.getLevel() < 77)
					{
						return getLowLevelMsg(77);
					}
					htmltext = st.isCompleted() && st.hasQuestItems(GAZKH) ? getAlreadyCompletedMsg(player, QuestType.ONE_TIME) : "32109-01.htm";
					break;
				case 1:
					if(st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30)
					{
						startQuestTimer("32109-04.htm", 0, npc, player);
						htmltext = "32109-04.htm";
					}
					else
					{
						htmltext = "<html><body> Возвращайтесь, когда соберете 30 Gazkh Fragments. </body></html>";
					}
					break;
				case 2:
					startQuestTimer("32109-05.htm", 0, npc, player);
					htmltext = "32109-05.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState first = player.getQuestState(_00126_TheNameOfEvil2.class);
		return first != null && first.isCompleted() && player.getLevel() >= 77;

	}
}