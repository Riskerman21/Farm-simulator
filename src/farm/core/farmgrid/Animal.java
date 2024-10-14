package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

/**
 * A Interface describing the required methods for an Animal.
 */
public interface Animal {

    /**
     * Feeds the animal.
     */
    void feed();

    /**
     * Checks whether the animal has been fed that day or not.
     */
    boolean isFed();

    /**
     * Harvests the animal product (e.g., milk, wool, eggs) if possible.
     */
    Product harvest(Quality quality) throws UnableToInteractException;

    /**
     * Resets the animal's fed and collected status at the end of the day.
     */
    void reset();

    /**
     * The symbol representing the animal's.
     */
    String getSymbol();

    /**
     * Checks whether the animal has been collected that day or not.
     */
    boolean isCollected();

    /**
     * returns the type of animal
     */
    String getType();

}