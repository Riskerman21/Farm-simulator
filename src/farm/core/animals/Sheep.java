package farm.core.animals;

import farm.inventory.product.Product;
import farm.inventory.product.Wool;
import farm.inventory.product.data.Quality;

/**
 * Class representing a sheep animal in farm grid
 */
public class Sheep extends AbstractAnimal {

    @Override
    protected Product createProduct(Quality quality) {
        return new Wool(quality);
    }

    @Override
    public String getSymbol() {
        return "ඔ";
    }

    @Override
    public String getType() {
        return "sheep";
    }
}