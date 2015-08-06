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
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.controller.object.PolyController;
import dwo.gameserver.model.actor.instance.L2ChestInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SSQInfo;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SunRise;
import dwo.gameserver.network.game.serverpackets.SunSet;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExRedSky;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import dwo.gameserver.util.Broadcast;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * <li> invis/invisible/vis/visible = makes yourself invisible or visible
 * <li> earthquake = causes an earthquake of a given intensity and duration around you
 * <li> bighead/shrinkhead = changes head size
 * <li> gmspeed = temporary Super Haste effect.
 * <li> para/unpara = paralyze/remove paralysis from target
 * <li> para_all/unpara_all = same as para/unpara, affects the whole world.
 * <li> polyself/unpolyself = makes you look as a specified mob.
 * <li> changename = temporary change name
 * <li> clearteams/setteam_close/setteam = team related commands
 * <li> social = forces an L2Character instance to broadcast social action packets.
 * <li> effect = forces an L2Character instance to broadcast MSU packets.
 * <li> abnormal = force changes over an L2Character instance's abnormal state.
 * <li> play_sound/play_sounds = Music broadcasting related commands
 * <li> atmosphere = sky change related commands.
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_invis", "admin_invisible", "admin_vis", "admin_visible", "admin_invis_menu", "admin_earthquake",
		"admin_earthquake_menu", "admin_bighead", "admin_shrinkhead", "admin_gmspeed", "admin_gmspeed_menu",
		"admin_unpara_all", "admin_para_all", "admin_unpara", "admin_para", "admin_unpara_all_menu",
		"admin_para_all_menu", "admin_unpara_menu", "admin_para_menu", "admin_polyself", "admin_unpolyself",
		"admin_polyself_menu", "admin_unpolyself_menu", "admin_clearteams", "admin_setteam_close", "admin_setteam",
		"admin_social", "admin_effect", "admin_social_menu", "admin_special", "admin_special_menu", "admin_effect_menu",
		"admin_abnormal", "admin_abnormal_menu", "admin_play_sounds", "admin_play_sound", "admin_atmosphere",
		"admin_atmosphere_menu", "admin_set_displayeffect", "admin_set_displayeffect_menu", "admin_set_animation",
		"admin_abt"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if(command.equals("admin_invis_menu"))
		{
			if(activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
			else
			{
				activeChar.getAppearance().setInvisible();
				activeChar.broadcastUserInfo();
				activeChar.getLocationController().decay();
				activeChar.getLocationController().spawn();
			}
			RegionBBSManager.getInstance().changeCommunityBoard();
			command = "";
			AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
		}
		else if(command.startsWith("admin_invis"))
		{
			activeChar.getAppearance().setInvisible();
			activeChar.broadcastUserInfo();
			activeChar.getLocationController().decay();
			activeChar.getLocationController().spawn();
			RegionBBSManager.getInstance().changeCommunityBoard();
		}

		else if(command.startsWith("admin_vis"))
		{
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
			RegionBBSManager.getInstance().changeCommunityBoard();
		}

		else if(command.startsWith("admin_earthquake"))
		{
			try
			{
				String val1 = st.nextToken();
				int intensity = Integer.parseInt(val1);
				String val2 = st.nextToken();
				int duration = Integer.parseInt(val2);
				EarthQuake eq = new EarthQuake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
				activeChar.broadcastPacket(eq);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //earthquake <intensity> <duration>");
			}
		}
		else if(command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				int duration = Integer.parseInt(st.nextToken());
				adminAtmosphere(type, state, duration, activeChar);
			}
			catch(Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
			}
		}
		else if(command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if(command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(18) + ".htm");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sounds <pagenumber>");
			}
		}
		else if(command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //play_sound <soundname>");
			}
		}
		else if(command.equals("admin_para_all"))
		{
			try
			{

				activeChar.getKnownList().getKnownPlayers().values().stream().filter(player -> !player.isGM()).forEach(player -> {
					player.startAbnormalEffect(AbnormalEffect.HOLD_1);
					player.setIsParalyzed(true);
					player.startParalyze();
				});
			}
			catch(Exception e)
			{
			}
		}
		else if(command.equals("admin_unpara_all"))
		{
			try
			{
				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					player.setIsParalyzed(false);
					player.stopParalyze(false);
				}
			}
			catch(Exception ignored)
			{
			}
		}
		else if(command.startsWith("admin_para")) // || command.startsWith("admin_para_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch(Exception e)
			{
			}
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					if(type.equals("1"))
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_1);
					}
					else
					{
						player.startAbnormalEffect(AbnormalEffect.HOLD_2);
					}
					player.setIsParalyzed(true);
					player.startParalyze();
				}
			}
			catch(Exception e)
			{
			}
		}
		else if(command.startsWith("admin_unpara")) // || command.startsWith("admin_unpara_menu"))
		{
			String type = "1";
			try
			{
				type = st.nextToken();
			}
			catch(Exception e)
			{
			}
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					if(type.equals("1"))
					{
						player.stopAbnormalEffect(AbnormalEffect.HOLD_1);
					}
					else
					{
						player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
					}
					player.setIsParalyzed(false);
					player.stopParalyze(false);
				}
			}
			catch(Exception e)
			{
			}
		}
		else if(command.startsWith("admin_bighead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.startAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch(Exception e)
			{
			}
		}
		else if(command.startsWith("admin_shrinkhead"))
		{
			try
			{
				L2Object target = activeChar.getTarget();
				L2Character player = null;
				if(target instanceof L2Character)
				{
					player = (L2Character) target;
					player.stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
				}
			}
			catch(Exception e)
			{
			}
		}
		else if(command.startsWith("admin_gmspeed"))
		{
			try
			{
				int val = Integer.parseInt(st.nextToken());
				boolean sendMessage = activeChar.getFirstEffect(7029) != null;
				activeChar.stopSkillEffects(7029);
				if(val == 0 && sendMessage)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(7029));
				}
				else if(val >= 1 && val <= 4)
				{
					L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
					activeChar.doSimultaneousCast(gmSpeedSkill);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //gmspeed <value> (0=off...4=max)");
			}
			if(command.contains("_menu"))
			{
				command = "";
				AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
			}
		}
		else if(command.startsWith("admin_polyself"))
		{
			try
			{
				String id = st.nextToken();
				activeChar.getPolyController().setInfo(PolyController.PolyType.NPC, Integer.parseInt(id));
				activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
				CI info1 = new CI(activeChar);
				activeChar.broadcastPacket(info1);
				activeChar.sendUserInfo();
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //polyself <npcId>");
			}
		}
		else if(command.startsWith("admin_unpolyself"))
		{
			activeChar.getPolyController().clearInfo();
			activeChar.getLocationController().decay();
			activeChar.getLocationController().spawn(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			CI info1 = new CI(activeChar);
			activeChar.broadcastPacket(info1);
			activeChar.sendUserInfo();
		}
		else if(command.equals("admin_clearteams"))
		{
			try
			{
				for(L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
				{
					player.setTeam(0);
					player.broadcastUserInfo();
				}
			}
			catch(Exception ignored)
			{
			}
		}
		else if(command.startsWith("admin_setteam_close"))
		{
			try
			{
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				activeChar.getKnownList().getKnownPlayers().values().stream().filter(player -> activeChar.isInsideRadius(player, 400, false, true)).forEach(player -> {
					player.setTeam(teamVal);
					if(teamVal != 0)
					{
						player.sendMessage("You have joined team " + teamVal);
					}
					player.broadcastUserInfo();
				});
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //setteam_close <teamId>");
			}
		}
		else if(command.startsWith("admin_setteam"))
		{
			try
			{
				String val = st.nextToken();
				int teamVal = Integer.parseInt(val);
				L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if(target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				player.setTeam(teamVal);
				if(teamVal != 0)
				{
					player.sendMessage("You have joined team " + teamVal);
				}
				player.broadcastUserInfo();
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //setteam <teamId>");
			}
		}
		else if(command.startsWith("admin_social"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if(st.countTokens() == 2)
				{
					int social = Integer.parseInt(st.nextToken());
					target = st.nextToken();
					if(target != null)
					{
						L2PcInstance player = WorldManager.getInstance().getPlayer(target);
						if(player != null)
						{
							if(performSocial(social, player, activeChar))
							{
								activeChar.sendMessage(player.getName() + " was affected by your request.");
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for(L2Character object : activeChar.getKnownList().getKnownCharactersInRadius(radius))
								{
									performSocial(social, object, activeChar);
								}

								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch(NumberFormatException nbe)
							{
								activeChar.sendMessage("Incorrect parameter");
							}
						}
					}
				}
				else if(st.countTokens() == 1)
				{
					int social = Integer.parseInt(st.nextToken());
					if(obj == null)
					{
						obj = activeChar;
					}

					if(performSocial(social, obj, activeChar))
					{
						activeChar.sendMessage(obj.getName() + " was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if(!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
				}
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
		else if(command.startsWith("admin_abnormal"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if(st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int abnormal = Integer.decode("0x" + parm);
					target = st.nextToken();
					if(target != null)
					{
						L2PcInstance player = WorldManager.getInstance().getPlayer(target);
						if(player != null)
						{
							if(performAbnormal(abnormal, player))
							{
								activeChar.sendMessage(player.getName() + "'s abnormal status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for(L2Object object : activeChar.getKnownList().getKnownCharactersInRadius(radius))
								{
									performAbnormal(abnormal, object);
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch(NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
							}
						}
					}
				}
				else if(st.countTokens() == 1)
				{
					int abnormal = Integer.decode("0x" + st.nextToken());
					if(obj == null)
					{
						obj = activeChar;
					}

					if(performAbnormal(abnormal, obj))
					{
						activeChar.sendMessage(obj.getName() + "'s abnormal status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if(!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //abnormal <abnormal_mask> [player_name|radius]");
				}
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
		else if(command.startsWith("admin_special"))
		{
			try
			{
				String target = null;
				L2Object obj = activeChar.getTarget();
				if(st.countTokens() == 2)
				{
					String parm = st.nextToken();
					int special = Integer.decode("0x" + parm);
					target = st.nextToken();
					if(target != null)
					{
						L2PcInstance player = WorldManager.getInstance().getPlayer(target);
						if(player != null)
						{
							if(performAbnormal(special, player))
							{
								activeChar.sendMessage(player.getName() + "'s special status was affected by your request.");
							}
							else
							{
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
							}
						}
						else
						{
							try
							{
								int radius = Integer.parseInt(target);
								for(L2Object object : activeChar.getKnownList().getKnownCharactersInRadius(radius))
								{
									performAbnormal(special, object);
								}
								activeChar.sendMessage(radius + " units radius affected by your request.");
							}
							catch(NumberFormatException nbe)
							{
								activeChar.sendMessage("Usage: //special <hex_special_mask> [player|radius]");
							}
						}
					}
				}
				else if(st.countTokens() == 1)
				{
					int special = Integer.decode("0x" + st.nextToken());
					if(obj == null)
					{
						obj = activeChar;
					}

					if(performAbnormal(special, obj))
					{
						activeChar.sendMessage(obj.getName() + "'s special status was affected by your request.");
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					}
				}
				else if(!command.contains("menu"))
				{
					activeChar.sendMessage("Usage: //special <special_mask> [player_name|radius]");
				}
			}
			catch(Exception e)
			{
				// Ignored
			}
		}
		else if(command.startsWith("admin_effect"))
		{
			try
			{
				L2Object obj = activeChar.getTarget();
				int level = 1;
				int hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				if(st.hasMoreTokens())
				{
					level = Integer.parseInt(st.nextToken());
				}
				if(st.hasMoreTokens())
				{
					hittime = Integer.parseInt(st.nextToken());
				}
				if(obj == null)
				{
					obj = activeChar;
				}
				if(obj instanceof L2Character)
				{
					L2Character target = (L2Character) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + '/' + level + " by your request.");
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}

			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		else if(command.startsWith("admin_set_displayeffect"))
		{
			L2Object target = activeChar.getTarget();
			if(!(target instanceof L2Npc))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			L2Npc npc = (L2Npc) target;
			try
			{
				String type = st.nextToken();
				int diplayeffect = Integer.parseInt(type);
				npc.setDisplayEffect(diplayeffect);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //set_displayeffect <id>");
			}
		}
		else if(command.startsWith("admin_set_animation"))
		{
			try
			{
				if(activeChar.getTarget() instanceof L2NpcInstance)
				{
					String val = command.substring(20);
					((L2NpcInstance) activeChar.getTarget()).sendPacket(new SocialAction(activeChar.getTarget().getObjectId(), Integer.parseInt(val)));
				}
				else
				{
					activeChar.sendMessage("Эта комманда может быть использована только на мобах!");
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				//Case of empty
			}
			catch(NumberFormatException nfe)
			{
				activeChar.sendMessage("Использование: //set_animation <anim_id>");
			}
		}
		if(command.contains("menu"))
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
	 * @param action bitmask that should be applied over target's abnormal
	 * @param target
	 * @return <i>true</i> if target's abnormal state was affected , <i>false</i> otherwise.
	 */
	private boolean performAbnormal(int action, L2Object target)
	{
		if(target instanceof L2Character)
		{
			L2Character character = (L2Character) target;
			if(character.getAbnormalEffects().contains(action))
			{
				character.stopAbnormalEffect(action);
			}
			else
			{
				character.startAbnormalEffect(action);
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean performSocial(int action, L2Object target, L2PcInstance activeChar)
	{
		try
		{
			if(target instanceof L2Character)
			{
				if(target instanceof L2ChestInstance)
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if(target instanceof L2Npc && (action < 1 || action > 3))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				if(target instanceof L2PcInstance && (action < 2 || action > 18 && action != SocialAction.LEVEL_UP))
				{
					activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
					return false;
				}
				L2Character character = (L2Character) target;
				character.broadcastPacket(new SocialAction(character.getObjectId(), action));
			}
			else
			{
				return false;
			}
		}
		catch(Exception e)
		{
		}
		return true;
	}

	/**
	 * @param type     - atmosphere type (signssky,sky)
	 * @param state    - atmosphere state(night,day)
	 * @param duration
	 */
	private void adminAtmosphere(String type, String state, int duration, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;

		switch(type)
		{
			case "signsky":
				if(state.equals("dawn"))
				{
					packet = new SSQInfo(2);
				}
				else if(state.equals("dusk"))
				{
					packet = new SSQInfo(1);
				}
				break;
			case "sky":
				switch(state)
				{
					case "night":
						packet = new SunSet();
						break;
					case "day":
						packet = new SunRise();
						break;
					case "red":
						packet = duration != 0 ? new ExRedSky(duration) : new ExRedSky(10);
						break;
				}
				break;
			default:
				activeChar.sendMessage("Usage: //atmosphere <signsky dawn|dusk>|<sky day|night|red>");
				break;
		}
		if(packet != null)
		{
			Broadcast.toAllOnlinePlayers(packet);
		}
	}

	private void playAdminSound(L2PcInstance activeChar, String sound)
	{
		PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		activeChar.sendMessage("Playing " + sound + '.');
	}

	private void showMainPage(L2PcInstance activeChar, String command)
	{
		String filename = "effects_menu";
		if(command.contains("abnormal"))
		{
			filename = "abnormal";
		}
		else if(command.contains("special"))
		{
			filename = "special";
		}
		else if(command.contains("social"))
		{
			filename = "social";
		}
		AdminHelpPage.showHelpPage(activeChar, filename + ".htm");
	}
}
