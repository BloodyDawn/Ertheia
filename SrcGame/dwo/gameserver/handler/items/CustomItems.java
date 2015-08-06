package dwo.gameserver.handler.items;

import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.stat.PcStat;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;

public class CustomItems implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}
		L2PcInstance player = playable.getActingPlayer();
		switch(item.getItemId())
		{
			case 15623: // Exp
				if(player.getLevel() == 99)
				{
					player.sendMessage("Вы уже досигли максимального уровня.");
					return false;
				}
				long pXp = player.getExp();
				long tXp = ExperienceTable.getInstance().getExpForLevel(100);

				if(pXp > tXp)
				{
					player.removeExpAndSp(pXp - tXp, 0);
				}
				else if(pXp < tXp)
				{
					player.addExpAndSp(tXp - pXp, 0);
				}
				player.destroyItemByItemId(ProcessType.CONSUME, item.getItemId(), 1, player, true);
				break;
			case 15624: // SP
				if(player.getSp() == 2147483647)
				{
					player.sendMessage("Ваш очки умений уже на максимуме!");
					return false;
				}
				player.getStat().setSp(2147483647);
				player.destroyItemByItemId(ProcessType.CONSUME, item.getItemId(), 1, player, true);
				break;
			case 15626: // Vitality
				if(player.getStat().getVitalityPoints() == PcStat.MAX_VITALITY_POINTS)
				{
					player.sendMessage("Ваша жизненная энергия и так полна!");
					return false;
				}
				if(player.getVitalityDataForCurrentClassIndex().getVitalityItems() > 0)
				{
					player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS);
					player.broadcastUserInfo();
					player.destroyItemByItemId(ProcessType.CONSUME, item.getItemId(), 1, player, true);
					player.decreaseVitalityItemsLeft();
				}
				else
				{
					player.sendMessage("Лимит достпуных для использования предметов жизненной энергии исчерпан."); // TODO: сообщение из клиента
					return false;
				}
				break;
			case 15627: // Clan Point
				if(player.getClan() == null)
				{
					player.sendMessage("Чтобы использовать этот предмет Вы должны состоять в клане.");
					return false;
				}
				player.getClan().addReputationScore(10000, true);
				player.destroyItemByItemId(ProcessType.CONSUME, item.getItemId(), 1, player, true);
				break;
			case 15633: // PK Reduction
				if(player.getPkKills() == 0)
				{
					player.sendMessage("Ваш счетчик ПК равен нулю.");
					return false;
				}
				player.setPkKills(0);
				player.broadcastUserInfo();
				player.destroyItemByItemId(ProcessType.CONSUME, item.getItemId(), 1, player, true);
				break;
		}
		return true;
	}
}