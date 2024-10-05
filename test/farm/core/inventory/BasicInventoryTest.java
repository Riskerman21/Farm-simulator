package farm.core.inventory;

import farm.core.FailedTransactionException;
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
        assertEquals(testproduct, inventory.getAllProducts());
    }

    @Test
    public void testAddProduct() {
        testproduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.getAllProducts());
    }

    @Test
    public void testADDMultipleProduct() {
        testproduct.add(new Jam());
        testproduct.add(new Egg());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.getAllProducts());
        testproduct.add(new Jam());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProduct() {
        testproduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.removeProduct(new Egg().getBarcode()));
        assertEquals(new ArrayList<>(), inventory.getAllProducts());
    }

    @Test
    public void testRemoveMultipleProduct() {
        testproduct.add(new Jam());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(testproduct, inventory.removeProduct(new Jam().getBarcode()));
        testproduct.removeFirst();
        testproduct.add(new Egg());
        assertEquals(testproduct, inventory.getAllProducts());
    }

    @Test
    public void testAddProductByQuantity() {
        try {
            inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 2);
        } catch (InvalidStockRequestException e) {
            assertEquals(e.getMessage(), "Current inventory is not fancy enough. "
                    + "Please supply products one at a time.");
        }
    }

    @Test
    public void testRemoveProductByQuantity() {
        try {
            inventory.removeProduct(new Egg().getBarcode(), 2);
        } catch (FailedTransactionException e) {
            assertEquals(e.getMessage(), "Current inventory is not fancy enough. "
                    + "Please purchase products one at a time.");
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
    public void existsProductNull(){
        assertFalse(inventory.existsProduct(null));
    }
}