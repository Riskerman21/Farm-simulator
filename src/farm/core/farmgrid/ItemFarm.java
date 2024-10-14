package farm.core.farmgrid;

import farm.core.UnableToInteractException;
import farm.inventory.product.Product;
import farm.inventory.product.data.Quality;

import java.util.List;

/**
 * Interface describing the required methods for a farm
 */
public interface ItemFarm {

    /**
     * chooses an object that belongs to the farm based on the given symbol
     * @param symbol a symbol representing the object on the farm
     * @return the object to be created and null if no matching object
     */
    Object createItem(char symbol);

    /**
     * a check before placement whether this is a valid item to add to the farm
     * @param item an object representing an item that could be placed on the farm
     * @return true iff the item is able to be added to this farm
     */
    boolean isValidForFarm(Object item);

    /**
     * handles the harvest of an item on a farm
     * @param item the item to be harvested from the farm
     * @param quality the quality of the product that will be the result of the harvest
     * @return the product as a result of the harvest
     * @throws UnableToInteractException when the item is a ground
     * @requires item to be an object of the farm or ground
     */
    Product harvestItem(Object item, Quality quality) throws UnableToInteractException;

    /**
     * displays the item stats of an object (i.e. fed or not fed)
     * @param item an object on the farm to return the stats of
     * @return the stats of item
     * @requires item to be an object of the farm or ground
     */
    List<String> getItemStats(Object item);

    /**
     * checks whether an item on the farm is feedable or not and feeds them
     * @param item the object on the farm
     * @return true iff the item is fed
     */
    boolean feedItem(Object item);

    /**
     * performs action on an item of the farm after the day is over
     * @param item the object on the farm to perform the action to
     */
    void processEndOfDay(Object item);

    /**
     * returns the symbol representing the object on the farm
     * @param item the object on the farm to display the symbol of
     * @return the string of the symbol
     * @requires item to be an object of the farm or ground
     */
    String getDisplaySymbol(Object item);
}