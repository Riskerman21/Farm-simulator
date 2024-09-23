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
    private final List<String> types;
    private final List<String> symbols;
    private final List<Map<String, String>> attributes;
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
     * Constructor for the FarmGrid, creating a farm of specified type.
     * @param rows the number of rows on the grid
     * @param columns the number of columns on the grid
     * @requires rows > 0 && columns > 0
     */
    public FarmGrid(int rows, int columns, String farmType) {
        this.rows = rows;
        this.columns = columns;
        this.farmType = farmType;
        int size = rows * columns;
        this.types = new ArrayList<>(size);
        this.symbols = new ArrayList<>(size);
        this.attributes = new ArrayList<>(size);
        this.randomQuality = new RandomQuality();

        for (int i = 0; i < size; i++) {
            types.add("ground");
            symbols.add(" ");
            attributes.add(new HashMap<>());
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

    @Override
    public boolean place(int row, int column, char symbol) throws IllegalStateException {
        if (!isValidPosition(row, column)) return false;
        String itemName = SYMBOL_TO_ITEM.get(symbol);
        if (itemName == null) return false; //invalid item
        int index = getIndex(row, column);
        if (!symbols.get(index).equals(" ")) {
            throw new IllegalStateException("Something is already there!");
        }
        placeItem(index, symbol, itemName);
        return true;
    }

    /**
     * Places a new item on the grid at the specified index.
     *
     * @param index The index in the grid where the item should be placed.
     * @param symbol The symbol representing the item on the grid.
     * @param itemName The name of the item to be placed.
     * @throws IllegalArgumentException If the item type does not match the farm type.
     */
    private void placeItem(int index, char symbol, String itemName) {
        boolean isAnimal = itemName.equals("chicken") || itemName.equals("cow") || itemName.equals("sheep");
        if ((farmType.equals("animal") && !isAnimal) || (farmType.equals("plant") && isAnimal)) {
            throw new IllegalArgumentException("Invalid item for this farm type!");
        }
        types.set(index, itemName);
        symbols.set(index, String.valueOf(symbol));
        Map<String, String> properties = attributes.get(index);
        properties.clear(); //remove ground attribute
        if (isAnimal) {
            properties.put("Fed", "Fed: false");
            properties.put("Collected", "Collected: false");
        } else {
            properties.put("Stage", "Stage: 1");
        }
    }

    @Override
    public Product harvest(int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("You can't harvest this location");
        }
        int index = getIndex(row, column);
        String type = types.get(index);
        if (type.equals("ground")) {
            throw new UnableToInteractException("Can't harvest an empty spot!");
        }
        return farmType.equals("animal") ? harvestAnimal(index) : harvestPlant(index);
    }

    /**
     * Harvests an animal product.
     *
     * @param index the index of the animal in the grid
     * @return the harvested Product
     * @throws UnableToInteractException if the animal cannot be harvested
     */
    private Product harvestAnimal(int index) throws UnableToInteractException {
        Map<String, String> animalAttributes = attributes.get(index);
        if (!animalAttributes.get("Fed").equals("Fed: true")) {
            throw new UnableToInteractException("Animal not fed today!");
        }
        if (animalAttributes.get("Collected").equals("Collected: true")) {
            throw new UnableToInteractException("Already collected today!");
        }
        animalAttributes.put("Collected", "Collected: true");
        Quality quality = randomQuality.getRandomQuality();
        return switch (types.get(index)) {
            case "cow" -> new Milk(quality);
            case "chicken" -> new Egg(quality);
            case "sheep" -> new Wool(quality);
            default -> throw new UnableToInteractException("Unknown animal type");
        };
    }

    /**
     * Harvests a plant product.
     *
     * @param index the index of the plant in the grid
     * @return the harvested Product
     * @throws UnableToInteractException if the plant cannot be harvested
     */
    private Product harvestPlant(int index) throws UnableToInteractException {
        String type = types.get(index);
        List<String> stages = GROWTH_STAGES.get(type);
        if (stages == null || !symbols.get(index).equals(stages.getLast())) {
            throw new UnableToInteractException("The crop is not fully grown!");
        }
        Quality quality = randomQuality.getRandomQuality();
        symbols.set(index, stages.getFirst());
        attributes.get(index).put("Stage", "Stage: 0");
        return switch (type) {
            case "wheat" -> new Bread(quality);
            case "coffee" -> new Coffee(quality);
            case "berry" -> new Jam(quality);
            default -> throw new UnableToInteractException("Unknown plant type");
        };
    }

    @Override
    public boolean interact(String command, int row, int column) throws UnableToInteractException {
        return switch (command) {
            case "feed" -> feed(row, column);
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
        int index = getIndex(row, column);
        String type = types.get(index);
        if (type.equals("cow") || type.equals("chicken") || type.equals("sheep")) {
            attributes.get(index).put("Fed", "Fed: true");
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
            int index = getIndex(row, column);
            types.set(index, "ground");
            symbols.set(index, " ");
            attributes.get(index).clear();
        }
    }

    /**
     * Advances the farm to the next day, updating all items on the grid.
     */
    public void endDay() {
        for (int i = 0; i < types.size(); i++) {
            if (farmType.equals("plant")) {
                growPlant(i);
            } else if (farmType.equals("animal")) {
                resetAnimal(i);
            }
        }
    }

    /**
     * Grows a plant to its next stage.
     *
     * @param index the index of the plant in the grid
     */
    private void growPlant(int index) {
        String type = types.get(index);
        List<String> stages = GROWTH_STAGES.get(type);
        if (stages != null) {
            int currentStage = stages.indexOf(symbols.get(index));
            if (currentStage < stages.size() - 1) {
                symbols.set(index, stages.get(currentStage + 1));
                String stage = String.valueOf(currentStage + 2);
                attributes.get(index).put("Stage", "Stage: " + stage);
            }
        }
    }

    /**
     * Resets an animal's fed and collected status for the new day.
     *
     * @param index the index of the animal in the grid
     */
    private void resetAnimal(int index) {
        String type = types.get(index);
        if (type.equals("cow") || type.equals("chicken") || type.equals("sheep")) {
            Map<String, String> animalAttributes = attributes.get(index);
            animalAttributes.put("Fed", "Fed: false");
            animalAttributes.put("Collected", "Collected: false");
        }
    }

    @Override
    public String farmDisplay() {
        StringBuilder display = new StringBuilder("-".repeat((columns * 2) + 3)).append('\n');
        for (int i = 0; i < rows; i++) {
            display.append("| ");
            for (int j = 0; j < columns; j++) {
                display.append(symbols.get(getIndex(i, j))).append(' ');
            }
            display.append("|\n");
        }
        return display.append("-".repeat((columns * 2) + 3)).append('\n').toString();
    }

    @Override
    public List<List<String>> getStats() {
        List<List<String>> stats = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            List<String> itemStats = new ArrayList<>(2 + attributes.get(i).size());
            itemStats.add(types.get(i));
            itemStats.add(symbols.get(i));
            itemStats.addAll(attributes.get(i).values());
            stats.add(itemStats);
        }
        return stats;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

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
     * Converts 2D coordinates to a 1D index in the grid lists.
     *
     * @param row    the row coordinate
     * @param column the column coordinate
     * @return the corresponding index in the grid lists
     */
    private int getIndex(int row, int column) {
        return row * columns + column;
    }
}