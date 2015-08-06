package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.base.funcs.LambdaConst;
import dwo.gameserver.model.skills.stats.Stats;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class EnchantBonusData extends XmlDocumentParser
{
    private static final Map<CrystalGrade, Integer[]> _armorHPBonus = new HashMap<>();

    private static final float fullArmorModifier = 1.5f;

    protected static EnchantBonusData _instance;

    private EnchantBonusData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static EnchantBonusData getInstance()
    {
        return _instance == null ? _instance = new EnchantBonusData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _armorHPBonus.clear();
        parseFile(FilePath.ENCHANT_ARMOR_HP_BONUS_DATA);
        _log.log(Level.INFO, "Enchant HP Bonus loaded for " + _armorHPBonus.size() + " Crystal Types.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("enchantHP"))
            {
                CrystalGrade grade;
                String att;
                att = element.getAttributeValue("grade");
                if(att == null)
                {
                    _log.log(Level.ERROR, "[EnchantArmorHPBonusData] Missing grade, skipping");
                    continue;
                }
                grade = CrystalGrade.valueOf(att);

                att = element.getAttributeValue("values");
                if(att == null)
                {
                    _log.log(Level.ERROR, "[EnchantArmorHPBonusData] Missing bonus id: " + grade + ", skipping");
                    continue;
                }
                StringTokenizer st = new StringTokenizer(att, ",");
                int tokenCount = st.countTokens();
                Integer[] bonus = new Integer[tokenCount];
                for(int i = 0; i < tokenCount; i++)
                {
                    Integer value = Integer.decode(st.nextToken().trim());
                    if(value == null)
                    {
                        _log.log(Level.ERROR, "[EnchantArmorHPBonusData] Bad Hp value!! grade: " + grade + " token: " + i);
                        value = 0;
                    }
                    bonus[i] = value;
                }
                _armorHPBonus.put(grade, bonus);
            }
        }

        if(_armorHPBonus.isEmpty())
        {
            return;
        }

        Set<Integer> itemIds = ItemTable.getInstance().getAllArmorsId();

        for(Integer itemId : itemIds)
        {
            L2Item item = ItemTable.getInstance().getTemplate(itemId);
            if(item != null && item.getCrystalType() != CrystalGrade.NONE)
            {
                FuncTemplate func;
                long i = item.getBodyPart();
                if(i == L2Item.SLOT_FEET)
                {
                    if(item.getItemGradeRPlus() == CrystalGrade.R)
                    {
                        func = new FuncTemplate(null, null, "EnchantRunSpd", Stats.RUN_SPEED, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }
                    else
                    {
                        func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }

                }
                else if(i == L2Item.SLOT_GLOVES)
                {
                    if(item.getItemGradeRPlus() == CrystalGrade.R)
                    {
                        func = new FuncTemplate(null, null, "EnchantAccEvas", Stats.ACCURACY_PHYSICAL, 0x60, new LambdaConst(0));
                        item.attach(func);
                        func = new FuncTemplate(null, null, "EnchantAccEvas", Stats.ACCURACY_MAGICAL, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }
                    else
                    {
                        func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }

                }
                else if(i == L2Item.SLOT_HEAD)
                {
                    if(item.getItemGradeRPlus() == CrystalGrade.R)
                    {
                        func = new FuncTemplate(null, null, "EnchantAccEvas", Stats.EVASION_PHYSICAL_RATE, 0x60, new LambdaConst(0));
                        item.attach(func);
                        func = new FuncTemplate(null, null, "EnchantAccEvas", Stats.EVASION_MAGICAL_RATE, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }
                    else
                    {
                        func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }

                }
                else if(i == L2Item.SLOT_CHEST)
                {
                    if(item.getItemGradeRPlus() == CrystalGrade.R)
                    {
                        func = new FuncTemplate(null, null, "EnchantPAtk", Stats.POWER_ATTACK, 0x60, new LambdaConst(0));
                        item.attach(func);
                        func = new FuncTemplate(null, null, "EnchantMAtk", Stats.MAGIC_ATTACK, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }
                    else
                    {
                        func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }

                }
                else if(i == L2Item.SLOT_LEGS)
                {
                    if(item.getItemGradeRPlus() == CrystalGrade.R)
                    {
                        func = new FuncTemplate(null, null, "EnchantPMcritAtk", Stats.CRITICAL_DAMAGE_ADD, 0x60, new LambdaConst(0));
                        item.attach(func);
                        func = new FuncTemplate(null, null, "EnchantPMcritAtk", Stats.MAGIC_CRIT_DMG_ADD, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }
                    else
                    {
                        func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                        item.attach(func);
                    }

                }
                else if(i == L2Item.SLOT_BACK || i == L2Item.SLOT_FULL_ARMOR || i == L2Item.SLOT_UNDERWEAR || i == L2Item.SLOT_L_HAND)
                {
                    func = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                    item.attach(func);

                }
            }
        }

        // shields in the weapons table
        itemIds = ItemTable.getInstance().getAllWeaponsId();
        for(Integer itemId : itemIds)
        {
            L2Item item = ItemTable.getInstance().getTemplate(itemId);
            if(item != null && item.getCrystalType() != CrystalGrade.NONE)
            {
                long i = item.getBodyPart();
                if(i == L2Item.SLOT_L_HAND)
                {
                    FuncTemplate ft = new FuncTemplate(null, null, "EnchantHp", Stats.MAX_HP, 0x60, new LambdaConst(0));
                    item.attach(ft);

                }
            }
        }
    }

    public int getHPBonus(L2ItemInstance item)
    {
        Integer[] values = _armorHPBonus.get(item.getItem().getItemGradeSPlus());

        if(values == null || values.length == 0)
        {
            return 0;
        }

        float blessedArmorBonus = item.isBlessedItem() ? Config.ARMOR_BLESSED_ENCHANT_BONUS : 1.0F;

        return item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR ? (int) (values[Math.min(item.getEnchantLevel(), values.length) - 1] * fullArmorModifier * blessedArmorBonus) : (int) (values[Math.min(item.getEnchantLevel(), values.length) - 1] * blessedArmorBonus);
    }
}