package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

/**
 * Class representing an abstract animal in farm grid
 */
public abstract class AbstractAnimal implements Animal {
    private boolean fed = false;
    private boolean collected = false;

    @Override
    public void feed() {
        this.fed = true;
    }


    @Override
    public boolean isFed() {
        return fed;
    }

    @Override
    public boolean isCollected() {
        return collected;
    }

    @Override
    public Product harvest(Quality quality) throws UnableToInteractException {
        if (!isFed()) {
            throw new UnableToInteractException("The animal has not been fed today.");
        }
        if (collected) {
            throw new UnableToInteractException("The product has already been collected today.");
        }
        collected = true;
        return createProduct(quality);
    }



    @Override
    public void reset() {
        this.fed = false;
        this.collected = false;
    }

    /**
     * method for creating the specific product of the animal.
     */
    protected abstract Product createProduct(Quality quality);
}