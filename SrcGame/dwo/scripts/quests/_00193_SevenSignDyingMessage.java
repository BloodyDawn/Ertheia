package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Util;

/*
 * TODO:
 * - При появлении Shilens Evil Thoughts прист должен лечить игрока
 */

public class _00193_SevenSignDyingMessage extends Quest
{
	// NPCs
	private static final int Hollint = 30191;
	private static final int Cain = 32569;
	private static final int Eric = 32570;
	private static final int SirGustavAthebaldt = 30760;

	// MOBS
	private static final int ShilensEvilThoughts = 27402;

	// ITEMS
	private static final int JacobsNecklace = 13814;
	private static final int DeadmansHerb = 13816;
	private static final int SculptureOfDoubt = 14352;

	// State
	private static boolean SPAWNED;

	// Skill
	private static L2Skill CAINSKILL = SkillTable.getInstance().getInfo(4065, 8);

	public _00193_SevenSignDyingMessage()
	{
		addStartNpc(Hollint);
		addTalkId(Hollint, Cain, Eric, SirGustavAthebaldt, ShilensEvilThoughts);
		addKillId(ShilensEvilThoughts);
		questItemIds = new int[]{JacobsNecklace, DeadmansHerb, SculptureOfDoubt};
	}

	public static void main(String[] args)
	{
		new _00193_SevenSignDyingMessage();
	}

	@Override
	public int getQuestId()
	{
		return 193;
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
			case "30191-1.htm":
				st.giveItems(JacobsNecklace, 1);
				st.startQuest();
				break;
			case "32569-4.htm":
				st.takeItems(JacobsNecklace, -1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32570-1.htm":
				st.giveItems(DeadmansHerb, 1);
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32569-6.htm":
				st.takeItems(DeadmansHerb, -1);
				st.setCond(4);
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_DYING_MASSAGE);
				return null;
			case "32569-7.htm":
				if(SPAWNED)
				{
					return "32569-busy.htm";
				}
				SPAWNED = true;
				npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP).addStringParameter(player.getName()));
				L2Npc Evil = addSpawn(ShilensEvilThoughts, 82500, 47485, -3225, 53495, false, 300000, false, 0);
				Evil.broadcastPacket(new NS(Evil, ChatType.NPC_ALL, NpcStringId.YOU_ARE_NOT_THE_OWNER_OF_THAT_ITEM));
				((L2Attackable) Evil).addDamageHate(player, 0, 999);
				Evil.setRunning();
				Evil.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
				startQuestTimer("despawn", 60000, Evil, player);
				startQuestTimer("cainhelp", 6000, npc, player);
				break;
			case "32569-11.htm":
				st.takeItems(SculptureOfDoubt, 1);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "30760-1.htm":
				if(player.isSubClassActive())
				{
					return "subclass_forbidden.htm";
				}
				st.addExpAndSp(10000000, 2500000);
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
			case "despawn":
				SPAWNED = false;
				npc.getLocationController().delete();
				return null;
			case "cainhelp":
				if(npc != null && SPAWNED && Util.checkIfInRange(900, npc, player, false))
				{
					if(player.getCurrentHp() < player.getMaxHp())
					{
						npc.setTarget(player);
						npc.doCast(CAINSKILL);
						NS ns = new NS(npc, ChatType.NPC_ALL, NpcStringId.S1_THAT_STRANGER_MUST_BE_DEFEATED_HERE_IS_THE_ULTIMATE_HELP);
						ns.addStringParameter(player.getName());
						npc.broadcastPacket(ns);
					}
					startQuestTimer("cainhelp", 6000, npc, player);
				}
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null || st.getCond() != 4)
		{
			return null;
		}
		npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, NpcStringId.S1_YOU_MAY_HAVE_WON_THIS_TIME_BUT_NEXT_TIME_I_WILL_SURELY_CAPTURE_YOU).addStringParameter(player.getName()));
		L2Npc priest = SpawnTable.getInstance().getFirstSpawn(Cain).getLastSpawn();
		priest.broadcastPacket(new NS(priest, ChatType.NPC_ALL, NpcStringId.WELL_DONE_S1_YOUR_HELP_IS_MUCH_APPRECIATED).addStringParameter(player.getName()));
		if(!st.hasQuestItems(SculptureOfDoubt))
		{
			st.giveItems(SculptureOfDoubt, 1);
		}
		st.setCond(5);
		st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		QuestState st1 = player.getQuestState(_00192_SevenSignSeriesOfDoubt.class);
		String htmltext = getNoQuestMsg(player);

		if(st == null || player.getLevel() < 79)
		{
			return htmltext;
		}

		int cond = st.getCond();
		if(st.getState() == COMPLETED)
		{
			htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		else if(npc.getNpcId() == Hollint)
		{
			if(player.getLevel() >= 79 && st1 != null && st1.isCompleted())
			{
				if(cond == 0)
				{
					htmltext = "30191-0.htm";
				}
				else if(cond >= 1)
				{
					htmltext = "30191-1a.htm";
				}
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "30191-0a.htm";
			}
		}
		else if(npc.getNpcId() == Cain)
		{
			switch(cond)
			{
				case 1:
					htmltext = "32569-0.htm";
					break;
				case 2:
					htmltext = "32569-4a.htm";
					break;
				case 3:
					htmltext = "32569-5.htm";
					break;
				case 4:
					htmltext = "32569-6.htm";
					break;
				case 5:
					htmltext = "32569-8.htm";
					break;
			}
		}
		else if(npc.getNpcId() == Eric)
		{
			if(cond == 2)
			{
				htmltext = "32570-0.htm";
			}
			else if(cond >= 3)
			{
				htmltext = "32570-1a.htm";
			}
		}
		else if(npc.getNpcId() == SirGustavAthebaldt)
		{
			if(cond == 6)
			{
				htmltext = "30760-0.htm";
			}
		}
		return htmltext;
	}
}