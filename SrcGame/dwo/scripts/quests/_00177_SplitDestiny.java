package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.player.base.SubClassType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2-GodWorld Team
 * @author ANZO, Yukio
 * Date: 10.05.13
 * Time: 17:25
 */

public class _00177_SplitDestiny extends Quest
{
	// NPCs
	private static final int HADEL = 33344;
	private static final int MAESTRO_ISHUMA = 32615;

	// Items
	private static final int PETRIFIED_GIANT_HAND_PIECE = 17720;
	private static final int PETRIFIED_GIANT_FOOT_PIECE = 17721;
	private static final int PETRIFIED_GIANT_HAND = 17718;
	private static final int PETRIFIED_GIANT_FOOT = 17719;

	// MOBs
	private static final int[] HAND_DROP_MOBS = {21549, 21547, 21548, 21582};
	private static final int[] FOOT_DROP_MOBS = {22257, 22258, 22259, 22260};

	// Quest reward items
	private static final int RECIPE_TWILIGHT_NECKLACE = 36791;

	public _00177_SplitDestiny()
	{
		addStartNpc(HADEL);
		addTalkId(HADEL, MAESTRO_ISHUMA);
		addKillId(HAND_DROP_MOBS);
		addKillId(FOOT_DROP_MOBS);
		questItemIds = new int[]{
			PETRIFIED_GIANT_HAND_PIECE, PETRIFIED_GIANT_FOOT_PIECE, PETRIFIED_GIANT_HAND, PETRIFIED_GIANT_FOOT
		};
	}

	public static void main(String[] args)
	{
		new _00177_SplitDestiny();
	}

