package dwo.gameserver.handler.admincommands;

import dwo.gameserver.engine.databaseengine.FiltredStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.QuestList;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowQuestMark;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.text.TextBuilder;

import java.sql.ResultSet;

public class AdminShowQuests implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_charquestmenu", "admin_setcharquest"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		String[] cmdParams = command.split(" ");
		L2PcInstance target = null;
		L2Object targetObject = null;
		String[] val = new String[4];
		val[0] = null;

		if(cmdParams.length > 1)
		{
			target = WorldManager.getInstance().getPlayer(cmdParams[1]);
			if(cmdParams.length > 2)
			{
				if(cmdParams[2].equals("0"))
				{
					val[0] = "var";
					val[1] = "Start";
				}
				if(cmdParams[2].equals("1"))
				{
					val[0] = "var";
					val[1] = "STARTED";
				}
				if(cmdParams[2].equals("2"))
				{
					val[0] = "var";
					val[1] = "COMPLETED";
				}
				if(cmdParams[2].equals("3"))
				{
					val[0] = "full";
				}
				if(cmdParams[2].contains("_"))
				{
					val[0] = "name";
					val[1] = cmdParams[2];
				}
				if(cmdParams.length > 3)
				{
					if(cmdParams[3].equals("custom"))
					{
						val[0] = "custom";
						val[1] = cmdParams[2];
					}
				}
			}
		}
		else
		{
			targetObject = activeChar.getTarget();

			if(targetObject instanceof L2PcInstance)
			{
				target = (L2PcInstance) targetObject;
			}
		}

		if(target == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		if(command.startsWith("admin_charquestmenu"))
		{
			if(val[0] != null)
			{
				try
				{
					showQuestMenu(target, activeChar, val);
				}
				catch(Exception ignored)
				{
				}
			}
			else
			{
				showFirstQuestMenu(target, activeChar);
			}
		}
		else if(command.startsWith("admin_setcharquest"))
		{
			if(cmdParams.length >= 5)
			{
				val[0] = cmdParams[2];
				val[1] = cmdParams[3];
				val[2] = cmdParams[4];
				if(cmdParams.length == 6)
				{
					val[3] = cmdParams[5];
				}
				try
				{
					setQuestVar(target, activeChar, val);
				}
				catch(Exception ignored)
				{
				}
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showFirstQuestMenu(L2PcInstance target, L2PcInstance actor)
	{
		TextBuilder replyMSG = new TextBuilder("<html><body><table width=270>" + "<tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td width=180><center>Player: " + target.getName() + "</center></td>" + "<td width=45><button value=\"Back\" action=\"bypass -h admin_admin6\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int ID = target.getObjectId();

		replyMSG.append("Quest Menu for <font color=\"LEVEL\">").append(target.getName()).append("</font> (ID:").append(String.valueOf(ID)).append(")<br><center>");
		replyMSG.append("<table width=250><tr><td><button value=\"CREATED\" action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" 0\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"STARTED\" action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" 1\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"COMPLETED\" action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" 2\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><br><button value=\"All\" action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" 3\" width=85 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><br><br>Manual Edit by Quest number:<br></td></tr>");
		replyMSG.append("<tr><td><edit var=\"qn\" width=50 height=15><br><button value=\"Edit\" action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" $qn custom\" width=50 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("</table></center></body></html>");
		adminReply.setHtml(replyMSG.toString());
		actor.sendPacket(adminReply);
	}

	private void showQuestMenu(L2PcInstance target, L2PcInstance actor, String[] val)
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rs = null;

		try
		{
			int ID = target.getObjectId();

			TextBuilder replyMSG = new TextBuilder("<html><body>");
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();

			switch(val[0])
			{
				case "full":
					replyMSG.append("<table width=250><tr><td>Full Quest List for <font color=\"LEVEL\">").append(target.getName()).append("</font> (ID:").append(String.valueOf(ID)).append(")</td></tr>");
					rs = statement.executeQuery("SELECT DISTINCT name FROM character_quests WHERE charId='" + ID + "' ORDER by name");
					while(rs.next())
					{
						replyMSG.append("<tr><td><a action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" ").append(rs.getString(1)).append("\">").append(rs.getString(1)).append("</a></td></tr>");
					}
					replyMSG.append("</table></body></html>");
					break;
				case "name":
				{
					String[] states = {"CREATED", "STARTED", "COMPLETED"};
					String state = states[target.getQuestState(val[1]).getState().ordinal()];
					replyMSG.append("Character: <font color=\"LEVEL\">").append(target.getName()).append("</font><br>Quest: <font color=\"LEVEL\">").append(val[1]).append("</font><br>State: <font color=\"LEVEL\">").append(state).append("</font><br><br>");
					replyMSG.append("<center><table width=250><tr><td>Var</td><td>Value</td><td>New Value</td><td>&nbsp;</td></tr>");
					rs = statement.executeQuery("SELECT var,value FROM character_quests WHERE charId='" + ID + "' and name='" + val[1] + '\'');

					while(rs.next())
					{
						String var_name = rs.getString(1);
						if(var_name.equals("<state>"))
						{
							// Nothing
						}
						else
						{
							replyMSG.append("<tr><td>").append(var_name).append("</td><td>").append(rs.getString(2)).append("</td><td><edit var=\"var").append(var_name).append("\" width=80 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(val[1]).append(" ").append(var_name).append(" $var").append(var_name).append("\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Del\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(val[1]).append(" ").append(var_name).append(" delete\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
						}
					}
					replyMSG.append("</table><br><br><table width=250><tr><td>Repeatable quest:</td><td>Unrepeatable quest:</td></tr>");
					replyMSG.append("<tr><td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(val[1]).append(" state COMLETED 1\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					replyMSG.append("<td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(val[1]).append(" state COMLETED 0\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
					replyMSG.append("</table><br><br><font color=\"ff0000\">Delete Quest from DB:</font><br><button value=\"Quest Delete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(val[1]).append(" state DELETE\" width=120 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					replyMSG.append("</center></body></html>");
					break;
				}
				case "var":
					replyMSG.append("Character: <font color=\"LEVEL\">").append(target.getName()).append("</font><br>Quests with state: <font color=\"LEVEL\">").append(val[1]).append("</font><br>");
					replyMSG.append("<table width=250>");
					rs = statement.executeQuery("SELECT DISTINCT name FROM character_quests WHERE charId='" + ID + "' and var='<state>' and value='" + val[1] + '\'');
					while(rs.next())
					{
						replyMSG.append("<tr><td><a action=\"bypass -h admin_charquestmenu ").append(target.getName()).append(" ").append(rs.getString(1)).append("\">").append(rs.getString(1)).append("</a></td></tr>");
					}
					replyMSG.append("</table></body></html>");
					break;
				case "custom":
					boolean exqdb = true;
					boolean exqch = true;
					int qnumber = Integer.parseInt(val[1]);
					String state = null;
					String qname = null;
					QuestState qs = null;

					Quest quest = QuestManager.getInstance().getQuest(qnumber);

					if(quest != null)
					{
						qname = quest.getName();
						qs = target.getQuestState(qname);
					}
					else
					{
						exqdb = false;
					}

					if(qs != null)
					{
						state = qs.getState().toString();
					}
					else
					{
						exqch = false;
						state = "N/A";
					}

					if(exqdb)
					{
						if(exqch)
						{
							replyMSG.append("Character: <font color=\"LEVEL\">").append(target.getName()).append("</font><br>Quest: <font color=\"LEVEL\">").append(qname).append("</font><br>State: <font color=\"LEVEL\">").append(state).append("</font><br><br>");
							replyMSG.append("<center><table width=250><tr><td>Var</td><td>Value</td><td>New Value</td><td>&nbsp;</td></tr>");
							rs = statement.executeQuery("SELECT var,value FROM character_quests WHERE charId='" + ID + "' and name='" + qname + '\'');
							while(rs.next())
							{
								String var_name = rs.getString(1);
								if(var_name.equals("<state>"))
								{
									// Nothing
								}
								else
								{
									replyMSG.append("<tr><td>").append(var_name).append("</td><td>").append(rs.getString(2)).append("</td><td><edit var=\"var").append(var_name).append("\" width=80 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(qname).append(" ").append(var_name).append(" $var").append(var_name).append("\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Del\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(qname).append(" ").append(var_name).append(" delete\" width=30 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
								}
							}
							replyMSG.append("</table><br><br><table width=250><tr><td>Repeatable quest:</td><td>Unrepeatable quest:</td></tr>");
							replyMSG.append("<tr><td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(qname).append(" state COMLETED 1\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
							replyMSG.append("<td><button value=\"Quest Complete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(qname).append(" state COMLETED 0\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
							replyMSG.append("</table><br><br><font color=\"ff0000\">Delete Quest from DB:</font><br><button value=\"Quest Delete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(qname).append(" state DELETE\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
							replyMSG.append("</center></body></html>");
						}
						else
						{
							replyMSG.append("Character: <font color=\"LEVEL\">").append(target.getName()).append("</font><br>Quest: <font color=\"LEVEL\">").append(qname).append("</font><br>State: <font color=\"LEVEL\">").append(state).append("</font><br><br>");
							replyMSG.append("<center>Start this Quest for player:<br>");
							replyMSG.append("<button value=\"Create Quest\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(String.valueOf(qnumber)).append(" state CREATE\" width=100 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
							replyMSG.append("<font color=\"ee0000\">Only for Unrepeateble quests:</font><br>");
							replyMSG.append("<button value=\"Create & Complete\" action=\"bypass -h admin_setcharquest ").append(target.getName()).append(" ").append(String.valueOf(qnumber)).append(" state CC\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
							replyMSG.append("</center></body></html>");
						}
					}
					else
					{
						replyMSG.append("<center><font color=\"ee0000\">Quest with number </font><font color=\"LEVEL\">").append(String.valueOf(qnumber)).append("</font><font color=\"ee0000\"> doesn't exist!</font></center></body></html>");
					}
					break;
			}
			adminReply.setHtml(replyMSG.toString());
			actor.sendPacket(adminReply);
		}
		catch(Exception e)
		{
			actor.sendMessage("Error!");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void setQuestVar(L2PcInstance target, L2PcInstance actor, String[] val)
	{
		QuestState qs = target.getQuestState(val[0]);
		String[] outval = new String[3];

		if(val[1].equals("state"))
		{
			switch(val[2])
			{
				case "COMLETED":
					qs.exitQuest(val[3].equals("1") ? QuestType.REPEATABLE : QuestType.ONE_TIME);
					break;
				case "DELETE":
					qs.getQuest();
					Quest.deleteQuestInDb(qs, true);
					target.sendPacket(new QuestList());
					target.sendPacket(new ExShowQuestMark(qs.getQuest().getQuestId(), qs.getInt("cond")));
					break;
				case "CREATE":
					qs = QuestManager.getInstance().getQuest(Integer.parseInt(val[0])).newQuestState(target);
					qs.setState(QuestStateType.CREATED);
					qs.set("cond", "1");
					target.sendPacket(new QuestList());
					target.sendPacket(new ExShowQuestMark(qs.getQuest().getQuestId(), qs.getInt("cond")));
					val[0] = qs.getQuest().getName();
					break;
				case "CC":
					qs = QuestManager.getInstance().getQuest(Integer.parseInt(val[0])).newQuestState(target);
					qs.exitQuest(QuestType.ONE_TIME);
					target.sendPacket(new QuestList());
					target.sendPacket(new ExShowQuestMark(qs.getQuest().getQuestId(), qs.getInt("cond")));
					val[0] = qs.getQuest().getName();
					break;
			}
		}
		else
		{
			if(val[2].equals("delete"))
			{
				qs.unset(val[1]);
			}
			else
			{
				qs.set(val[1], val[2]);
			}
			target.sendPacket(new QuestList());
			target.sendPacket(new ExShowQuestMark(qs.getQuest().getQuestId(), qs.getInt("cond")));
		}
		actor.sendMessage("");
		outval[0] = "name";
		outval[1] = val[0];
		try
		{
			showQuestMenu(target, actor, outval);
		}
		catch(Exception ignored)
		{
		}
	}
}