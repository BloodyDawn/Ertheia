package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HennaTreeTable extends XmlDocumentParser
{
    private static final Map<Integer, List<L2HennaInstance>> _hennaTrees = new HashMap<>();

    protected static HennaTreeTable _instance;

    private HennaTreeTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static HennaTreeTable getInstance()
    {
        return _instance == null ? _instance = new HennaTreeTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _hennaTrees.clear();
        parseFile(FilePath.HENNA_TREE_TABLE);
        _log.log(Level.INFO, "HennaTreeTable: Loaded " + _hennaTrees.size() + " Henna Tree Templates.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        List<L2HennaInstance> list;
        int classId;
        String[] symbol_id;

        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("henna"))
            {
                list = new FastList<>();
                classId = Integer.parseInt(element.getAttributeValue("class_id"));
                symbol_id = element.getAttributeValue("symbol_id").split(",");

                for(String symbol : symbol_id)
                {
                    int id = Integer.parseInt(symbol);
                    L2Henna template = HennaTable.getInstance().getTemplate(id);
                    if(template == null)
                    {
                        return;
                    }

                    L2HennaInstance temp = new L2HennaInstance(template);
                    temp.setSymbolId(id);
                    temp.setItemIdDye(template.getDyeId());
                    temp.setAmountDyeRequire(template.getAmountDyeRequire());
                    temp.setPrice(template.getPrice());
                    temp.setStatINT(template.getStatINT());
                    temp.setStatSTR(template.getStatSTR());
                    temp.setStatCON(template.getStatCON());
                    temp.setStatMEN(template.getStatMEN());
                    temp.setStatDEX(template.getStatDEX());
                    temp.setStatWIT(template.getStatWIT());
                    temp.setStatLUC(template.getStatLUC());
                    temp.setStatCHA(template.getStatCHA());
                    temp.setAttrSkill(template.getAttrSkill());
                    list.add(temp);
                }
                _hennaTrees.put(classId, list);
            }
        }
    }

    public L2HennaInstance[] getAvailableHenna(int classId)
    {
        List<L2HennaInstance> result = new FastList<>();
        List<L2HennaInstance> henna = _hennaTrees.get(classId);
        if(henna == null)
        {
            // the hennatree for this class is undefined, so we give an empty list
            _log.log(Level.WARN, "Hennatree for class " + classId + " is not defined !");
            return new L2HennaInstance[0];
        }

        result.addAll(henna.stream().collect(Collectors.toList()));

        return result.toArray(new L2HennaInstance[result.size()]);
    }

    public List<Integer> getAvialableHennaIds(int classId)
    {
        List<Integer> result = new FastList<>();
        List<L2HennaInstance> hennas = _hennaTrees.get(classId);
        result.addAll(hennas.stream().map(L2HennaInstance::getSymbolId).collect(Collectors.toList()));
        return result;
    }
}
