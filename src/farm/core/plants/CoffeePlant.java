package farm.core.plants;

import farm.inventory.product.Coffee;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;

/**
 * Class representing a coffee plant in farm grid
 */
public class CoffeePlant extends AbstractPlant {

    /**
     * constructor for the coffee plant
     */
    public CoffeePlant() {
        super(List.of(":", ";", "*", "%"));
    }

    @Override
    protected Product createProduct(Quality quality) {
        return new Coffee(quality);
    }

    @Override
    public String getType() {
        return "coffee";
    }
}