package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.*;
import farm.inventory.product.data.Quality;
import farm.inventory.product.data.RandomQuality;

import java.util.*;

public class FarmGrid implements Grid {

    private final List<String> types;
    private final List<String> symbols;
    private final List<Map<String, String>> attributes;
    private final int rows;
    private final int columns;
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
        this.randomQuality = new RandomQuality();
        int size = rows * columns;
        this.types = new ArrayList<>(size);
        this.symbols = new ArrayList<>(size);
        this.attributes = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            this.types.add("ground");
            this.symbols.add(" ");
            this.attributes.add(new HashMap<>());
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

        int index = getGridIndex(row, column);
        if (!symbols.get(index).equals(" ")) {
            throw new IllegalStateException("Something is already there!");
        }

        placeItem(index, symbol, itemName);
        return true;
    }

    private void placeItem(int index, char symbol, String itemName) {
        boolean isAnimal = isAnimalItem(itemName);
        validateItemPlacement(isAnimal);

        types.set(index, itemName);
        symbols.set(index, String.valueOf(symbol));
        attributes.get(index).clear();

        if (isAnimal) {
            attributes.get(index).put("Fed", "Fed: false");
            attributes.get(index).put("Collected", "Collected: false");
        } else {
            attributes.get(index).put("Stage", "Stage: 1");
        }
    }

    @Override
    public Product harvest(int row, int column) throws UnableToInteractException {
        if (!isValidPosition(row, column)) {
            throw new UnableToInteractException("You can't harvest this location");
        }

        int index = getGridIndex(row, column);
        String type = types.get(index);

        if (type.equals("ground")) throw new UnableToInteractException("Can't harvest an empty spot!");

        return farmType.equals("animal") ? harvestAnimal(index) : harvestPlant(index);
    }

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

    private Product harvestPlant(int index) throws UnableToInteractException {
        List<String> stages = GROWTH_STAGES.get(types.get(index));

        if (!symbols.get(index).equals(stages.getLast())) {
            throw new UnableToInteractException("The crop is not fully grown!");
        }

        Quality quality = randomQuality.getRandomQuality();
        symbols.set(index, stages.getFirst());
        attributes.get(index).put("Stage", "Stage: 0");

        return switch (types.get(index)) {
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

    private boolean feed(int row, int column) {
        if (!isValidPosition(row, column)) return false;

        int index = getGridIndex(row, column);
        String type = types.get(index);

        if (isAnimalItem(type)) {
            attributes.get(index).put("Fed", "Fed: true");
            return true;
        }
        return false;
    }

    private void remove(int row, int column) {
        if (isValidPosition(row, column)) {
            int index = getGridIndex(row, column);
            types.set(index, "ground");
            symbols.set(index, " ");
            attributes.get(index).clear();
        }
    }

    public void endDay() {
        for (int i = 0; i < types.size(); i++) {
            if (farmType.equals("plant")) {
                growPlant(i);
            } else if (farmType.equals("animal")) {
                resetAnimal(i);
            }
        }
    }

    private void growPlant(int index) {
        List<String> stages = GROWTH_STAGES.get(types.get(index));
        if (stages != null) {
            int currentStage = stages.indexOf(symbols.get(index));
            if (currentStage < stages.size() - 1) {
                symbols.set(index, stages.get(currentStage + 1));
                attributes.get(index).put("Stage", "Stage: " + (currentStage + 2));
            }
        }
    }

    private void resetAnimal(int index) {
        if (isAnimalItem(types.get(index))) {
            attributes.get(index).put("Fed", "Fed: false");
            attributes.get(index).put("Collected", "Collected: false");
        }
    }

    @Override
    public String farmDisplay() {
        StringBuilder display = new StringBuilder("-".repeat((columns * 2) + 3)).append('\n');

        for (int i = 0; i < rows; i++) {
            display.append("| ");
            for (int j = 0; j < columns; j++) {
                display.append(symbols.get(getGridIndex(i, j))).append(' ');
            }
            display.append("|\n");
        }

        return display.append("-".repeat((columns * 2) + 3)).append('\n').toString();
    }

    @Override
    public List<List<String>> getStats() {
        List<List<String>> stats = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            List<String> itemStats = new ArrayList<>(attributes.get(i).size() + 2);
            itemStats.add(types.get(i));
            itemStats.add(symbols.get(i));
            itemStats.addAll(attributes.get(i).values());
            stats.add(itemStats);
        }
        return stats;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public int getColumns() {
        return this.columns;
    }

    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < this.rows && column >= 0 && column < this.columns;
    }

    private int getGridIndex(int row, int column) {
        return row * this.columns + column;
    }

    private boolean isAnimalItem(String itemName) {
        return itemName.equals("cow") || itemName.equals("chicken") || itemName.equals("sheep");
    }

    private void validateItemPlacement(boolean isAnimal) {
        if ((farmType.equals("animal") && !isAnimal) || (farmType.equals("plant") && isAnimal)) {
            throw new IllegalArgumentException("Invalid item for this farm type!");
        }
    }

    @Override
    public String toString() {
        return farmType;
    }
}