package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.ServerObjectInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.Rnd;

import java.util.Collection;

public class L2WaterZone extends L2ZoneType
{
	private static final AbnormalEffect[] _ae = new AbnormalEffect[]
            {
                AbnormalEffect.CHANGE_SWIMSUIT_A,
                AbnormalEffect.CHANGE_SWIMSUIT_B
            };

    public L2WaterZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, true);

        character.startAbnormalEffect(_ae[Rnd.get(_ae.length)]);

		if(character instanceof L2PcInstance)
		{
			if(character.isTransformed() && !((L2PcInstance) character).isCursedWeaponEquipped())
			{
				character.stopTransformation(true);
			}
			else
			{
				((L2PcInstance) character).broadcastUserInfo();
			}
		}
        else if (character.isNpc())
        {
            Collection<L2PcInstance> players = character.getKnownList().getKnownPlayers().values();

            for (L2PcInstance player : players)
            {
                if (character.getRunSpeed() == 0)
                {
                    player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
                }
                else
                {
                    player.sendPacket(new NpcInfo((L2Npc) character));
                }
            }
        }
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_WATER, false);

        for (AbnormalEffect ae : _ae)
        {
            character.stopAbnormalEffect(ae);
        }

		if(character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
		else if(character instanceof L2Npc)
		{
			for(L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			{
				if(character.getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) character, player));
				}
				else
				{
					player.sendPacket(new NpcInfo((L2Npc) character));
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

	public int getWaterZ()
	{
		return getZone().getHighZ();
	}
}
