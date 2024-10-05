package farm.core.inventory;

import farm.core.InvalidStockRequestException;
import farm.inventory.*;
import farm.inventory.product.*;
import farm.inventory.product.data.*;
import org.junit.*;

import java.util.*;
import static org.junit.Assert.*;


public class BasicInventoryTest {
    private BasicInventory inventory;
    private List<Product> testproduct;

    @Before
    public void setUp(){
        inventory = new BasicInventory();
        testproduct = new ArrayList<>();

    }

    @Test
    public void testBasicInventory() {
        assertEquals(inventory.getAllProducts(), new BasicInventory().getAllProducts());
    }

    @Test
    public void testAddProduct() {
        testproduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, testproduct);
    }

    @Test
    public void testRemoveProduct() {
        testproduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.removeProduct(new Egg().getBarcode()));
        assertEquals(new ArrayList<>(), inventory.getAllProducts());
    }

    @Test
    public void testRemoveMultipleProductByBarcode() {
        testproduct.add(new Jam());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.removeProduct(new Jam().getBarcode()));
        testproduct.removeFirst();
        testproduct.add(new Egg());
        assertEquals(testproduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProductByBarcodeAndQuantity() {
        try {
            inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 2);
        } catch (InvalidStockRequestException e) {
            assertEquals(e.getMessage(), "Current inventory is not fancy enough. "
                    + "Please supply products one at a time.");
        }
    }

    @Test
    public void testExistsProduct() {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertTrue(inventory.existsProduct(new Egg().getBarcode()));
    }

    @Test
    public void testNotExistsProduct() {
        assertFalse(inventory.existsProduct(new Egg().getBarcode()));
    }

    @Test
    public void testGetProductByBarcode() {}
}
