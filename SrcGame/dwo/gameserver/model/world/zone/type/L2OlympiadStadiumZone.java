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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.olympiad.OlympiadGameTask;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadMatchEnd;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadUserInfo;

/**
 * An olympiad stadium
 *
 * @author durgus, DS
 */
public class L2OlympiadStadiumZone extends L2ZoneRespawn
{
	public L2OlympiadStadiumZone(int id)
	{
		super(id);
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if(settings == null)
		{
			settings = new Settings();
		}
		setSettings(settings);
	}

	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character.getInstanceId() == getInstanceId())
		{
			if(getSettings().getOlympiadTask() != null)
			{
				if(getSettings().getOlympiadTask().isBattleStarted())
				{
					character.setInsideZone(L2Character.ZONE_PVP, true);
					if(character instanceof L2PcInstance)
					{
						character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
						getSettings().getOlympiadTask().getGame().sendOlympiadInfo(character);
					}
				}
			}
			if(character instanceof L2Playable)
			{
				L2PcInstance player = character.getActingPlayer();
				if(player != null)
				{
					// only participants, observers and GMs allowed
					if(!player.isGM() && !player.getOlympiadController().isParticipating() && !player.getObserverController().isObserving())
					{
						ThreadPoolManager.getInstance().executeTask(new KickPlayer(player));
					}
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(getSettings().getOlympiadTask() != null)
		{
			if(getSettings().getOlympiadTask().isBattleStarted())
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				if(character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public void registerTask(OlympiadGameTask task)
	{
		getSettings().setTask(task);
	}

	public void openDoors()
	{
		InstanceManager.getInstance().getInstance(getInstanceId()).getDoors().stream().filter(door -> door != null && !door.isOpened()).forEach(L2DoorInstance::openMe);
	}

	public void closeDoors()
	{
		InstanceManager.getInstance().getInstance(getInstanceId()).getDoors().stream().filter(door -> door != null && door.isOpened()).forEach(L2DoorInstance::closeMe);
	}

	/***
	 * Скрытие баферов из олимпиадной зоны
	 */
	public void hideBuffers()
	{
		for(L2Npc npc : InstanceManager.getInstance().getInstance(getInstanceId()).getAllByNpcId(36402, true))
		{
			npc.getLocationController().setVisible(false);
		}
	}

	/***
	 * Сделать баферов видимыми в олимпиадной зоне
	 */
	public void showBuffers()
	{
		for(L2Npc npc : InstanceManager.getInstance().getInstance(getInstanceId()).getAllByNpcId(36402, true))
		{
			npc.getLocationController().setVisible(true);
		}
	}

	public void broadcastStatusUpdate(L2PcInstance player)
	{
		ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for(L2Character character : getCharactersInside())
		{
			if(character == null)
			{
				continue;
			}

			if(character instanceof L2PcInstance)
			{
				if(character.getActingPlayer().getObserverController().isObserving() || character.getActingPlayer().getOlympiadController().getSide() != player.getOlympiadController().getSide())
				{
					character.sendPacket(packet);
				}
			}
		}
	}

	public void broadcastPacketToObservers(L2GameServerPacket packet)
	{
		getCharactersInside().stream().filter(character -> character instanceof L2PcInstance && ((L2PcInstance) character).getObserverController().isObserving()).forEach(character -> character.sendPacket(packet));
	}

	public void updateZoneStatusForCharactersInside()
	{
		if(getSettings().getOlympiadTask() == null)
		{
			return;
		}

		boolean battleStarted = getSettings().getOlympiadTask().isBattleStarted();

		for(L2Character character : getCharactersInside())
		{
			if(character == null)
			{
				continue;
			}

			if(battleStarted)
			{
				character.setInsideZone(L2Character.ZONE_PVP, true);
				if(character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				}
			}
			else
			{
				character.setInsideZone(L2Character.ZONE_PVP, false);
				if(character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}

	private static class KickPlayer implements Runnable
	{
		private L2PcInstance _player;

		public KickPlayer(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			if(_player != null)
			{
				if(!_player.getPets().isEmpty())
				{
					for(L2Summon pet : _player.getPets())
					{
						pet.getLocationController().decay();
					}
				}
				_player.teleToLocation(TeleportWhereType.TOWN);
				_player.getInstanceController().setInstanceId(0);
				_player = null;
			}
		}
	}

	private class Settings extends AbstractZoneSettings
	{
		private OlympiadGameTask _task;

		public Settings()
		{
		}

		public OlympiadGameTask getOlympiadTask()
		{
			return _task;
		}

		protected void setTask(OlympiadGameTask task)
		{
			_task = task;
		}

		@Override
		public void clear()
		{
			_task = null;
		}
	}
}
