package dwo.scripts.quests;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.07.12
 * Time: 16:53
 */

public class _00421_LittleWingBigAdventure extends Quest
{
	// Квестовые предметы
	private static final int FT_LEAF = 4325;

	// Квестовые персонажи
	private static final int CRONOS = 30610;
	private static final int MIMYU = 30747;

	// Квестовые монстры
	private static final int[] Деревья = {27185, 27186, 27187, 27188};
	private static int Fairy_Tree_of_Wind = 27185;
	private static int Fairy_Tree_of_Star = 27186;
	private static int Fairy_Tree_of_Twilight = 27187;
	private static int Fairy_Tree_of_Abyss = 27188;

	public _00421_LittleWingBigAdventure()
	{
		addStartNpc(CRONOS);
		addTalkId(CRONOS, MIMYU);
		addAttackId(Деревья);
		// addKillId(Деревья); TODO: Спаун гвардов при килле деревьев
		questItemIds = new int[]{FT_LEAF};
	}

	private static boolean CheckTree(QuestState st, int Fairy_Tree_id)
	{
		return st.getInt(String.valueOf(Fairy_Tree_id)) == 1000000;
	}

	public static void main(String[] args)
	{
		new _00421_LittleWingBigAdventure();
	}

	@Override
	public int getQuestId()
	{
		return 421;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		QuestState st = attacker.getQuestState(getClass());

		if(st == null)
		{
			return super.onAttack(npc, attacker, damage, isPet);
		}

		if(CheckTree(st, npc.getNpcId()))
		{
			return super.onAttack(npc, attacker, damage, isPet);
		}

		if(isPet && st.getInt("id") < 16)
		{
			if(Rnd.getChance(2) && st.hasQuestItems(FT_LEAF))
			{
				st.takeItems(FT_LEAF, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				npc.broadcastPacket(new NS(npc.getNpcId(), ChatType.NPC_ALL, npc.getNpcId(), 42111));
				st.set(String.valueOf(npc.getNpcId()), "1000000");
				if(CheckTree(st, Fairy_Tree_of_Wind) && CheckTree(st, Fairy_Tree_of_Star) && CheckTree(st, Fairy_Tree_of_Twilight) && CheckTree(st, Fairy_Tree_of_Abyss))
				{
					st.set("id", "15");
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isStarted())
		{
			if(st.hasQuestItems(3500) || st.hasQuestItems(3501) || st.hasQuestItems(3502))
			{
				if(st.hasQuestItems(3500))
				{
					L2ItemInstance item = st.getPlayer().getInventory().getItemByItemId(3500);
					if(item.getEnchantLevel() < 55)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "sage_cronos_q0421_06.htm";
					}
					else
					{
						st.startQuest();
						st.set("summonOid", String.valueOf(item.getObjectId()));
						st.set("id", "1");
					}
				}
				else if(st.hasQuestItems(3501))
				{
					L2ItemInstance item = st.getPlayer().getInventory().getItemByItemId(3501);
					if(item.getEnchantLevel() < 55)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "sage_cronos_q0421_06.htm";
					}
					else
					{
						st.startQuest();
						st.set("summonOid", String.valueOf(item.getObjectId()));
						st.set("id", "1");
					}
				}
				else if(st.hasQuestItems(3502))
				{
					L2ItemInstance item = st.getPlayer().getInventory().getItemByItemId(3502);
					if(item.getEnchantLevel() < 55)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "sage_cronos_q0421_06.htm";
					}
					else
					{
						st.startQuest();
						st.set("summonOid", String.valueOf(item.getObjectId()));
						st.set("id", "1");
					}
				}
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "sage_cronos_q0421_06.htm";
			}
			return "sage_cronos_q0421_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == MIMYU)
		{
			if(reply == 1)
			{
				L2Summon summon = player.getPets().getFirst();
				if(summon != null)
				{
					return summon.getControlObjectId() == st.getInt("summonOid") ? "fairy_mymyu_q0421_04.htm" : "fairy_mymyu_q0421_03.htm";
				}
			}
			else if(reply == 2)
			{
				L2Summon summon = player.getPets().getFirst();
				if(summon != null)
				{
					if(summon.getControlObjectId() == st.getInt("summonOid"))
					{
						st.giveItems(FT_LEAF, 4);
						st.setCond(2);
						st.set("id", "0");
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						return "fairy_mymyu_q0421_05.htm";
					}
					else
					{
						return "fairy_mymyu_q0421_06.htm";
					}
				}
				else
				{
					return "fairy_mymyu_q0421_06.htm";
				}
			}
			else if(reply == 3)
			{
				return "fairy_mymyu_q0421_07.htm";
			}
			else if(reply == 4)
			{
				return "fairy_mymyu_q0421_08.htm";
			}
			else if(reply == 5)
			{
				return "fairy_mymyu_q0421_09.htm";
			}
			else if(reply == 6)
			{
				return "fairy_mymyu_q0421_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		L2PcInstance player = st.getPlayer();

		if(npcId == CRONOS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 45 && (st.hasQuestItems(3500) || st.hasQuestItems(3501) || st.hasQuestItems(3502)))
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "sage_cronos_q0421_01.htm";
					}
					if(player.getLevel() >= 45 && st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) >= 2)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "sage_cronos_q0421_02.htm";
					}
					if(player.getLevel() >= 45 && st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) == 1)
					{
						if(st.hasQuestItems(3500))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3500);
							return item.getEnchantLevel() < 55 ? "sage_cronos_q0421_03.htm" : "sage_cronos_q0421_04.htm";
						}
						else if(st.hasQuestItems(3501))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3501);
							return item.getEnchantLevel() < 55 ? "sage_cronos_q0421_03.htm" : "sage_cronos_q0421_04.htm";
						}
						else if(st.hasQuestItems(3502))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3502);
							return item.getEnchantLevel() < 55 ? "sage_cronos_q0421_03.htm" : "sage_cronos_q0421_04.htm";
						}
					}
					break;
				case STARTED:
					return "sage_cronos_q0421_07.htm";
			}
		}
		else if(npcId == MIMYU)
		{
			if(st.isStarted())
			{
				if(st.getInt("id") == 0)
				{
					return "fairy_mymyu_q0421_07.htm";
				}
				else if(st.getInt("id") == 1)
				{
					st.set("id", "2");
					return "fairy_mymyu_q0421_01.htm";
				}
				else if(st.getInt("id") == 2)
				{
					L2Summon summon = player.getPets().getFirst();
					if(summon != null)
					{
						return summon.getControlObjectId() == st.getInt("summonOid") ? "fairy_mymyu_q0421_04.htm" : "fairy_mymyu_q0421_03.htm";
					}
					else
					{
						return "fairy_mymyu_q0421_02.htm";
					}
				}
				else if(st.getInt("id") > 0 && st.getInt("id") < 15 && st.hasQuestItems(FT_LEAF))
				{
					return "fairy_mymyu_q0421_11.htm";
				}
				else if(st.getInt("id") == 15 && !st.hasQuestItems(FT_LEAF))
				{
					L2Summon summon = player.getPets().getFirst();
					if(summon != null)
					{
						if(summon.getControlObjectId() == st.getInt("summonOid"))
						{
							st.set("id", "16");
							return "fairy_mymyu_q0421_13.htm";
						}
						else
						{
							return "fairy_mymyu_q0421_14.htm";
						}
					}
					else
					{
						return "fairy_mymyu_q0421_12.htm";
					}
				}
				else if(st.getInt("id") == 16)
				{
					// TODO убрать хардкод по сумонам
					L2Summon summon = null;
					if(player.getPets() != null && !player.getPets().isEmpty())
					{
						summon = player.getPets().getFirst();
					}

					if(summon != null)
					{
						return "fairy_mymyu_q0421_15.htm";
					}
					else if(st.hasQuestItems(3500) || st.hasQuestItems(3501) || st.hasQuestItems(3502))
					{
						if(st.hasQuestItems(3500))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3500);
							if(item.getObjectId() == st.getInt("summonOid"))
							{
								//EvolvePet(player,item,4422)
								st.takeItems(3500, 1);
								st.giveItems(4422, 1);
								st.exitQuest(QuestType.REPEATABLE);
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								return "fairy_mymyu_q0421_16.htm";
							}
							else
							{
								npc.setTarget(player);
								L2Skill skill = SkillTable.getInstance().getInfo(4167, 1);
								if(skill != null)
								{
									skill.getEffects(npc, player);
								}
								return "fairy_mymyu_q0421_18.htm";
							}
						}
						else if(st.hasQuestItems(3501))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3501);
							if(item.getObjectId() == st.getInt("summonOid"))
							{
								//EvolvePet(player,item,4422)
								st.takeItems(3501, 1);
								st.giveItems(4423, 1);
								st.exitQuest(QuestType.REPEATABLE);
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								return "fairy_mymyu_q0421_16.htm";
							}
							else
							{
								npc.setTarget(player);
								L2Skill skill = SkillTable.getInstance().getInfo(4167, 1);
								if(skill != null)
								{
									skill.getEffects(npc, player);
								}
								return "fairy_mymyu_q0421_18.htm";
							}
						}
						else if(st.hasQuestItems(3502))
						{
							L2ItemInstance item = player.getInventory().getItemByItemId(3502);
							if(item.getObjectId() == st.getInt("summonOid"))
							{
								//EvolvePet(player,item,4422)
								st.takeItems(3502, 1);
								st.giveItems(4424, 1);
								st.exitQuest(QuestType.REPEATABLE);
								st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
								return "fairy_mymyu_q0421_16.htm";
							}
							else
							{
								npc.setTarget(player);
								L2Skill skill = SkillTable.getInstance().getInfo(4167, 1);
								if(skill != null)
								{
									skill.getEffects(npc, player);
								}
								return "fairy_mymyu_q0421_18.htm";
							}
						}
						else
						{
							return "fairy_mymyu_q0421_18.htm";
						}
					}
					else if(st.getQuestItemsCount(3500) + st.getQuestItemsCount(3501) + st.getQuestItemsCount(3502) >= 2)
					{
						return "fairy_mymyu_q0421_17.htm";
					}
				}
			}
		}
		return getNoQuestMsg(player);
	}

	private void evolvePet(L2PcInstance player, L2ItemInstance item, int striderControlItem)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE items SET item_id =? WHERE object_id=? AND owner_id=?");
			statement.setInt(1, striderControlItem);
			statement.setInt(2, item.getObjectId());
			statement.setInt(3, player.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while updating dragon to strider: Player: " + player.getName() + " Control Item Id: " + striderControlItem + " : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addNumber(1));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.getEnchantLevel()).addItemName(striderControlItem));
			player.sendPacket(new ItemList(player, false));
		}
	}
}