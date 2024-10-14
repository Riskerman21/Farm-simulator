package farm.core.farmgrid;

import farm.inventory.product.Jam;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;

/**
 * Class representing a berry plant in farm grid
 */
public class Berry extends AbstractPlant {

    /**
     * constructor for the berry plant
     */
    public Berry() {
        super(List.of(".", "o", "@"));
    }

    @Override
    protected Product createProduct(Quality quality) {
        return new Jam(quality);
    }

    @Override
    public String getType() {
        return "berry";
    }


}