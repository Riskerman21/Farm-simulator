package farm.debugged.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.*;
import farm.inventory.product.data.Quality;
import farm.inventory.product.data.RandomQuality;

import java.util.*;

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

    public FarmGrid(int rows, int columns) {
        this(rows, columns, "plant");
    }

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

    private GridItem getGridItem(char symbol, String itemName) {
        boolean isAnimal = itemName.equals("chicken") || itemName.equals("cow") || itemName.equals("sheep");
        if ((farmType.equals("animal") && !isAnimal) || (farmType.equals("plant") && isAnimal)) {
            throw new IllegalArgumentException("Invalid item for this farm type!");
        }

        return isAnimal
                ? new GridItem(itemName, String.valueOf(symbol), Map.of("Fed", "false", "Collected", "false"))
                : new GridItem(itemName, String.valueOf(symbol), Map.of("Stage", "1"));
    }

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

    private Product harvestPlant(GridItem plant) throws UnableToInteractException {
        List<String> stages = GROWTH_STAGES.get(plant.type);
        if (stages == null || !plant.symbol.equals(stages.get(stages.size() - 1))) {
            throw new UnableToInteractException("Crop not fully grown!");
        }

        Quality quality = randomQuality.getRandomQuality();
        plant.symbol = stages.get(0);
        plant.attributes.put("Stage", "0");

        return switch (plant.type) {
            case "wheat" -> new Bread(quality);
            case "coffee" -> new Coffee(quality);
            case "berry" -> new Jam(quality);
            default -> throw new UnableToInteractException("Unknown plant type");
        };
    }

    @Override
    public boolean interact(String command, int row, int column) throws UnableToInteractException {
        return switch (command) {
            case "feed" -> farmType.equals("animal") && feed(row, column);
            case "end-day" -> { endDay(); yield true; }
            case "remove" -> { remove(row, column); yield true; }
            default -> throw new UnableToInteractException("Unknown command: " + command);
        };
    }

    private boolean feed(int row, int column) {
        if (!isValidPosition(row, column)) return false;

        GridItem item = farmState.get(getIndex(row, column));
        if (item.type.equals("cow") || item.type.equals("chicken") || item.type.equals("sheep")) {
            item.attributes.put("Fed", "true");
            return true;
        }
        return false;
    }

    private void remove(int row, int column) {
        if (isValidPosition(row, column)) {
            farmState.set(getIndex(row, column), new GridItem("ground", " ", new HashMap<>()));
        }
    }

    public void endDay() {
        for (GridItem item : farmState) {
            if (farmType.equals("plant")) {
                growPlant(item);
            } else if (farmType.equals("animal")) {
                resetAnimal(item);
            }
        }
    }

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

    private void resetAnimal(GridItem animal) {
        if (animal.type.equals("cow") || animal.type.equals("chicken") || animal.type.equals("sheep")) {
            animal.attributes.put("Fed", "false");
            animal.attributes.put("Collected", "false");
        }
    }

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

    @Override
    public int getRows() { return rows; }

    @Override
    public int getColumns() { return columns; }

    public String getFarmType() { return farmType; }

    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    private int getIndex(int row, int column) {
        return row * columns + column;
    }

    private static class GridItem {
        String type;
        String symbol;
        Map<String, String> attributes;

        GridItem(String type, String symbol) {
            this(type, symbol, new HashMap<>());
        }

        GridItem(String type, String symbol, Map<String, String> attributes) {
            this.type = type;
            this.symbol = symbol;
            this.attributes = new LinkedHashMap<>(attributes);
        }
    }
}