package dwo.gameserver.model.world.zone.type;

import dwo.config.Config;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.residence.Siegable;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortSiegeEngine;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;

/**
 * A  siege zone
 * @author durgus
 */

public class L2SiegeZone extends L2ZoneType
{
	private static final int DISMOUNT_DELAY = 5;

	public L2SiegeZone(int id)
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
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "castleId":
				if(getSettings().getSiegeableId() != -1)
				{
					throw new IllegalArgumentException("CastleSiegeEngine object already defined!");
				}
				getSettings().setSiegeableId(Integer.parseInt(value));
				break;
			case "fortId":
				if(getSettings().getSiegeableId() != -1)
				{
					throw new IllegalArgumentException("CastleSiegeEngine object already defined!");
				}
				getSettings().setSiegeableId(Integer.parseInt(value));
				break;
			case "clanHallId":
				if(getSettings().getSiegeableId() != -1)
				{
					throw new IllegalArgumentException("CastleSiegeEngine object already defined!");
				}
				getSettings().setSiegeableId(Integer.parseInt(value));
				ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getSiegableHall(getSettings().getSiegeableId());
				if(hall == null)
				{
					_log.log(Level.WARN, "L2SiegeZone: Siegable clan hall with id " + value + " does not exist!");
				}
				else
				{
					hall.setSiegeZone(this);
				}
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(getSettings().isActiveSiege())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			character.setInsideZone(L2Character.ZONE_SIEGE, true);
			character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true); // FIXME: Custom ?

			if(character instanceof L2PcInstance)
			{

				if(((L2PcInstance) character).isRegisteredOnThisSiegeField(getSettings().getSiegeableId()))
				{
					((L2PcInstance) character).setIsInSiege(true); // in siege
					if(getSettings().getSiege().giveFame() && getSettings().getSiege().getFameFrequency() > 0)
					{
						((L2PcInstance) character).startFameTask(getSettings().getSiege().getFameFrequency() * 1000, getSettings().getSiege().getFameAmount());
					}
				}
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				if(!Config.ALLOW_WYVERN_DURING_SIEGE && ((L2PcInstance) character).getMountType() == 2)
				{
					character.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
					((L2PcInstance) character).enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		character.setInsideZone(L2Character.ZONE_SIEGE, false);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false); // FIXME: Custom ?
		if(getSettings().isActiveSiege())
		{
			if(character instanceof L2PcInstance)
			{
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				if(((L2PcInstance) character).getMountType() == 2)
				{
					((L2PcInstance) character).exitedNoLanding();
				}
				// Set pvp flag
				if(!((L2PcInstance) character).getPvPFlagController().isFlagged())
				{
					((L2PcInstance) character).getPvPFlagController().startFlag();
				}
			}
		}
		if(character instanceof L2PcInstance)
		{
			L2PcInstance activeChar = (L2PcInstance) character;
			activeChar.stopFameTask();
			activeChar.setIsInSiege(false);

			if(getSettings().getSiege() instanceof FortSiegeEngine && activeChar.getInventory().getItemByItemId(9819) != null)
			{
				// drop combat flag
				Fort fort = FortManager.getInstance().getFortById(getSettings().getSiegeableId());
				if(fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getFortId());
				}
				else
				{
					long slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
					activeChar.getInventory().unEquipItemInBodySlot(slot);
					activeChar.destroyItem(ProcessType.COMBATFLAG, activeChar.getInventory().getItemByItemId(9819), null, true);
				}
			}
		}

		if(character instanceof L2SiegeSummonInstance)
		{
			character.getLocationController().decay();
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		if(getSettings().isActiveSiege())
		{
			// debuff participants only if they die inside siege zone
			if(character instanceof L2PcInstance && ((L2PcInstance) character).isRegisteredOnThisSiegeField(getSettings().getSiegeableId()))
			{
				int lvl = 1;
				L2Effect e = character.getFirstEffect(5660);
				if(e != null)
				{
					lvl = Math.min(lvl + e.getSkill().getLevel(), 5);
				}

				L2Skill skill = SkillTable.getInstance().getInfo(5660, lvl);
				if(skill != null)
				{
					skill.getEffects(character, character);
				}
			}
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public void updateZoneStatusForCharactersInside()
	{
		if(getSettings().isActiveSiege())
		{
			getCharactersInside().stream().filter(character -> character != null).forEach(this::onEnter);
		}
		else
		{
			for(L2Character character : getCharactersInside())
			{
				if(character == null)
				{
					continue;
				}
				character.setInsideZone(L2Character.ZONE_PVP, false);
				character.setInsideZone(L2Character.ZONE_SIEGE, false);
				character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);

				if(character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					((L2PcInstance) character).stopFameTask();
					if(((L2PcInstance) character).getMountType() == 2)
					{
						((L2PcInstance) character).exitedNoLanding();
					}
				}
				if(character instanceof L2SiegeSummonInstance)
				{
					character.getLocationController().decay();
				}
			}
		}
	}

	/**
	 * Sends a message to all players in this zone
	 *
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		getPlayersInside().stream().filter(player -> player != null).forEach(player -> player.sendMessage(message));
	}

	public int getSiegeObjectId()
	{
		return getSettings().getSiegeableId();
	}

	public boolean isSiegeActive()
	{
		return getSettings().isActiveSiege();
	}

	public void setIsSiegeActive(boolean val)
	{
		getSettings().setActiveSiege(val);
	}

	public void setSiegeInstance(Siegable siege)
	{
		getSettings().setSiege(siege);
	}

	/**
	 * Removes all foreigners from the zone
	 *
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		TeleportWhereType type = TeleportWhereType.TOWN;
		for(L2PcInstance temp : getPlayersInside())
		{
			if(temp.getClanId() == owningClanId)
			{
				continue;
			}

			temp.teleToLocation(type);
		}
	}

	private class Settings extends AbstractZoneSettings
	{
		private int _siegableId = -1;
		private Siegable _siege;
		private boolean _isActiveSiege;

		public Settings()
		{
		}

		public int getSiegeableId()
		{
			return _siegableId;
		}

		protected void setSiegeableId(int id)
		{
			_siegableId = id;
		}

		public Siegable getSiege()
		{
			return _siege;
		}

		public void setSiege(Siegable s)
		{
			_siege = s;
		}

		public boolean isActiveSiege()
		{
			return _isActiveSiege;
		}

		public void setActiveSiege(boolean val)
		{
			_isActiveSiege = val;
		}

		@Override
		public void clear()
		{
			_siegableId = -1;
			_siege = null;
			_isActiveSiege = false;
		}
	}
}