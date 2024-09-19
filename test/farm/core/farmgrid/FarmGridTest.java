package farm.core.farmgrid;

import farm.core.Farm;
import farm.core.FarmManager;
import farm.core.ShopFront;
import farm.core.UnableToInteractException;
import farm.customer.AddressBook;
import farm.inventory.FancyInventory;
import farm.inventory.product.*;
import farm.inventory.product.data.Quality;
import farm.inventory.product.data.RandomQuality;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import static org.junit.Assert.*;

public class FarmGridTest {

    private MockShopFront shopFront;
    private FarmManager farmManager;

    private Grid plantGrid;
    private Grid animalGrid;
    private final int INIT_ROW = 5;
    private final int INIT_COL = 10;
    Map<Integer, List<String>> itemsPlacedPlant = new HashMap<>();
    Map<Integer, List<String>> itemsPlacedAnimal = new HashMap<>();

    private final char coffeeStage1 = ':';
    private final char coffeeStage2 = ';';
    private final char coffeeStage3 = '*';
    private final char coffeeStage4 = '%';
    private final char berryStage1 = '.';
    private final char berryStage2 = 'o';
    private final char berryStage3 = '@';
    private final char wheatStage1 = '\u1F34';
    private final char wheatStage2 = '#';

    private final char chicken = '\u09EC';
    private final char cow = '\u096A';
    private final char sheep = '\u0D94';
    // char that does not represent any plants of animals in the farm
    private final char notInUse = 'F';
    private final String shouldNotThrow = "Should not have thrown any exceptions";
    private final String notThrownOrIncorrect = "Exception was not thrown or gave incorrect message";

    private final String REMOVE = "remove";
    private final String END_DAY = "end-day";
    private final String FEED = "feed";

    /**
     * Might need to change how plantGrid and/or animalGrid is constructed in setUp() otherwise feel free to add
     * additional test but would recommend against modifying existing test but highly recommend adding more test.
     * Remember that the behaviour of bug free FarmGrid should stay the same
     */
    @Before
    public void setUp() {
        // Used to set the seeding so that we can test the output for quality when harvesting plants and animals
        RandomQuality.setSeed((long) 0118999881999119725.0);
        shopFront = new MockShopFront();

        // Create a plant grid using the FarmManager
        shopFront.setNewGridData(true, INIT_ROW, INIT_COL);
        farmManager = new FarmManager(new Farm(new FancyInventory(), new AddressBook()),
                shopFront, true);
        farmManager.run();
        plantGrid = farmManager.getCurrentGrid();

        // Create an animal grid using the FarmManager
        shopFront.setNewGridData(false, INIT_ROW, INIT_COL);
        farmManager = new FarmManager(new Farm(new FancyInventory(), new AddressBook()),
                shopFront, true);
        farmManager.run();
        animalGrid = farmManager.getCurrentGrid();

        itemsPlacedPlant.clear();
        itemsPlacedAnimal.clear();
    }

    @Test
    public void emptyPlantGridTest() {
        assertEquals("Plant farm did not return expected cols", INIT_COL ,plantGrid.getColumns());
        assertEquals("Plant farm did not return expected rows", INIT_ROW, plantGrid.getRows());
        assertEquals("Plant farm did not generate expected stats", expectedStats(new HashMap<>()), plantGrid.getStats());
    }

    @Test
    public void emptyAnimalGridTest() {
        assertEquals("Animal farm did not return expected cols", INIT_COL ,animalGrid.getColumns());
        assertEquals("Animal farm did not return expected rows", INIT_ROW, animalGrid.getRows());
        assertEquals("Animal farm did not generate expected stats", expectedStats(new HashMap<>()), animalGrid.getStats());
    }

