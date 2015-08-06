package dwo.scripts.npc.town;

import dwo.gameserver.datatables.xml.RaidRadarTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.RaidRadarHolder;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowQuestInfo;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.10.12
 * Time: 20:49
 */

public class AdventurerGuildsman extends Quest
{
	private static final int[] NPCs = {
		31729, 31730, 31731, 31732, 31733, 31734, 31735, 31736, 31737, 31738, 31739, 31740, 31741, 31742, 31743, 31744,
		31745, 31746, 31747, 31748, 31749, 31750, 31751, 31752, 31753, 31754, 31755, 31756, 31757, 31758, 31759, 31760,
		31761, 31762, 31763, 31764, 31765, 31766, 31767, 31768, 31769, 31770, 31771, 31772, 31773, 31774, 31775, 31776,
		31777, 31778, 31779, 31780, 31781, 31782, 31783, 31784, 31785, 31786, 31787, 31788, 31789, 31790, 31791, 31792,
		31793, 31794, 31795, 31796, 31797, 31798, 31799, 31800, 31801, 31802, 31803, 31804, 31805, 31806, 31807, 31808,
		31809, 31810, 31811, 31812, 31813, 31814, 31815, 31816, 31817, 31818, 31819, 31820, 31821, 31822, 31823, 31824,
		31825, 31826, 31827, 31828, 31829, 31830, 31831, 31832, 31833, 31834, 31835, 31836, 31837, 31838, 31839, 31840,
		31841, 32337, 32338, 32339, 32340
	};

	public AdventurerGuildsman()
	{
		addAskId(NPCs, -18);
	}

	public static void main(String[] args)
	{
		new AdventurerGuildsman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		long groupId = -1;
		switch(reply)
		{
			case 1: // Список квестов
				player.sendPacket(new ExShowQuestInfo());
				break;
			// TODO: ID Групп
			case 2: // Рейдбоссы 20-29
				break;
			case 3: // Рейдбоссы 30-39
				break;
			case 4: // Рейдбоссы 40-49
				break;
			case 5: // Рейдбоссы 50-59
				break;
			case 6: // Рейдбоссы 60-69
				break;
			case 7: // Рейдбоссы 70-79
				break;
			case 8: // Рейдбоссы 80-89
				break;
			case 9: // Рейдбоссы 90-99
				break;
		}
		if(groupId > 0)
		{
			List<RaidRadarHolder> radarList = RaidRadarTable.getInstance().getRadarGroup(groupId);
			StringBuilder content = new StringBuilder();
			content.append("<html><body><br><br>");
			int indexId = 0;
			for(RaidRadarHolder holder : radarList)
			{
				content.append("<a action=\"bypass -h show_radar?id=").append(groupId).append("&index=").append(indexId).append("\"><fstring>").append(holder.getFstring()).append("</fstring></a><br1>");
				indexId++;
			}
			content.append("<br></body></html>");
			return content.toString();
		}
		return null;
	}
}