package farm.core.farmgrid;

import farm.inventory.product.Bread;
import farm.inventory.product.Jam;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;

/**
 * Class representing a wheat plant in farm grid
 */
public class Wheat extends AbstractPlant {

    /**
     * constructor for the wheat plant
     */
    public Wheat() {
        super(List.of("ἴ", "#"));
    }

    @Override
    protected Product createProduct(Quality quality) {
        return new Bread(quality);
    }

    @Override
    public String getType() {
        return "wheat";
    }
}