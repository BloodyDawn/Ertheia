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

package dwo.gameserver.model.actor;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;

public abstract class L2Decoy extends L2Attackable
{
	private L2PcInstance _owner;

	protected L2Decoy(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		_owner = owner;
		setXYZ(owner.getX(), owner.getY(), owner.getZ(), false);
		setIsInvul(false);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
        if (isClone()) {
            sendPacket(new NpcInfo(this));
            setIsRunning(true);
        }
        else
        {
            sendPacket(new CI(this));
        }
	}

	@Override
	public boolean isAttackable()
	{
		return false;
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	public void unSummon(L2PcInstance owner)
	{
		synchronized(this)
		{

			if(isVisible() && !isDead())
			{
				if(getLocationController().getWorldRegion() != null)
				{
					getLocationController().getWorldRegion().removeFromZones(this);
				}

				owner.removeDecoy(this);
				getLocationController().decay();
				getKnownList().removeAllKnownObjects();
			}
		}
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
        if (isClone()) {
            activeChar.sendPacket(new NpcInfo(this));
        }
        else
        {
            activeChar.sendPacket(new CI(this));
        }
	}

	@Override
	public void updateAbnormalEffect()
	{
		getKnownList().getKnownPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(new CI(this)));
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean onDecay()
	{
		getLocationController().delete();
		return true;
	}

	@Override
	public boolean onDelete()
	{
		getKnownList().removeAllKnownObjects();
		_owner.removeDecoy(this);

		return true;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if(_owner != null)
		{
			_owner.sendPacket(mov);
		}
	}

	@Override
	public void sendPacket(SystemMessageId id)
	{
		if(_owner != null)
		{
			_owner.sendPacket(id);
		}
	}
}
