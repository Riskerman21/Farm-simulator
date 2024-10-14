package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.RandomQuality;

import java.util.*;

/**
 * Represents a grid-based farm that can contain plants or animals.
 * Implements the Grid interface to provide farming functionality.
 */
public class FarmGrid implements Grid {
    private final int rows;
    private final int columns;
    private final Object[][] grid;  // This stores both Plant and Animal objects
    private final RandomQuality randomQuality;
    private final String farmType;

    // Map symbols to Plant/Animal classes
    private static final Map<Character, Class<?>> SYMBOL_TO_ITEM = Map.of(
            '.', Berry.class,
            ':', CoffeePlant.class,
            'ἴ', Wheat.class,
            '৬', Chicken.class,
            '४', Cow.class,
            'ඔ', Sheep.class
    );

    /**
     * Constructor for the FarmGrid, creating a farm of specified type.
     * @param rows the number of rows on the grid
     * @param columns the number of columns on the grid
     * @requires rows > 0 && columns > 0
     */
    public FarmGrid(int rows, int columns, String farmType) {
        this.rows = rows;
        this.columns = columns;
        this.farmType = farmType;
        this.grid = new Object[rows][columns];
        this.randomQuality = new RandomQuality();
    }

    @Override
    public boolean place(int row, int column, char symbol) throws IllegalStateException {
        if (!isValidPosition(row, column)) {
            return false;
        }

        if (grid[row][column] != null) {
            throw new IllegalStateException("Something is already there!");
        }

        Object newItem = createItemFromSymbol(symbol);
        if (newItem == null) {
            return false;
        }

        grid[row][column] = newItem;
        return true;
    }

    /**
     * creates an instance of a valid animal/plant on the grid
     * @param symbol The symbol of a valid animal/plant
     * @return the created object
     */
    private Object createItemFromSymbol(char symbol) {
        Class<?> itemClass = SYMBOL_TO_ITEM.get(symbol);
        if (itemClass == null) {
            return null;
        }

        try {
            return itemClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create item from symbol.", e);
        }
    }

    @Override
    public Product harvest(int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("You can't harvest this location");
        }

        Object item = grid[row][column];
        if (item instanceof Plant plant) {
            if (plant.isHarvestable()) {
                Product product = plant.harvest(randomQuality.getRandomQuality());
                plant.reset();
                return product;
            } else {
                throw new UnableToInteractException("The crop is not fully grown!");
            }
        } else if (item instanceof Animal animal) {
            if (animal.isFed()) {
                Product product = animal.harvest(randomQuality.getRandomQuality());
                return product;
            } else {
                throw new UnableToInteractException("Animal is not fed.");
            }
        } else {
            throw new UnableToInteractException("Nothing to harvest here!");
        }
    }

    @Override
    public List<List<String>> getStats() {
        List<List<String>> stats = new ArrayList<>(rows * columns);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                stats.add(getItemStats(item));
            }
        }

        return stats;
    }

    private List<String> getItemStats(Object item) {
        if (item instanceof Plant plant) {
            return List.of(
                    plant.getType(),
                    plant.getSymbol(),
                    "Stage: " + plant.getStage()
            );
        } else if (item instanceof Animal animal) {
            return List.of(
                    animal.getType(),
                    animal.getSymbol(),
                    "Fed: " + animal.isFed(),
                    "Collected: " + animal.isCollected()
            );
        } else {
            return List.of("ground", " ");
        }
    }

    @Override
    public boolean interact(String command, int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("Invalid position.");
        }

        Object item = grid[row][column];

        return switch (command) {
            case "feed" -> feedAnimal(row, column);
            case "end-day" -> {
                endDay();
                yield true;
            }
            case "remove" -> {
                remove(row, column);
                yield true;
            }
            default -> throw new UnableToInteractException("Unknown command: " + command);
        };
    }

    private boolean feedAnimal(int row, int column) throws UnableToInteractException {
        Object item = grid[row][column];
        if (item instanceof Animal animal) {
            animal.feed();
            return true;
        }
        return false;
    }

    private void endDay() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                if (item instanceof Plant) {
                    ((Plant) item).grow();
                } else if (item instanceof Animal) {
                    ((Animal) item).reset();
                }
            }
        }
    }

    private void remove(int row, int column) {
        if (isValidPosition(row, column)) {
            grid[row][column] = null;
        }
    }

    @Override
    public String farmDisplay() {
        StringBuilder display = new StringBuilder("-".repeat((columns * 2) + 3)).append('\n');
        for (int i = 0; i < rows; i++) {
            display.append("| ");
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                if (item instanceof Plant) {
                    display.append(((Plant) item).getSymbol()).append(' ');
                } else if (item instanceof Animal) {
                    display.append(((Animal) item).getSymbol()).append(' ');
                } else {
                    display.append("  ");
                }
            }
            display.append("|\n");
        }
        return display.append("-".repeat((columns * 2) + 3)).append('\n').toString();
    }



    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < this.rows && column >= 0 && column < this.columns;
    }
}