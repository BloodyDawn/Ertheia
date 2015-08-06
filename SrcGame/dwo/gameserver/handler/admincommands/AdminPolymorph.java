/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.TransformationManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.controller.object.PolyController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SetupGauge;

import java.util.StringTokenizer;

public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_polymorph", "admin_unpolymorph", "admin_polymorph_menu", "admin_unpolymorph_menu", "admin_transform",
		"admin_untransform", "admin_transform_menu", "admin_untransform_menu",
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
			return false;
		}

		if(activeChar.isTransformed() || activeChar.isInStance())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}

		if(activeChar.isInWater())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			return false;
		}

		if(activeChar.isFlyingMounted() || activeChar.isMounted() || activeChar.isRidingStrider())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
			return false;
		}

		if(command.startsWith("admin_untransform"))
		{
			L2Object obj = activeChar.getTarget();
			if(obj instanceof L2Character)
			{
				((L2Character) obj).stopTransformation(true);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if(command.startsWith("admin_transform"))
		{
			L2Object obj = activeChar.getTarget();
			if(obj instanceof L2PcInstance)
			{
				L2PcInstance cha = (L2PcInstance) obj;

				String[] parts = command.split(" ");
				if(parts.length >= 2)
				{
					try
					{
						int id = Integer.parseInt(parts[1]);
						if(!TransformationManager.getInstance().transformPlayer(id, cha))
						{
							cha.sendMessage("Неизвестный ID трансформации: " + id);
						}
					}
					catch(NumberFormatException e)
					{
						activeChar.sendMessage("Использование: //transform <id>");
					}
				}
				else if(parts.length == 1)
				{
					cha.untransform(true);
				}
				else
				{
					activeChar.sendMessage("Использование: //transform <id>");
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		if(command.startsWith("admin_polymorph"))
		{
			StringTokenizer st = new StringTokenizer(command);
			L2Object target = activeChar.getTarget();
			try
			{
				st.nextToken();
				String p1 = st.nextToken();
				if(st.hasMoreTokens())
				{
					String p2 = st.nextToken();
					doPolymorph(activeChar, target, p2, p1);
				}
				else
				{
					doPolymorph(activeChar, target, p1, "npc");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Использование: //polymorph [type] <id>");
			}
		}
		else if(command.equals("admin_unpolymorph"))
		{
			doUnpoly(activeChar, activeChar.getTarget());
		}
		if(command.contains("_menu"))
		{
			showMainPage(activeChar, command);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * @param activeChar
	 * @param obj
	 * @param id
	 * @param type
	 */
	private void doPolymorph(L2PcInstance activeChar, L2Object obj, String id, String type)
	{
		if(obj != null)
		{
			obj.getPolyController().setInfo(PolyController.PolyType.valueOf(type.toUpperCase()), Integer.parseInt(id));
			//animation
			if(obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				Char.broadcastPacket(new MagicSkillUse(Char, 1008, 1, 4000, 0));
				Char.sendPacket(new SetupGauge(SetupGauge.BLUE_DUAL, 4000));
			}
			//end of animation
			obj.getLocationController().decay();
			obj.getLocationController().spawn(obj.getX(), obj.getY(), obj.getZ());
			activeChar.sendMessage("Polymorph succeed");
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

	/**
	 * @param activeChar
	 * @param target
	 */
	private void doUnpoly(L2PcInstance activeChar, L2Object target)
	{
		if(target != null)
		{
			target.getPolyController().clearInfo();
			target.getLocationController().decay();
			target.getLocationController().spawn(target.getX(), target.getY(), target.getZ());
			activeChar.sendMessage("Unpolymorph succeed");
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}

	private void showMainPage(L2PcInstance activeChar, String command)
	{
		if(command.contains("transform"))
		{
			AdminHelpPage.showHelpPage(activeChar, "transform.htm");
		}
		else if(command.contains("abnormal"))
		{
			AdminHelpPage.showHelpPage(activeChar, "abnormal.htm");
		}
		else
		{
			AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
		}
	}
}
