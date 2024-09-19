package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.*;
import farm.inventory.product.data.Quality;
import farm.inventory.product.data.RandomQuality;

import java.util.*;

/**
 * Represents a grid-based farm that can contain plants or animals.
 * Implements the Grid interface to provide farming functionality.
 */
public class FarmGrid implements Grid {
    private final List<GridItem> farmState;
    private final int rows, columns;
    private final String farmType;
    private final RandomQuality randomQuality;

    private static final Map<Character, String> SYMBOL_TO_ITEM = Map.of(
            '.', "berry", ':', "coffee", 'ἴ', "wheat",
            '৬', "chicken", '४', "cow", 'ඔ', "sheep"
    );

    private static final Map<String, List<String>> GROWTH_STAGES = Map.of(
            "berry", List.of(".", "o", "@"),
            "coffee", List.of(":", ";", "*", "%"),
            "wheat", List.of("ἴ", "#")
    );

    /**
     * Constructs a FarmGrid with specified dimensions and type.
     *
     * @param rows     the number of rows in the grid
     * @param columns  the number of columns in the grid
     * @param farmType the type of farm ("plant" or "animal")
     */
    public FarmGrid(int rows, int columns, String farmType) {
        this.rows = rows;
        this.columns = columns;
        this.farmType = farmType;
        this.farmState = new ArrayList<>(rows * columns);
        this.randomQuality = new RandomQuality();
        for (int i = 0; i < rows * columns; i++) {
            farmState.add(new GridItem("ground", " ", new HashMap<>()));
        }
    }

    /**
     * Constructs a plant-type FarmGrid with specified dimensions.
     *
     * @param rows    the number of rows in the grid
     * @param columns the number of columns in the grid
     */
    public FarmGrid(int rows, int columns) {
        this(rows, columns, "plant");
    }

    /**
     * Places an item on the grid at the specified position.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @param symbol the character symbol representing the item to place
     * @return true if the item was successfully placed, false otherwise
     * @throws IllegalStateException if the position is already occupied
     * @throws IllegalArgumentException if the item type is invalid for the farm type
     */
    @Override
    public boolean place(int row, int column, char symbol) {
        if (!isValidPosition(row, column)) return false;
        String itemName = SYMBOL_TO_ITEM.get(symbol);
        if (itemName == null) return false;
        int index = getIndex(row, column);
        if (!farmState.get(index).symbol.equals(" ")) {
            throw new IllegalStateException("Something is already there!");
        }
        GridItem newItem = getGridItem(symbol, itemName);
        farmState.set(index, newItem);
        return true;
    }

    /**
     * Creates a new GridItem based on the provided symbol and item name.
     *
     * @param symbol The symbol representing the item on the grid.
     * @param itemName The name of the item to be created.
     * @return A GridItem object configured based on the item type and farm type.
     * @throws IllegalArgumentException If the item type does not match the farm type.
     */
    private GridItem getGridItem(char symbol, String itemName) {
        boolean isAnimal = itemName.equals("chicken") || itemName.equals("cow") || itemName.equals("sheep");
        if ((farmType.equals("animal") && !isAnimal) || (farmType.equals("plant") && isAnimal)) {
            throw new IllegalArgumentException("Invalid item for this farm type!");
        }
        Map<String, String> properties;
        if (isAnimal) {
            properties = Map.of("Fed", "false", "Collected", "false");
        } else {
            properties = Map.of("Stage", "1");
        }
        return new GridItem(itemName, String.valueOf(symbol), properties);
    }


