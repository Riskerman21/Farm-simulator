package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.RandomQuality;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a grid-based farm that can contain plants or animals.
 * Implements the Grid interface to provide farming functionality.
 */
public class FarmGrid implements Grid {
    private final int rows;
    private final int columns;
    private final Object[][] grid;
    private final RandomQuality randomQuality;
    private final String farmType;
    private final ItemFarm itemFarm;

    private static final Map<String, ItemFarm> FARM_TYPE_TO_FACTORY = Map.of(
            "plant", new PlantFarm(),
            "animal", new AnimalFarm()
    );

    /**
     * Constructor for the FarmGrid, creating a farm of specified type.
     * @param rows the number of rows on the grid
     * @param columns the number of columns on the grid
     * @param farmType the type of farm (either "plant" or "animal")
     */
    public FarmGrid(int rows, int columns, String farmType) {
        this.rows = rows;
        this.columns = columns;
        this.farmType = farmType;
        this.grid = new Object[rows][columns];
        this.randomQuality = new RandomQuality();
        this.itemFarm = FARM_TYPE_TO_FACTORY.get(farmType.toLowerCase());
        if (this.itemFarm == null) {
            throw new IllegalArgumentException("Invalid farm type: " + farmType);
        }
    }

    @Override
    public boolean place(int row, int column, char symbol) {
        Object newItem = itemFarm.createItem(symbol);
        if (!isValidPosition(row, column) || !itemFarm.isValidForFarm(newItem)) {
            return false;
        }
        if (grid[row][column] != null) {
            throw new IllegalStateException("Something is already there!");
        }

        grid[row][column] = newItem;
        return true;
    }

    @Override
    public Product harvest(int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("You can't harvest this location");
        }
        Object item = grid[row][column];
        return itemFarm.harvestItem(item, randomQuality.getRandomQuality());
    }

    @Override
    public List<List<String>> getStats() {
        List<List<String>> stats = new ArrayList<>(rows * columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                stats.add(itemFarm.getItemStats(item));
            }
        }
        return stats;
    }


    @Override
    public boolean interact(String command, int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("Invalid position.");
        }
        Object item = grid[row][column];
        return switch (command) {
            case "feed" -> itemFarm.feedItem(item);
            case "end-day" -> endDay();
            case "remove" -> remove(row, column);
            default -> throw new UnableToInteractException("Unknown command: " + command);
        };
    }

    /**
     * process the end of day of each farm
     * @return always true
     */
    private boolean endDay() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                itemFarm.processEndOfDay(item);
            }
        }
        return true;
    }

    /**
     * resets a tile to ground
     * @param row the row of the tile to reset
     * @param column the column of the tile to reset
     * @return true if a farm tile is reset to ground
     */
    private boolean remove(int row, int column) {
        if (isValidPosition(row, column)) {
            grid[row][column] = null;
            return true;
        }
        return false;
    }

    @Override
    public String farmDisplay() {
        StringBuilder display = new StringBuilder("-".repeat((columns * 2) + 3)).append('\n');
        for (int i = 0; i < rows; i++) {
            display.append("| ");
            for (int j = 0; j < columns; j++) {
                Object item = grid[i][j];
                display.append(itemFarm.getDisplaySymbol(item));
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

    @Override
    public String toString() {
        return farmType;
    }
}