    @Test
    public void placePlantInInvalidPositionPlantGridTest() {
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(-5, -5, berryStage1));
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(-5, 5, berryStage1));
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(5, -5, berryStage1));
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(INIT_ROW, INIT_COL, berryStage1));
    }

    @Test
    public void placeInvalidItemPlantGridTest() {
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(0, 0, notInUse));
        assertFalse("Placement succeeded when it should have failed",
                plantGrid.place(-5, 0, notInUse));
    }

    @Test
    public void placeSingleItemPlantGridTest() {
        plantGrid.place(INIT_ROW/2, INIT_COL/2, berryStage1);
        itemsPlacedPlant.put(convertToPosition(INIT_ROW/2, INIT_COL/2),
                new ArrayList<>(Arrays.asList("berry", Character.toString(berryStage1), "Stage: 1")));
        assertEquals("Plant grid stats was incorrect after placement",
                expectedStats(itemsPlacedPlant), plantGrid.getStats());
    }

    @Test
    public void placeSingleAnimalGridTest() {
        animalGrid.place(INIT_ROW/2, INIT_COL/2, sheep);
        itemsPlacedAnimal.put(convertToPosition(INIT_ROW/2, INIT_COL/2),
                new ArrayList<>(Arrays.asList("sheep", Character.toString(sheep), "Fed: false", "Collected: false")));
        assertEquals("Animal grid stats was incorrect after placement",
                expectedStats(itemsPlacedAnimal), animalGrid.getStats());
    }

    @Test
    public void simpleRemovePlantGridTest() {
        plantGrid.place(INIT_ROW/2, INIT_COL/2, berryStage1);
        attemptInteraction(plantGrid, REMOVE, INIT_ROW/2, INIT_COL/2);
        assertEquals("Plant grid stats was incorrect after removal",
                expectedStats(new HashMap<>()), plantGrid.getStats());
    }

    @Test
    public void simpleFeedAnimalGridTest() {
        populateAnimalFarm(animalGrid);
        attemptInteraction(animalGrid, FEED, 0, 0);
        itemsPlacedAnimal.get(convertToPosition(0, 0)).set(2, "Fed: true");
        assertEquals("Animal grid stats was incorrect after feeding",
                expectedStats(itemsPlacedAnimal), animalGrid.getStats());
    }

    @Test
    public void fedAndHarvestSingleAnimalGridTest() {
        populateAnimalFarm(animalGrid);
        attemptInteraction(animalGrid, FEED, INIT_ROW/2, INIT_COL/2);
        itemsPlacedAnimal.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(2, "Fed: true");
        try {
            assertEquals(new Milk(Quality.SILVER), animalGrid.harvest(INIT_ROW/2, INIT_COL/2));
        } catch (Exception e) {
            fail(shouldNotThrow);
        }
        itemsPlacedAnimal.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(3, "Collected: true");
        assertEquals(expectedStats(itemsPlacedAnimal), animalGrid.getStats());
    }

    @Test
    public void harvestInvalidPositionAnimalGridTest() {
        populateAnimalFarm(animalGrid);
        String exceptionMsg1 = assertThrows(UnableToInteractException.class,
                () -> animalGrid.harvest(-5, 5)).getMessage();
        String exceptionMsg2 = assertThrows(UnableToInteractException.class,
                () -> animalGrid.harvest(INIT_ROW, INIT_COL)).getMessage();
        String exceptionMsg3 = assertThrows(UnableToInteractException.class,
                () -> animalGrid.harvest(10, -5)).getMessage();

        assertEquals(expectedStats(itemsPlacedAnimal) ,animalGrid.getStats());
        assertEquals("You can't harvest this location", exceptionMsg1);
        assertEquals("You can't harvest this location", exceptionMsg2);
        assertEquals("You can't harvest this location", exceptionMsg3);
    }

    @Test
    public void harvestPlantEarlyTest() {
        populatePlantFarm(plantGrid);
        String errorMsg1 = assertThrows(UnableToInteractException.class,
                () -> plantGrid.harvest(0, 0)).getMessage();
        String errorMsg2 = assertThrows(UnableToInteractException.class,
                () -> plantGrid.harvest(INIT_ROW/2, INIT_COL/2)).getMessage();
        assertEquals(errorMsg1, "The crop is not fully grown!");
        assertEquals(errorMsg2, "The crop is not fully grown!");
    }

    @Test
    public void harvestPlantGridTest() {
        populatePlantGrownPlantGrid(plantGrid);
        try {
            assertEquals(new Coffee(Quality.SILVER) ,plantGrid.harvest(0, 0));
            itemsPlacedPlant.get(convertToPosition(0, 0)).set(2, "Stage: 0");
            itemsPlacedPlant.get(convertToPosition(0, 0)).set(1, Character.toString(coffeeStage1));

            assertEquals(new Bread(Quality.REGULAR), plantGrid.harvest(INIT_ROW/2, INIT_COL/2));
            itemsPlacedPlant.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(2, "Stage: 0");
            itemsPlacedPlant.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(1, Character.toString(wheatStage1));
            assertEquals("Unexpected stats produced", expectedStats(itemsPlacedPlant), plantGrid.getStats());
        } catch (Exception e) {
            fail(shouldNotThrow);
        }
    }

    @Test
    public void endDayHarvestedAnimalFarm() {
        populateAnimalFarm(animalGrid);
        attemptInteraction(animalGrid, FEED, 0, 0);
        attemptInteraction(animalGrid, FEED, INIT_ROW - 1, INIT_COL - 1);
        try {
            animalGrid.harvest(0, 0);
            animalGrid.harvest(INIT_ROW - 1, INIT_COL - 1);
        } catch (UnableToInteractException e) {
            fail(shouldNotThrow);
        }
        attemptInteraction(animalGrid, END_DAY, 0, 0);
        assertEquals("Animal grid stats was incorrect after end-day", expectedStats(itemsPlacedAnimal), animalGrid.getStats());
    }

    @Test
    public void endDayPlantFarm() {
        populatePlantFarm(plantGrid);
        attemptInteraction(plantGrid, END_DAY, 0, 0);
        itemsPlacedPlant.get(convertToPosition(0, 0)).set(2, "Stage: 2");
        itemsPlacedPlant.get(convertToPosition(0, 0)).set(1, Character.toString(coffeeStage2));
        itemsPlacedPlant.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(2, "Stage: 2");
        itemsPlacedPlant.get(convertToPosition(INIT_ROW/2, INIT_COL/2)).set(1, Character.toString(wheatStage2));
        itemsPlacedPlant.get(convertToPosition(INIT_ROW - 1, INIT_COL - 1)).set(2, "Stage: 2");
        itemsPlacedPlant.get(convertToPosition(INIT_ROW - 1, INIT_COL - 1)).set(1, Character.toString(berryStage2));
        assertEquals(expectedStats(itemsPlacedPlant), plantGrid.getStats());
    }

    private void populatePlantGrownPlantGrid(Grid plantGrid) {
        populatePlantFarm(plantGrid);
        attemptInteraction(plantGrid, END_DAY, 0, 0);
        attemptInteraction(plantGrid, END_DAY, 0, 0);
        attemptInteraction(plantGrid, END_DAY, 0, 0);
        attemptInteraction(plantGrid, END_DAY, 0, 0);
        itemsPlacedPlant.put(convertToPosition(0, 0),
                new ArrayList<>(Arrays.asList("coffee", Character.toString(coffeeStage4), "Stage: 4")));
        itemsPlacedPlant.put(convertToPosition(INIT_ROW/2, INIT_COL/2),
                new ArrayList<>(Arrays.asList("wheat", Character.toString(wheatStage2), "Stage: 2")));
        itemsPlacedPlant.put(convertToPosition(INIT_ROW - 1, INIT_COL - 1),
                new ArrayList<>(Arrays.asList("berry", Character.toString(berryStage3), "Stage: 3")));
    }

    private void populatePlantFarm(Grid plantGrid) {
        plantGrid.place(0, 0, coffeeStage1);
        plantGrid.place(INIT_ROW/2, INIT_COL/2, wheatStage1);
        plantGrid.place(INIT_ROW - 1, INIT_COL - 1, berryStage1);

        itemsPlacedPlant.put(convertToPosition(0, 0),
                new ArrayList<>(Arrays.asList("coffee", Character.toString(coffeeStage1), "Stage: 1")));
        itemsPlacedPlant.put(convertToPosition(INIT_ROW/2, INIT_COL/2),
                new ArrayList<>(Arrays.asList("wheat", Character.toString(wheatStage1), "Stage: 1")));
        itemsPlacedPlant.put(convertToPosition(INIT_ROW - 1, INIT_COL - 1),
                new ArrayList<>(Arrays.asList("berry", Character.toString(berryStage1), "Stage: 1")));
    }

    private void populateAnimalFarm(Grid animalGrid) {
        animalGrid.place(INIT_ROW/2, INIT_COL/2, cow);
        animalGrid.place(0, 0, chicken);
        animalGrid.place(INIT_ROW - 1, INIT_COL - 1, sheep);
        itemsPlacedAnimal.put(convertToPosition(0, 0),
                new ArrayList<>(Arrays.asList("chicken", Character.toString(chicken), "Fed: false", "Collected: false")));
        itemsPlacedAnimal.put(convertToPosition(INIT_ROW/2, INIT_COL/2),
                new ArrayList<>(Arrays.asList("cow", Character.toString(cow), "Fed: false", "Collected: false")));
        itemsPlacedAnimal.put(convertToPosition(INIT_ROW - 1, INIT_COL - 1),
                new ArrayList<>(Arrays.asList("sheep", Character.toString(sheep), "Fed: false", "Collected: false")));
    }

    /**
     * helper method that coverts rows and cols into a single integer
     */
    private int convertToPosition(int row, int col) {
        return INIT_COL * row + col;
    }

    /**
     * Helper method to generate expected stats
     * @param toPlace mapping of single integer that represents location to a List<String> that contain the stat for
     *                the tile
     */
    private List<List<String>> expectedStats(Map<Integer, List<String>> toPlace) {
        List<List<String>> stats = new ArrayList<>();
        List<String> emptyPlace = new ArrayList<>(Arrays.asList("ground", " "));
        for (int pos = 0; pos < INIT_ROW * INIT_COL; pos++) {
            List<String> tile = toPlace.getOrDefault(pos, emptyPlace);
            stats.add(tile);
        }
        return stats;
    }

    private boolean attemptInteraction(Grid grid, String command, int row, int column) {
        try {
            return grid.interact(command, row , column);
        } catch (UnableToInteractException e) {
            fail(String.format("Unexpected exception thrown for %s interaction. Message: %s",
                    command, e.getMessage()));
        }
        return false;
    }

    // Mocked ShopFront UI to inject desired prompts
    private static class MockShopFront extends ShopFront {
        private boolean makePlant = false;
        private int rowNum = 0;
        private int colNum = 0;

        public void setNewGridData(boolean makePlant, int rowNum, int colNum) {
            this.makePlant = makePlant;
            this.rowNum = rowNum;
            this.colNum = colNum;
        }

        @Override
        public void displayInlineMessage(String message) {

        }

        @Override
        public String[] loadOrNewHandler(Set<String> commands, String helpMsg) {
            displayMessage("Would you like to load a farm, or start a new one?");
            displayMessage(helpMsg);
            return new String[]{"new", makePlant ? "plant" : "animal",
                    String.valueOf(rowNum), String.valueOf(colNum)};
        }

        @Override
        public String[] modePromptHandler(String modeName, Set<String> commands, String helpMsg) {
            displayInlineMessage(modeName + ": Please enter command (h to see options): ");
            return new String[]{"q"};
        }
    }
}