package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;

public class _10363_RequestOfTheSeeker extends Quest
{
	private static final int NAGEL = 33450;
	private static final int[] CORPSE = {32962, 32963, 32964};
	private static final int CELIN = 33451;

	public _10363_RequestOfTheSeeker()
	{
		addStartNpc(NAGEL);
		addTalkId(NAGEL, CELIN);
		addSocialSeeId(CORPSE);
	}

	public static void main(String[] args)
	{
		new _10363_RequestOfTheSeeker();
	}

	@Override
	public int getQuestId()
	{
		return 10363;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "si_illusion_nazel_q10363_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == NAGEL)
		{
			if(reply == 1)
			{
				return "si_illusion_nazel_q10363_04.htm";
			}
			else if(reply == 2)
			{
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "si_illusion_nazel_q10363_08.htm";
			}
		}
		else if(npc.getNpcId() == CELIN)
		{
			if(reply == 1)
			{
				return "si_illusion_selin_q10363_02.htm";
			}
			if(reply == 2 && st.getCond() == 7)
			{
				st.giveAdena(48000, true);
				st.addExpAndSp(70200, 16);
				st.giveItems(43, 1);
				st.giveItems(1060, 100);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "si_illusion_selin_q10363_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == NAGEL)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "si_illusion_nazel_q10363_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_nazel_q10363_02.htm";
					}
				case STARTED:
					switch(st.getCond())
					{
						case 1:
							return "si_illusion_nazel_q10363_06.htm";
						case 6:
							return "si_illusion_nazel_q10363_07.htm";
						case 7:
							return "si_illusion_nazel_q10363_08.htm";
					}
					break;
				case COMPLETED:
					return "si_illusion_nazel_q10363_03.htm";
			}
		}
		else if(npc.getNpcId() == CELIN)
		{
			if(st.getCond() == 7)
			{
				return "si_illusion_selin_q10363_01.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_selin_q10363_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onSocialSee(L2Npc npc, L2PcInstance player, L2Object target, int socialId)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(target != null && target instanceof L2Npc && socialId == 13)
		{
			L2Npc t = (L2Npc) target;
			if(!t.isDead())
			{
				if(player.isInsideRadius(t, 80, true, false))
				{
					switch(st.getCond())
					{
						case 1:
							t.doDie(player);
							st.setCond(2);
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1036301), ExShowScreenMessage.MIDDLE_CENTER, 2000));
							break;
						case 2:
							t.doDie(player);
							st.setCond(3);
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1036302), ExShowScreenMessage.MIDDLE_CENTER, 2000));
							break;
						case 3:
							t.doDie(player);
							st.setCond(4);
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1036303), ExShowScreenMessage.MIDDLE_CENTER, 2000));
							break;
						case 4:
							t.doDie(player);
							st.setCond(5);
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1036304), ExShowScreenMessage.MIDDLE_CENTER, 2000));
							break;
						case 5:
							t.doDie(player);
							st.setCond(6);
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1036305), ExShowScreenMessage.MIDDLE_CENTER, 2000));
							break;
					}
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else if(st.getCond() < 6)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(1810363), ExShowScreenMessage.MIDDLE_CENTER, 2000));
				}
			}
		}
		else if(target instanceof L2Npc)
		{
			((L2Npc) target).doDie(player);
		}

		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10362_CertificationOfTheSeeker.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 12 && player.getLevel() <= 20;
	}
} 