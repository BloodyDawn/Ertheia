package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.11.12
 * Time: 19:30
 */

public class BeautyShopData extends XmlDocumentParser
{
    private static final HashMap<Integer, HashMap<Integer, BeautyShopList>> _initialList = new HashMap<>();

    private static BeautyShopData _instance;

    protected BeautyShopData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static BeautyShopData getInstance()
    {
        return _instance == null ? _instance = new BeautyShopData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _initialList.clear();
        parseFile(FilePath.BEAUTY_SHOP_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + _initialList.size() + " BeautyShopList data.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("group"))
            {
                final int styleType = Integer.parseInt(element.getAttributeValue("styleType"));

                HashMap<Integer, BeautyShopList> style_list = new HashMap<>();
                for(Element element1 : element.getChildren())
                {
                    final String name1= element1.getName();
                    if(name1.equalsIgnoreCase("style"))
                    {
                        BeautyShopList list = new BeautyShopList(element1);
                        HashMap<Integer, Integer> color_list = new HashMap<>();
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("color"))
                            {
                                final int color_id = Integer.parseInt(element2.getAttributeValue("id"));
                                final int color_price = Integer.parseInt(element2.getAttributeValue("price"));
                                color_list.put(color_id, color_price);
                            }
                        }
                        list.setColorList(color_list);
                        style_list.put(list._id, list);
                    }
                }
                _initialList.put(styleType, style_list);
            }
        }
    }

    public Map<Integer, BeautyShopList> getBeautyList(int itemType)
    {
        return _initialList.containsKey(itemType) ? _initialList.get(itemType) : null;
    }

    public BeautyShopList getBeautyById(int itemType, int _id)
    {
        if(_initialList.containsKey(itemType) && _initialList.get(itemType).containsKey(_id))
        {
            return _initialList.get(itemType).get(_id);
        }
        return null;
    }

    public class BeautyShopList
    {
        public final int _id;
        public final int _cost;
        public final int _ownCoin;
        public final int _resetCost;
        public final int _val;
        public HashMap<Integer, Integer> _colorList;

        public BeautyShopList(Element style)
        {
            _id = Integer.parseInt(style.getAttributeValue("id"));
            _cost = Integer.parseInt(style.getAttributeValue("cost"));
            _resetCost = Integer.parseInt(style.getAttributeValue("resetCost"));
            _val = Integer.parseInt(style.getAttributeValue("val"));
            _ownCoin = 0;
        }

        public void setColorList(HashMap<Integer, Integer> colorList)
        {
            _colorList = colorList;
        }
    }
}
