package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

/**
 * A Interface describing the required methods for an Plant.
 */
public interface Plant {

    /**
     * Advances the plant to its next stage of growth.
     */
    void grow();

    /**
     * Determines whether the plant is ready for harvesting.
     */
    boolean isHarvestable();

    /**
     * Harvests the plant and returns a Product.
     */
    Product harvest(Quality quality) throws UnableToInteractException;

    /**
     * Resets the plant after harvest.
     */
    void reset();

    /**
     * Returns the symbol representing the plant's current growth stage.
     */
    String getSymbol();

    /**
     * returns the type of plants
     */
    String getType();

    /**
     * gets the stages of growth of a given plant
     */
    int getStage();
}