package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuest;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Yorie, Bacek
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class ExDynamicQuest extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final boolean _isCampaign;
	private final int _campainId;
	private final int _step;
	private Status _status;
	// Прогресс
	private UpdateAction _action;
	private int _remainingTime;
	private int _currentTask;
	private int _currentCount;
	private int _totalCount;
	private List<DynamicQuest.DynamicQuestResult> _participiants;

	public ExDynamicQuest(L2PcInstance player, boolean isCampain, int campainId, int step)
	{
		_player = player;
		_isCampaign = isCampain;
		_campainId = campainId;
		_step = step;
	}

	/**
	 * Начать кампанию.
	 * @return Для удобства возвращаем себя.
	 */
	public ExDynamicQuest startCampain()
	{
		_status = Status.CAMPAIN_START;
		return this;
	}

	/**
	 * Метод для отправки прогресса кампании.
	 *
	 * @param action Тип действия.
	 *                  Может быть:
	 *                  - 0, кампания в процессе;
	 *                  - 1, получить награду;
	 *                  - 2, Просмотреть результат;
	 *                  - 3, неудачная кампания.
	 * @param remainingTime Оставлееся время.
	 * @param currentTask ID текущего задания.
	 * @param currentCount Текущий прогресс кампании.
	 * @param totalCount Необходимое количество для успешного завершения кампании.
	 * @return Для удобства возвращаем себя.
	 */
	public ExDynamicQuest setProgressUpdate(UpdateAction action, int remainingTime, int currentTask, int currentCount, int totalCount)
	{
		_action = action;
		_status = Status.CAMPAIN_PROGRESS;
		_remainingTime = remainingTime;
		_currentTask = currentTask;
		_currentCount = currentCount;
		_totalCount = totalCount;
		return this;
	}

	/**
	 * Обновление таблицы результатов кампании.
	 *
	 * @param remainingTime Оставшееся время.
	 * @return Для удобства возвращаем себя.
	 */
	public ExDynamicQuest setResultsUpdate(List<DynamicQuest.DynamicQuestResult> participiants, int remainingTime)
	{
		_participiants = participiants;
		_status = Status.CAMPAIN_STATISTICS;
		_remainingTime = remainingTime;
		return this;
	}

	/**
	 * Завершение кампании.
	 * @return Для удобства возвращаем себя.
	 */
	public ExDynamicQuest endCampain()
	{
		_status = Status.CAMPAIN_END;
		return this;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_isCampaign ? 0 : 1);     // 0 компания  1  квест зона
		writeC(_status.ordinal());                 // 1 CampaignFinish
		writeD(_campainId);                     // номер компании в DynamicContentsName-ru.txt
		writeD(_step);                     // этап компании

		if(_status.ordinal() == 2)                   // обновление прогресса
		{
			writeC(_action.ordinal());                   // 0 компания в процессе 1 получить награду  2 посмотреть результат  3 неудачная компания
			writeD(_remainingTime);                 // осталось время до конца компании в секундах

			if(!_isCampaign)             //  квест зона
			{
				if(!_isCampaign && _player.getParty() != null && _player.getParty() != null)
				{
					writeD(_player.getParty().getMemberCount());
				}
				else
				{
					writeD(0x01);            // число человек в пати
				}
			}

			int size = 1;                // размер массива заданий ( для орбиса 1 )
			writeD(size);
			for(int i = 0; i < size; ++i)
			{
				writeD(_currentTask);               // текущие задание берется из датки  ( для орбиса 201	u,Уничтожение Проклятых Древних Героев\0 )
				writeD(_currentCount);               // текущие количество
				writeD(_totalCount);               // необходимое количество  ( нужно убить для орбиса 42000 )
			}
		}

		if(_status.ordinal() == 3)                     // окно результатов
		{
			if(_isCampaign)
			{
				if(_participiants != null)
				{
					int size = _participiants.size();            // размер массива игроков в таблице   ( в клиенте лимит 1000 )
					writeD(size);
					for(DynamicQuest.DynamicQuestResult _participiant : _participiants)
					{
						writeS(_participiant.getParticipiant()); // Ник игрока
					}
				}
			}
			else
			{
				writeD(_remainingTime);               // сталось время до конца компании в секундах

				if(_player.getParty() == null)
				{
					writeD(0x01);               // число человек в пати
				}
				else
				{
					writeD(_player.getParty().getMemberCount());
				}

				if(_participiants != null)
				{
					int size = _participiants.size();            // размер массива игроков в таблице  ( в клиенте лимит 1000 )
					writeD(size);
					for(DynamicQuest.DynamicQuestResult _participiant : _participiants)
					{
						writeS(_participiant.getParticipiant());     // Ник игрока
						writeD(_participiant.getContributed(DynamicQuest.ContributionType.CURRENT));           // basicContributed
						writeD(_participiant.getContributed(DynamicQuest.ContributionType.ADDITIONAL));           // additionalContributed
						writeD(_participiant.getContributed(DynamicQuest.ContributionType.TOTAL));           // totalContributed
					}
				}
				else
				{
					writeD(0x00);
				}
			}
		}
	}

	public enum Status
	{
		CAMPAIN_START,
		CAMPAIN_END,
		CAMPAIN_PROGRESS,
		CAMPAIN_STATISTICS,
	}

	public enum UpdateAction
	{
		ACTION_PROGRESS,
		ACTION_GET_REWARD,
		ACTION_VIEW_RESULT,
		ACTION_FAIL,
	}
}
