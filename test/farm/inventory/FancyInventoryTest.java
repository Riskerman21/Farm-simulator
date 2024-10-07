package farm.inventory;

import farm.core.FailedTransactionException;
import farm.core.InvalidStockRequestException;
import farm.inventory.product.Egg;
import farm.inventory.product.Jam;
import farm.inventory.product.Product;
import farm.inventory.product.data.Barcode;
import farm.inventory.product.data.Quality;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


public class FancyInventoryTest {
    private FancyInventory inventory;
    private List<Product> tesProduct;

    @Before
    public void setUp(){
        inventory = new FancyInventory();
        tesProduct = new ArrayList<>();

    }

    @Test
    public void testBasicInventory() {
        assertEquals(tesProduct, inventory.getAllProducts());
    }

    @Test
    public void testAddProduct() {
        tesProduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(tesProduct, inventory.getAllProducts());
    }

    @Test
    public void testADDMultipleProduct() {
        tesProduct.add(new Egg());
        tesProduct.add(new Jam());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(tesProduct, inventory.getAllProducts());
        tesProduct.add(new Jam());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(tesProduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProduct() {
        tesProduct.add(new Egg());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(tesProduct, inventory.removeProduct(new Egg().getBarcode()));
        assertEquals(new ArrayList<>(), inventory.getAllProducts());
    }

    @Test
    public void testRemoveMultipleProduct() {
        tesProduct.add(new Jam());
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        assertEquals(tesProduct, inventory.removeProduct(new Jam().getBarcode()));
        tesProduct.removeFirst();
        tesProduct.add(new Egg());
        assertEquals(tesProduct, inventory.getAllProducts());
    }

    @Test
    public void testAddProductByQuantity() throws InvalidStockRequestException {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 2);
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR, 2);
        tesProduct.add(new Egg());
        tesProduct.add(new Egg());
        tesProduct.add(new Jam());
        tesProduct.add(new Jam());
        assertEquals(tesProduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProductByQuantity() throws FailedTransactionException {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        inventory.removeProduct(new Egg().getBarcode(), 2);
        assertEquals(tesProduct, inventory.getAllProducts());
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
