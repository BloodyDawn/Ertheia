package dwo.gameserver.model.items;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EnchantEffectTable
{
	private static final int[][] SHIRT_MAP = {
		{24949, 0, 0}, {24805, 0, 0}, {24806, 0, 0}, {24807, 0, 0}, {24808, 24855, 24902}, {24809, 24856, 24903},
		{24810, 24857, 24904}, {24811, 24858, 24905}, {24812, 24859, 24906}, {24813, 24860, 24907},
		{24814, 24861, 24908}, {24815, 24862, 24909}, {24816, 24863, 24910}, {24817, 24864, 24911},
		{24818, 24865, 24912}, {24819, 24866, 24913}, {24820, 24867, 24914}, {24821, 24868, 24915},
		{24822, 24869, 24916}, {24823, 24870, 24917}, {24824, 24871, 24918}, {24825, 24872, 24919},
		{24826, 24873, 24920}, {24827, 24874, 24921}, {24828, 24875, 24922}, {24829, 24876, 24923},
		{24830, 24877, 24924}, {24831, 24878, 24925}, {24832, 24879, 24926}, {24833, 24880, 24927},
		{24834, 24881, 24928}, {24835, 24882, 24929}, {24836, 24883, 24930}, {24837, 24884, 24931},
		{24838, 24885, 24932}, {24839, 24886, 24933}, {24840, 24887, 24934}, {24841, 24888, 24935},
		{24842, 24889, 24935}, {24843, 24890, 24937}, {24844, 24891, 24938}, {24845, 24892, 24939},
		{24846, 24893, 24940}, {24847, 24894, 24941}, {24848, 24895, 24942}, {24849, 24896, 24943},
		{24850, 24897, 24944}, {24851, 24898, 24945}, {24852, 24899, 24946}, {24853, 24900, 24947},
		{24854, 24901, 24948}
	};
	// Description of scroll say p.def, m.def
	// not sure how is handled vitality effect since we don't have that item
	// in freya files probably is H5 item
	private static final int[][] VITALITY_BELT_MAP = {
		{24949, 0, 0}, {24805, 0, 0}, {24806, 0, 0}, {24807, 0, 0}, {24808, 0, 0}, {24809, 0, 0}, {24810, 0, 0},
		{24811, 0, 0}, {24812, 0, 0}, {24813, 0, 0}, {24814, 0, 0}, {24815, 0, 0}, {24816, 0, 0}, {24817, 0, 0},
		{24818, 0, 0}, {24819, 0, 0}, {24820, 0, 0}, {24821, 0, 0}, {24822, 0, 0}, {24823, 0, 0}, {24824, 0, 0},
		{24825, 0, 0}, {24826, 0, 0}, {24827, 0, 0}, {24828, 0, 0}, {24829, 0, 0}, {24830, 0, 0}, {24831, 0, 0},
		{24832, 0, 0}, {24833, 0, 0}, {24834, 0, 0}, {24835, 0, 0}, {24836, 0, 0}, {24837, 0, 0}, {24838, 0, 0},
		{24839, 0, 0}, {24840, 0, 0}, {24841, 0, 0}, {24842, 0, 0}, {24843, 0, 0}, {24844, 0, 0}, {24845, 0, 0},
		{24846, 0, 0}, {24847, 0, 0}, {24848, 0, 0}, {24849, 0, 0}, {24850, 0, 0}, {24851, 0, 0}, {24852, 0, 0},
		{24853, 0, 0}, {24854, 0, 0}
	};
	// Based on:
	// http://translate.google.com/translate?sl=auto&tl=en&js=n&prev=_t&hl=ru&ie=UTF-8&layout=2&eotf=1&u=http%3A%2F%2Fl2wiki.info%2F%D0%A4%D1%83%D1%82%D0%B1%D0%BE%D0%BB%D0%BA%D0%B0_%D0%9E%D0%BB%D1%8C%D1%84%D0%B0
	// and client files
	private static final int[][] OLF_SHIRT = {
		{24965, 0, 0}, {24966, 0, 0}, {24967, 0, 0}, {24968, 0, 0}, {24969, 24975, 0}, {24970, 24976, 0},
		{24971, 24977, 0}, {24972, 24978, 24982}, {24973, 24979, 24982}, {24974, 24980, 24982}, {24984, 24985, 24982},
	};
	private static TIntObjectHashMap<int[][]> _enchantEffectTable;

	private EnchantEffectTable()
	{
		_enchantEffectTable = new TIntObjectHashMap<>();

		load();
	}

	public static EnchantEffectTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		// Weaver's Multi-colored Clothes
		_enchantEffectTable.put(15383, SHIRT_MAP);
		_enchantEffectTable.put(15384, SHIRT_MAP);
		_enchantEffectTable.put(15385, SHIRT_MAP);
		_enchantEffectTable.put(15386, SHIRT_MAP);
		_enchantEffectTable.put(15387, SHIRT_MAP);
		_enchantEffectTable.put(15388, SHIRT_MAP);
		_enchantEffectTable.put(15389, SHIRT_MAP);
		_enchantEffectTable.put(15390, SHIRT_MAP);
		_enchantEffectTable.put(15391, SHIRT_MAP);
		_enchantEffectTable.put(15392, SHIRT_MAP);
		// Christmas Shirt 24-hour limited period
		_enchantEffectTable.put(20759, SHIRT_MAP);
		// Vitality Belt
		_enchantEffectTable.put(15393, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15394, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15395, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15396, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15397, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15398, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15399, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15400, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15401, VITALITY_BELT_MAP);
		_enchantEffectTable.put(15402, VITALITY_BELT_MAP);
		// Olf's T-shirt
		_enchantEffectTable.put(21580, OLF_SHIRT);
		_enchantEffectTable.put(21706, OLF_SHIRT);
	}

	public int[] getEnchantEffect(L2ItemInstance item)
	{
		if(_enchantEffectTable.contains(item.getItemId()))
		{
			try
			{
				int[][] _table = _enchantEffectTable.get(item.getItemId());
				int[] _result = _table[item.getEnchantLevel()];
				if(_result != null && _result.length == 3)
				{
					return _result;
				}
			}
			catch(Exception e)
			{
			}
		}
		return new int[]{0, 0, 0};
	}

	private static class SingletonHolder
	{
		protected static final EnchantEffectTable _instance = new EnchantEffectTable();
	}
}