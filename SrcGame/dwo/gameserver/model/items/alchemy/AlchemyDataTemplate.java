package dwo.gameserver.model.items.alchemy;

import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 17.06.2015
 * Team: La2Era Team
 */
public class AlchemyDataTemplate
{
    private int _skillId;
    private int _skillLevel;
    private int _successRate;
    private List<AlchemyItem> _ingridients;
    private List<AlchemyItem> _onSuccessProducts;
    private List<AlchemyItem> _onFailProducts;

    public AlchemyDataTemplate(int skillId, int skillLevel, int successRate) {
        _ingridients = new ArrayList<>();
        _onSuccessProducts = new ArrayList<>();
        _onFailProducts = new ArrayList<>();
        _skillId = skillId;
        _skillLevel = skillLevel;
        _successRate = successRate;
    }

    public int getSkillId() {
        return _skillId;
    }

    public int getSkillLevel() {
        return _skillLevel;
    }

    public int getSuccessRate() {
        return _successRate;
    }

    public void addIngridient(AlchemyItem ingridient) {
        _ingridients.add(ingridient);
    }

    public AlchemyItem[] getIngridients() {
        return _ingridients.toArray(new AlchemyItem[_ingridients.size()]);
    }

    public void addOnSuccessProduct(AlchemyItem product) {
        _onSuccessProducts.add(product);
    }

    public AlchemyItem[] getOnSuccessProducts() {
        return _onSuccessProducts.toArray(new AlchemyItem[_onSuccessProducts.size()]);
    }

    public void addOnFailProduct(AlchemyItem product) {
        _onFailProducts.add(product);
    }

    public AlchemyItem[] getOnFailProducts() {
        return _onFailProducts.toArray(new AlchemyItem[_onFailProducts.size()]);
    }

    public static class AlchemyItem
    {
        private int _id;
        private int _count;

        public AlchemyItem(int id, int count) {
            _id = id;
            _count = count;
        }

        public int getId() {
            return _id;
        }

        public long getCount() {
            return _count;
        }
    }
}
