package farm.core.farmgrid;

import farm.inventory.product.Egg;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

/**
 * Class representing a chicken animal in farm grid
 */
public class Chicken extends AbstractAnimal {

    @Override
    protected Product createProduct(Quality quality) {
        return new Egg(quality);
    }

    @Override
    public String getSymbol() {
        return "৬";
    }

    @Override
    public String getType() {
        return "chicken";
    }

}