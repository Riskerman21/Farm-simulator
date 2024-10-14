package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.core.animals.Animal;
import farm.core.animals.Chicken;
import farm.core.animals.Cow;
import farm.core.animals.Sheep;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;
import java.util.Map;

/**
 * class representing an animal farm
 */
public class AnimalFarm implements ItemFarm {
    private static final Map<Character, Class<?>> SYMBOL_TO_ANIMAL = Map.of(
            '৬', Chicken.class,
            '४', Cow.class,
            'ඔ', Sheep.class
    );

    @Override
    public Object createItem(char symbol) {
        Class<?> animalClass = SYMBOL_TO_ANIMAL.get(symbol);
        if (animalClass == null) {
            return null;
        }

        try {
            return animalClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create animal from symbol: " + symbol, e);
        }
    }

    @Override
    public boolean isValidForFarm(Object item) {
        return item instanceof Animal;
    }

    @Override
    public Product harvestItem(Object item, Quality quality) throws UnableToInteractException {
        if (item instanceof Animal animal) {
            return animal.harvest(quality);
        } else {
            throw new UnableToInteractException("Nothing to harvest here!");
        }
    }

    @Override
    public List<String> getItemStats(Object item) {
        if (item instanceof Animal animal) {
            return animal.getsStats();
        } else {
            return List.of("ground", " ");
        }
    }

    @Override
    public boolean feedItem(Object item) {

        if (item instanceof Animal animal) {
            animal.feed();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void processEndOfDay(Object item) {
        if (item instanceof Animal animal) {
            animal.reset();
        }
    }

    @Override
    public String getDisplaySymbol(Object item) {
        if (item instanceof Animal animal) {
            return animal.getSymbol();
        } else {
            return "  "; //ground
        }
    }
}