    /**
     * Harvests the item at the specified position.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @return the harvested Product
     * @throws UnableToInteractException if the harvest cannot be performed
     */
    @Override
    public Product harvest(int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("Invalid position");
        }
        GridItem item = farmState.get(getIndex(row, column));
        if (item.type.equals("ground")) {
            throw new UnableToInteractException("Can't harvest an empty spot!");
        }
        if (farmType.equals("animal")) {
            return harvestAnimal(item);
        } else {
            return harvestPlant(item);
        }
    }

    /**
     * Harvests an animal product.
     *
     * @param animal the GridItem representing the animal
     * @return the harvested Product
     * @throws UnableToInteractException if the animal cannot be harvested
     */
    private Product harvestAnimal(GridItem animal) throws UnableToInteractException {
        if (!animal.attributes.get("Fed").equals("true")) {
            throw new UnableToInteractException("Animal not fed today!");
        }
        if (animal.attributes.get("Collected").equals("true")) {
            throw new UnableToInteractException("Already collected today!");
        }
        animal.attributes.put("Collected", "true");
        Quality quality = randomQuality.getRandomQuality();
        return switch (animal.type) {
            case "cow" -> new Milk(quality);
            case "chicken" -> new Egg(quality);
            case "sheep" -> new Wool(quality);
            default -> throw new UnableToInteractException("Unknown animal type");
        };
    }

    /**
     * Harvests a plant product.
     *
     * @param plant the GridItem representing the plant
     * @return the harvested Product
     * @throws UnableToInteractException if the plant cannot be harvested
     */
    private Product harvestPlant(GridItem plant) throws UnableToInteractException {
        List<String> stages = GROWTH_STAGES.get(plant.type);
        if (stages == null || !plant.symbol.equals(stages.getLast())) {
            throw new UnableToInteractException("Crop not fully grown!");
        }
        Quality quality = randomQuality.getRandomQuality();
        plant.symbol = stages.getFirst();
        plant.attributes.put("Stage", "0");
        return switch (plant.type) {
            case "wheat" -> new Bread(quality);
            case "coffee" -> new Coffee(quality);
            case "berry" -> new Jam(quality);
            default -> throw new UnableToInteractException("Unknown plant type");
        };
    }

    /**
     * Performs an interaction on the grid at the specified position.
     *
     * @param command the interaction command
     * @param row     the row coordinate
     * @param column  the column coordinate
     * @return true if the interaction was successful, false otherwise
     * @throws UnableToInteractException if the interaction cannot be performed
     */
    @Override
    public boolean interact(String command, int row, int column) throws UnableToInteractException {
        return switch (command) {
            case "feed" -> farmType.equals("animal") && feed(row, column);
            case "end-day" -> { endDay(); yield true; }
            case "remove" -> { remove(row, column); yield true; }
            default -> throw new UnableToInteractException("Unknown command: " + command);
        };
    }

    /**
     * Feeds the animal at the specified position.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @return true if the animal was successfully fed, false otherwise
     */
    private boolean feed(int row, int column) {
        if (!isValidPosition(row, column)) return false;
        GridItem item = farmState.get(getIndex(row, column));
        if (item.type.equals("cow") || item.type.equals("chicken") || item.type.equals("sheep")) {
            item.attributes.put("Fed", "true");
            return true;
        }
        return false;
    }

    /**
     * Removes the item at the specified position.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     */
    private void remove(int row, int column) {
        if (isValidPosition(row, column)) {
            farmState.set(getIndex(row, column), new GridItem("ground", " ", new HashMap<>()));
        }
    }

    /**
     * Advances the farm to the next day, updating all items on the grid.
     */
    public void endDay() {
        for (GridItem item : farmState) {
            if (farmType.equals("plant")) {
                growPlant(item);
            } else if (farmType.equals("animal")) {
                resetAnimal(item);
            }
        }
    }

    /**
     * Grows a plant to its next stage.
     *
     * @param plant the GridItem representing the plant
     */
    private void growPlant(GridItem plant) {
        List<String> stages = GROWTH_STAGES.get(plant.type);
        if (stages != null) {
            int currentStage = stages.indexOf(plant.symbol);
            if (currentStage < stages.size() - 1) {
                plant.symbol = stages.get(currentStage + 1);
                plant.attributes.put("Stage", String.valueOf(currentStage + 2));
            }
        }
    }

    /**
     * Resets an animal's fed and collected status for the new day.
     *
     * @param animal the GridItem representing the animal
     */
    private void resetAnimal(GridItem animal) {
        if (animal.type.equals("cow") || animal.type.equals("chicken") || animal.type.equals("sheep")) {
            animal.attributes.put("Fed", "false");
            animal.attributes.put("Collected", "false");
        }
    }

    /**
     * Generates a string representation of the farm grid.
     *
     * @return a string representing the current state of the farm grid
     */
    @Override
    public String farmDisplay() {
        StringBuilder display = new StringBuilder("-".repeat((columns * 2) + 3) + "\n");
        for (int i = 0; i < rows; i++) {
            display.append("| ");
            for (int j = 0; j < columns; j++) {
                display.append(farmState.get(getIndex(i, j)).symbol).append(" ");
            }
            display.append("|\n");
        }
        display.append("-".repeat((columns * 2) + 3)).append("\n");
        return display.toString();
    }

    /**
     * Retrieves the current stats of all items on the farm grid.
     *
     * @return a list of lists containing the stats for each grid position
     */
    @Override
    public List<List<String>> getStats() {
        return farmState.stream()
                .map(item -> {
                    List<String> stats = new ArrayList<>();
                    stats.add(item.type);
                    stats.add(item.symbol);
                    stats.addAll(item.attributes.values());
                    return stats;
                })
                .toList();
    }

    /**
     * @return the number of rows in the grid
     */
    @Override
    public int getRows() { return rows; }

    /**
     * @return the number of columns in the grid
     */
    @Override
    public int getColumns() { return columns; }


    /**
     * Checks if the given position is valid within the grid.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    /**
     * Converts 2D coordinates to a 1D index in the farmState list.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @return the corresponding index in the farmState list
     */
    private int getIndex(int row, int column) {
        return row * columns + column;
    }

    /**
     * Represents an item on the farm grid.
     */
    private static class GridItem {
        String type;
        String symbol;
        Map<String, String> attributes;

        /**
         * Constructs a GridItem with no attributes.
         *
         * @param type   the type of the item
         * @param symbol the symbol representing the item
         */
        GridItem(String type, String symbol) {
            this(type, symbol, new HashMap<>());
        }

        /**
         * Constructs a GridItem with specified attributes.
         *
         * @param type       the type of the item
         * @param symbol     the symbol representing the item
         * @param attributes a map of the item's attributes
         */
        GridItem(String type, String symbol, Map<String, String> attributes) {
            this.type = type;
            this.symbol = symbol;
            this.attributes = new LinkedHashMap<>(attributes);
        }
    }
}