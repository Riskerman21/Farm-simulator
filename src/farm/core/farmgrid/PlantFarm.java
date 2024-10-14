package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.core.plants.Berry;
import farm.core.plants.CoffeePlant;
import farm.core.plants.Plant;
import farm.core.plants.Wheat;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;
import java.util.Map;

/**
 * class representing an animal farm
 */
public class PlantFarm implements ItemFarm {
    private static final Map<Character, Class<?>> SYMBOL_TO_PLANT = Map.of(
            '.', Berry.class,
            ':', CoffeePlant.class,
            'ἴ', Wheat.class
    );

    @Override
    public Object createItem(char symbol) {
        Class<?> plantClass = SYMBOL_TO_PLANT.get(symbol);
        if (plantClass == null) {
            return null;
        }

        try {
            return plantClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create plant from symbol: " + symbol, e);
        }
    }

    @Override
    public boolean isValidForFarm(Object item) {
        return item instanceof Plant;
    }

    @Override
    public Product harvestItem(Object item, Quality quality) throws UnableToInteractException {
        if (item instanceof Plant plant) {
            return plant.harvest(quality);
        } else {
            throw new UnableToInteractException("Nothing to harvest here!");
        }
    }

    @Override
    public List<String> getItemStats(Object item) {
        if (item instanceof Plant plant) {
            return plant.getsStats();
        } else {
            return List.of("ground", " ");
        }
    }

    @Override
    public boolean feedItem(Object item) {
        return false;
    }

    @Override
    public void processEndOfDay(Object item) {
        if (item instanceof Plant plant) {
            plant.grow();
        }
    }

    @Override
    public String getDisplaySymbol(Object item) {
        if (item instanceof Plant plant) {
            return plant.getSymbol() + " ";
        } else {
            return "  ";  //ground
        }
    }
}