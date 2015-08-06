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
package dwo.scripts.npc.teleporter;

import dwo.config.Config;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2BossZone;

public class SteelCitadelTeleport extends Quest
{
	private static final int BELETH = 29118;
	private static final int NAIA_CUBE = 32376;

	public SteelCitadelTeleport()
	{

		addAskId(NAIA_CUBE, -415);

	}

	public static void main(String[] args)
	{
		new SteelCitadelTeleport();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -415:
				if(reply == 1)
				{
					if(GrandBossManager.getInstance().getBossStatus(BELETH) == 3)
					{
						return "cube_for_naia002.htm";
					}

					L2CommandChannel channel = player.getParty() == null ? null : player.getParty().getCommandChannel();

					if(channel == null || channel.getLeader().getObjectId() != player.getObjectId() || channel.getMemberCount() < Config.BELETH_MIN_PLAYERS)
					{
						return "cube_for_naia002a.htm";
					}

					if(GrandBossManager.getInstance().getBossStatus(BELETH) > 0)
					{
						return "cube_for_naia003.htm";
					}

					L2BossZone zone = (L2BossZone) ZoneManager.getInstance().getZoneById(12018);
					if(zone != null)
					{
						GrandBossManager.getInstance().setBossStatus(BELETH, 1);

						for(L2Party party : channel.getPartys())
						{
							if(party == null)
							{
								continue;
							}

							party.getMembers().stream().filter(pl -> pl.isInsideRadius(npc.getX(), npc.getY(), npc.getZ(), 3000, true, false)).forEach(pl -> {
								zone.allowPlayerEntry(pl, 30);
								pl.teleToLocation(16342, 209557, -9352, true);
							});
						}
					}
				}
				break;
		}
		return null;
	}
}
