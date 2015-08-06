package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;

import java.util.HashMap;
import java.util.Map;

public class OrbisBox implements IItemHandler
{
	// TODO: Нужно сделать группы в ExtractableItems и перенести потом это
	private static final int[] BOXES = {
		32604, // Наградной Сундук Снабжения Орбиса
		32605, // Хороший Наградной Сундук Орбиса
		32606, // Элитный Наградной Сундук Орбиса
		32607, // Лучший Наградной Сундук Орбиса
	};

	private static final Map<Integer, Integer> REWARD_MAP = new HashMap<>();

	static
	{
		REWARD_MAP.put(BOXES[0], 0);
		REWARD_MAP.put(BOXES[1], 1);
		REWARD_MAP.put(BOXES[2], 2);
		REWARD_MAP.put(BOXES[3], 3);
	}

	/**
	 * BoxId -> Group -> ItemList -> {}ItemId, [Count, Chance]}
	 */
	private final int[][][][] REWARDS = {
		{
			{{18568, 1, 5}},      // Камень Жизни - Ранг R95
			{{19167, 1, 5}},      // Камень Жизни для Аксессуаров - Ранг R95
			{{19441, 1500, 10}},  // Заряд Духа: Ранг R
			{{17754, 2000, 9}},   // Заряд Души: Ранг R
			{{19442, 1000, 11}},  // Благословенный Заряд Духа: Ранг R
			{{30357, 3}},         // Эликсир Жизни - Ранг R
			{{30358, 3}},         // Эликсир Ментальной Силы - Ранг R
			{{30359, 3}},         // Эликсир CP - Ранг R
			{{30360}},            // Великий Эликсир Жизни - Ранг R
			{{30361}},            // Великий Эликсир Ментальной Силы - Ранг R
			{{30362}},            // Великий Эликсир CP - Ранг R
			{{32316}},            // Эликсир Благословения
		}, {
		{
			{18554, 1, 1},  // Красный Кристалл Души - Ранг R95
			{18555, 1, 1},  // Зеленый Кристалл Души - Ранг R95
			{18556, 1, 1},  // Синий Кристалл Души - Ранг R95
		}, {{18569, 1, 95}},      // Камень Жизни Среднего Качества - Ранг R95
		{{19167, 1, 95}},      // Камень Жизни для Аксессуаров - Ранг R95
		{{17527, 1, 10}},      // Свиток: Модифицировать Доспех (R)
		{{17526, 1, 8}},      // Свиток: Модифицировать Оружие (R)
		// Ресурсы
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
	}, {
		{
			{18554, 1, 1},  // Красный Кристалл Души - Ранг R95
			{18555, 1, 1},  // Зеленый Кристалл Души - Ранг R95
			{18556, 1, 1},  // Синий Кристалл Души - Ранг R95
		}, {{18569, 1, 85}},      // Камень Жизни Среднего Качества - Ранг R95
		{{19167, 1, 55}},      // Камень Жизни для Аксессуаров - Ранг R95
		{{17527, 1, 15}},      // Свиток: Модифицировать Доспех (R)
		{{17526, 1, 12}},      // Свиток: Модифицировать Оружие (R)
		{{19448, 1, 1}},       // Благословенный Свиток: Модифицировать Доспех (R)
		{{19447, 1, 1}},       // Благословенный Свиток: Модифицировать Оружие (R)
		// Ресурсы
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},

	}, {
		{
			{18554, 1, 4},  // Красный Кристалл Души - Ранг R95
			{18555, 1, 4},  // Зеленый Кристалл Души - Ранг R95
			{18556, 1, 4},  // Синий Кристалл Души - Ранг R95
		}, {
		{19511, 1, 1},  // Часть Желтого Кристалла Души - Ранг R95
		{19512, 1, 1},  // Часть Аквамаринового Кристалла Души - Ранг R95
		{19513, 1, 1},  // Часть Фиолетового Кристалла Души - Ранг R95
	}, {{18569, 1, 80}},      // Камень Жизни Среднего Качества - Ранг R95
		{{19167, 1, 70}},      // Камень Жизни для Аксессуаров - Ранг R95
		{{17527, 1, 35}},      // Свиток: Модифицировать Доспех (R)
		{{17526, 1, 30}},      // Свиток: Модифицировать Оружие (R)
		{{19448, 1, 2}},       // Благословенный Свиток: Модифицировать Доспех (R)
		{{19447, 1, 2}},       // Благословенный Свиток: Модифицировать Оружие (R)
		// Ресурсы
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},
		{{19305}, {19306}, {19307}, {19308}, {19309}, {19310}, {19311}, {19312}, {19313}, {19314}, {19315}},

	},
	};

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getActingPlayer();

		if(activeChar.getMaxLoad() > 0)
		{
			int weightproc = (activeChar.getCurrentLoad() - activeChar.getBonusWeightPenalty()) * 100 / activeChar.getMaxLoad();
			if(weightproc >= 80 || activeChar.getInventory().getSize(false) >= activeChar.getInventoryLimit() - 10)
			{
				activeChar.sendPacket(SystemMessageId.SLOTS_FULL);
				return false;
			}
		}

		//destroy item
		if(!activeChar.destroyItem(ProcessType.EXTRACTABLES, item.getObjectId(), 1, activeChar, true))
		{
			return false;
		}

		boolean created = false;

		if(REWARD_MAP.containsKey(item.getItemId()))
		{
			int[][][] rewards = REWARDS[REWARD_MAP.get(item.getItemId())];
			for(int[][] group : rewards)
			{
				int randomItem = Rnd.get(0, group.length - 1);
				int[] itemInfo = group[randomItem];
				int itemId = itemInfo[0];
				int itemCount = itemInfo.length > 1 ? itemInfo[1] : 1;
				int chance = itemInfo.length > 2 ? itemInfo[2] : 100;

				if((int) (Rnd.get() * 100.0) <= chance)
				{
					activeChar.addItem(ProcessType.EXTRACTABLES, itemId, itemCount, activeChar, true);
					created = true;
				}
			}
		}

		if(!created)
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
		}
		return true;
	}
}
