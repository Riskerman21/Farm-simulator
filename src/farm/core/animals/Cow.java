package farm.core.animals;

import farm.inventory.product.Milk;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

/**
 * Class representing a cow animal in farm grid
 */
public class Cow extends AbstractAnimal {

    @Override
    protected Product createProduct(Quality quality) {
        return new Milk(quality);
    }

    @Override
    public String getSymbol() {
        return "४";
    }

    @Override
    public String getType() {
        return "cow";
    }
}