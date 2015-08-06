package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.WorldStatisticStatueHolder;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.network.game.serverpackets.packet.statistic.ExLoadStatHotLink;

import java.util.List;

public class L2WorldStatueInstance extends L2Npc
{
	private final List<ClassId> _classesToTeach;

	public L2WorldStatueInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
		_classesToTeach = template.getTeachInfo();
	}

	public WorldStatisticStatueHolder getStatueTemplate()
	{
		return (WorldStatisticStatueHolder) getTemplate();
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(new ExLoadStatHotLink(getStatueTemplate().getCategory()));
	}

	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
	}

	@Override
	public void showChatWindow(L2PcInstance player, String filename)
	{
	}
}

