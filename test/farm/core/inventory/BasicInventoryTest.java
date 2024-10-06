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
    private List<Product> testProduct;

    @Before
    public void setUp(){
        inventory = new BasicInventory();
        testProduct = new ArrayList<>();

    }

    @Test
    public void testBasicInventory() {
        assertEquals(testProduct, inventory.getAllProducts());
    }

    @Test
    public void testAddProduct() {
        testProduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testProduct, inventory.getAllProducts());
    }

    @Test
    public void testADDMultipleProduct() {
        testProduct.add(new Jam());
        testProduct.add(new Egg());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testProduct, inventory.getAllProducts());
        testProduct.add(new Jam());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(testProduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProduct() {
        testProduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testProduct, inventory.removeProduct(new Egg().getBarcode()));
        assertEquals(new ArrayList<>(), inventory.getAllProducts());
    }

    @Test
    public void testRemoveMultipleProduct() {
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);

        List<Product> removed = inventory.removeProduct(new Jam().getBarcode());
        assertEquals(1, removed.size());
        assertEquals(new Jam(Quality.REGULAR), removed.getFirst());

        assertEquals(1, inventory.getAllProducts().size());
        assertEquals(new Egg(Quality.REGULAR), inventory.getAllProducts().getFirst());
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
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        assertTrue(inventory.existsProduct(Barcode.EGG));
        assertFalse(inventory.existsProduct(Barcode.JAM));
    }

    @Test
    public void testNotExistsProduct() {
        assertFalse(inventory.existsProduct(new Egg().getBarcode()));
    }

    @Test
    public void existsProductNull(){
        assertFalse(inventory.existsProduct(null));
    }

    @Test
    public void testAddNullProductError() {
        try {
            inventory.addProduct(null, Quality.REGULAR);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            assertEquals(e.getMessage(),
                    "Cannot invoke \"farm.inventory.product.data.Barcode.ordinal()\" because \"barcode\" is null");
        }
        assertEquals(0, inventory.getAllProducts().size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullProduct() {
        inventory.addProduct(null, Quality.REGULAR);
    }

    @Test
    public void testAddDuplicateProducts() {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(2, inventory.getAllProducts().size());
    }

    @Test
    public void testRemoveProductFromEmptyInventory() {
        assertEquals(Collections.emptyList(), inventory.removeProduct(new Egg().getBarcode()));
    }

    @Test
    public void testAddAllProductTypes() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.addProduct(Barcode.MILK, Quality.IRIDIUM);
        inventory.addProduct(Barcode.JAM, Quality.SILVER);
        inventory.addProduct(Barcode.WOOL, Quality.REGULAR);
        inventory.addProduct(Barcode.BREAD, Quality.GOLD);
        inventory.addProduct(Barcode.COFFEE, Quality.REGULAR);

        assertEquals(6, inventory.getAllProducts().size());
    }

    @Test
    public void testAddProductWithDifferentQualities() {
        inventory.addProduct(Barcode.EGG, Quality.IRIDIUM);
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.addProduct(Barcode.EGG, Quality.SILVER);
        inventory.addProduct(Barcode.EGG, Quality.GOLD);
        assertEquals(4, inventory.getAllProducts().size());
    }

    @Test
    public void testAddNullQuality() {
        inventory.addProduct(Barcode.EGG, null);
    }


    @Test
    public void testLargeInventory() {
        for (int i = 0; i < 100000; i++) {
            inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        }
        assertEquals(100000, inventory.getAllProducts().size());
    }


    @Test
    public void testRemoveNonExistingProduct() {
        List<Product> removedProducts = inventory.removeProduct(Barcode.MILK);
        assertTrue(removedProducts.isEmpty());
    }

    @Test
    public void testAddAndRemoveQuickSuccession() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        List<Product> removed = inventory.removeProduct(Barcode.EGG);

        assertEquals(1, removed.size());
        assertTrue(inventory.getAllProducts().isEmpty());
    }

    @Test
    public void testRemoveProductTwice() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.removeProduct(Barcode.EGG);  // First removal
        List<Product> secondRemoval = inventory.removeProduct(Barcode.EGG);  // Second removal

        assertTrue(secondRemoval.isEmpty());
    }

}
