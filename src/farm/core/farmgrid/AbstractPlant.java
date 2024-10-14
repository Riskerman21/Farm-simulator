package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;

/**
 * Class representing an abstract plant in farm grid
 */
public abstract class AbstractPlant implements Plant {
    private List<String> growthStages;
    private int currentStage;
    private boolean fed = false;
    private boolean collected = false;
    private boolean harvested = false;

    /**
     * constructor for an abstract plant
     */
    public AbstractPlant(List<String> growthStages) {
        this.growthStages = growthStages;
        this.currentStage = 0;
    }

    @Override
    public int getStage() {
        if (harvested) {
            return currentStage;
        } else {
            return currentStage + 1;
        }
    }

    @Override
    public void grow() {
        if (currentStage < growthStages.size() - 1) {
            currentStage++;
            harvested = false;
        }
    }

    @Override
    public boolean isHarvestable() {
        return currentStage == growthStages.size() - 1;
    }

    @Override
    public Product harvest(Quality quality) throws UnableToInteractException {
        if (!isHarvestable()) {
            throw new UnableToInteractException("The plant is not ready for harvest.");
        }
        harvested = true;
        return createProduct(quality);
    }

    @Override
    public void reset() {
        this.currentStage = 0;
    }

    /**
     * get the symbol representing the plant on the grid
     */
    public String getSymbol() {
        return growthStages.get(currentStage);
    }

    protected abstract Product createProduct(Quality quality);
}