	@Override
	public int getQuestId()
	{
		return 177;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && st.getPlayer().getLevel() >= 80 && !st.isCompleted())
		{
			st.startQuest();
			st.set("_177clId" + st.getPlayer().getClassIndex(), "true");
			return "hadel_q0177_14.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == HADEL)
		{
			if(reply == 2)
			{
				return "hadel_q0177_18.htm";
			}
			else if(reply == 3 && cond == 3)
			{
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "hadel_q0177_19.htm";
			}
			else if(reply == 4 && cond == 6)
			{
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "hadel_q0177_22.htm";
			}
			else if(reply == 5 && cond == 9)
			{
				st.takeItems(PETRIFIED_GIANT_HAND, -1);
				st.takeItems(PETRIFIED_GIANT_FOOT, -1);
				st.setCond(10);
				return "hadel_q0177_25.htm";
			}
			else if(reply == 6)
			{
				return "hadel_q0177_27.htm";
			}
			else if(reply == 7)
			{
				return "hadel_q0177_27a.htm";
			}
			else if(reply == 8 && cond == 10)
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.LEVEL_UP));
				player.getSubclass().setClassType(SubClassType.DUAL_CLASS);
				player.sendPacket(SystemMessage.getSystemMessage(3279).addClassId(player.getActiveClassId()).addClassId(player.getActiveClassId()));
				st.giveItem(10480);
				st.giveItem(RECIPE_TWILIGHT_NECKLACE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(175739575, 288630);
				st.exitQuest(QuestType.ONE_TIME);
				return "hadel_q0177_28.htm";
			}
			else if(reply == 9 && cond == 10)
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.LEVEL_UP));
				player.getSubclass().setClassType(SubClassType.DUAL_CLASS);
				player.sendPacket(SystemMessage.getSystemMessage(3279).addClassId(player.getActiveClassId()).addClassId(player.getActiveClassId()));
				st.giveItem(10481);
				st.giveItem(RECIPE_TWILIGHT_NECKLACE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(175739575, 288630);
				st.exitQuest(QuestType.ONE_TIME);
				return "hadel_q0177_28.htm";
			}
			else if(reply == 10 && cond == 10)
			{
				player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.LEVEL_UP));
				player.getSubclass().setClassType(SubClassType.DUAL_CLASS);
				player.sendPacket(SystemMessage.getSystemMessage(3279).addClassId(player.getActiveClassId()).addClassId(player.getActiveClassId()));
				st.giveItem(10482);
				st.giveItem(RECIPE_TWILIGHT_NECKLACE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(175739575, 288630);
				st.exitQuest(QuestType.ONE_TIME);
				return "hadel_q0177_28.htm";
			}
		}
		else if(npcId == MAESTRO_ISHUMA)
		{
			if(reply == 1)
			{
				return "maestro_ishuma_q0177_02.htm";
			}
			if(reply == 2 && cond == 7)
			{
				st.takeItems(PETRIFIED_GIANT_HAND_PIECE, -1);
				st.takeItems(PETRIFIED_GIANT_FOOT_PIECE, -1);
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "maestro_ishuma_q0177_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(!killer.isSubClassActive())
		{
			return null;
		}

		switch(st.getCond())
		{
			case 1:
				if(ArrayUtils.contains(HAND_DROP_MOBS, npc.getNpcId()))
				{
					st.giveItem(PETRIFIED_GIANT_HAND_PIECE);
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case 2:
				if(ArrayUtils.contains(HAND_DROP_MOBS, npc.getNpcId()) && Rnd.getChance(60))
				{
					st.giveItem(PETRIFIED_GIANT_HAND_PIECE);
					if(st.getQuestItemsCount(PETRIFIED_GIANT_HAND_PIECE) >= 10)
					{
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
				break;
			case 4:
				if(ArrayUtils.contains(FOOT_DROP_MOBS, npc.getNpcId()))
				{
					st.giveItem(PETRIFIED_GIANT_FOOT_PIECE);
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case 5:
				if(ArrayUtils.contains(FOOT_DROP_MOBS, npc.getNpcId()) && Rnd.getChance(60))
				{
					st.giveItem(PETRIFIED_GIANT_FOOT_PIECE);
					if(st.getQuestItemsCount(PETRIFIED_GIANT_FOOT_PIECE) >= 10)
					{
						st.setCond(6);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
				break;
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == HADEL)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "hadel_q0177_12.htm";
				case CREATED:
					if(player.isSubClassActive() && player.getClassId().level() == ClassLevel.THIRD.ordinal())
					{
						if(player.getBaseClassId() >= 148) // Мейн должен быть пробужден
						{
							int checkForFutureClass = Util.getAwakenRelativeClass(player.getActiveClassId());

							// Если например пришли с сабом 2-ой профы
							if(checkForFutureClass == -1)
							{
								return "hadel_q0177_03.htm";
							}

							// Если пытаемся сделать дуал-сабкласс, который может в последствии переродиться в такой же как и основной
							if(Util.getGeneralIdForAwaken(checkForFutureClass) == Util.getGeneralIdForAwaken(player.getBaseClassId()))
							{
								switch(Util.getGeneralIdForAwaken(checkForFutureClass))
								{
									case 139:
										return "hadel_q0177_04.htm";
									case 140:
										return "hadel_q0177_05.htm";
									case 141:
										return "hadel_q0177_06.htm";
									case 142:
										return "hadel_q0177_07.htm";
									case 143:
										return "hadel_q0177_08.htm";
									case 144:
										return "hadel_q0177_10.htm";
									case 145:
										return "hadel_q0177_09.htm";
									case 146:
										return "hadel_q0177_11.htm";
									case -1:
										return null;
								}
							}

							// Проверяем, есть-ли уже у игрока дуал-сабклассы
							boolean alreadyHaveDualClass = false;
							for(SubClass sub : player.getSubClasses().values())
							{
								if(sub.isDualClass())
								{
									alreadyHaveDualClass = true;
								}
							}
							return !alreadyHaveDualClass ? "hadel_q0177_13.htm" : "hadel_q0177_12.htm";
						}
						else
						{
							return "hadel_q0177_03.htm";
						}
					}
					else
					{
						return "hadel_q0177_02.htm";
					}
				case STARTED:
					// Если пришли другим сабклассом или на мейне
					if(!player.isSubClassActive() || player.getLevel() < 80 || player.getClassId().level() != ClassLevel.THIRD.ordinal())
					{
						return "hadel_q0177_02.htm";
					}

					int checkForFutureClass = Util.getAwakenRelativeClass(player.getActiveClassId());

					// Если например пришли с сабом 2-ой профы
					if(checkForFutureClass == -1)
					{
						return "hadel_q0177_02.htm";
					}

					// Если пытаемся сделать дуал-сабкласс, который может в последсивии переродиться в такой же как и основной
					if(Util.getGeneralIdForAwaken(checkForFutureClass) == Util.getGeneralIdForAwaken(player.getBaseClassId()))
					{
						switch(Util.getGeneralIdForAwaken(checkForFutureClass))
						{
							case 139:
								return "hadel_q0177_04.htm";
							case 140:
								return "hadel_q0177_05.htm";
							case 141:
								return "hadel_q0177_06.htm";
							case 142:
								return "hadel_q0177_07.htm";
							case 143:
								return "hadel_q0177_08.htm";
							case 144:
								return "hadel_q0177_10.htm";
							case 145:
								return "hadel_q0177_09.htm";
							case 146:
								return "hadel_q0177_11.htm";
							case -1:
								return null;
						}
					}

					switch(st.getCond())
					{
						case 1:
						case 2:
							return "hadel_q0177_15.htm";
						case 3:
							return "hadel_q0177_17.htm";
						case 4:
						case 5:
							return "hadel_q0177_20.htm";
						case 6:
							return "hadel_q0177_21.htm";
						case 7:
						case 8:
							return "hadel_q0177_23.htm";
						case 9:
							return "hadel_q0177_24.htm";
						case 10:
							return "hadel_q0177_26.htm";
					}
					break;
			}
		}
		else if(npc.getNpcId() == MAESTRO_ISHUMA)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 7)
				{
					return "maestro_ishuma_q0177_01.htm";
				}
				else if(st.getCond() == 8)
				{
					st.setCond(9);
					st.giveItems(PETRIFIED_GIANT_HAND, 2);
					st.giveItems(PETRIFIED_GIANT_FOOT, 2);
					return "maestro_ishuma_q0177_04.htm";
				}
				else
				{
					return "maestro_ishuma_q0177_05.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "maestro_ishuma_q0177_05.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		if(!player.getSubClasses().isEmpty())
		{
			boolean isAllowToAddDC = true;
			for(SubClass sub : player.getSubClasses().values())
			{
				if(sub.isDualClass())
				{
					isAllowToAddDC = false;
				}
			}
			if(isAllowToAddDC && player.getBaseClassId() >= 148)
			{
				return true;
			}
		}
		return false;
	}
}
