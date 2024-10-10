package farm.inventory;

import farm.core.FailedTransactionException;
import farm.core.InvalidStockRequestException;
import farm.inventory.product.*;
import farm.inventory.product.data.Barcode;
import farm.inventory.product.data.Quality;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class BasicInventoryTest {
    private BasicInventory inventory;
    private List<Product> testProduct;
    List<Product> products = Arrays.asList(
            new Egg(),
            new Milk(),
            new Jam(),
            new Wool(),
            new Bread(),
            new Coffee()
    );

    @Before
    public void setUp() {
        inventory = new BasicInventory();
        testProduct = new ArrayList<>();
    }

    @Test
    public void testRemoveProductByBarcode_EnsureCorrectRemoval() {
        Product egg1 = new Egg(Quality.REGULAR);
        Product milk1 = new Milk(Quality.REGULAR);
        Product jam1 = new Jam(Quality.REGULAR);
        inventory.addProduct(egg1.getBarcode(), egg1.getQuality());
        inventory.addProduct(milk1.getBarcode(), milk1.getQuality());
        inventory.addProduct(jam1.getBarcode(), jam1.getQuality());
        List<Product> removedProducts = inventory.removeProduct(milk1.getBarcode());

        assertEquals(1, removedProducts.size());
        assertEquals(milk1.getBarcode(), removedProducts.getFirst().getBarcode());

        List<Product> remainingProducts = inventory.getAllProducts();
        assertEquals(2, remainingProducts.size());
        assertTrue(remainingProducts.stream().anyMatch(p -> p.getBarcode().equals(egg1.getBarcode())));
        assertTrue(remainingProducts.stream().anyMatch(p -> p.getBarcode().equals(jam1.getBarcode())));
    }

    @Test(expected = InvalidStockRequestException.class)
    public void testAddProductWithQuantityGreaterThanOne_ThrowsException() throws InvalidStockRequestException {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 2);
    }
    @Test(expected = InvalidStockRequestException.class)
    public void testAddProductWithQuantityOne_ThrowsException() throws InvalidStockRequestException {
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 1);
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
    public void testAddMultipleProducts() {
        testProduct.add(new Jam());
        testProduct.add(new Egg());
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);
        assertEquals(testProduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProduct() {
        for (Product product : products) {
            testProduct.add(product);
            inventory.addProduct(product.getBarcode(), Quality.REGULAR);
            List<Product> removedProducts = inventory.removeProduct(product.getBarcode());
            assertEquals(1, removedProducts.size());
            assertEquals(product.getBarcode(), removedProducts.get(0).getBarcode());
            testProduct.remove(product);
            assertEquals(testProduct, inventory.getAllProducts());
        }
        assertTrue(inventory.getAllProducts().isEmpty());
    }

    @Test
    public void testRemoveMultipleProduct() {
        inventory.addProduct(new Jam().getBarcode(), Quality.REGULAR);
        inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR);

        List<Product> removed = inventory.removeProduct(new Jam().getBarcode());
        assertEquals(1, removed.size());
        assertEquals(new Jam(Quality.REGULAR), removed.get(0));
        assertEquals(1, inventory.getAllProducts().size());
        assertEquals(new Egg(Quality.REGULAR), inventory.getAllProducts().get(0));
    }

    @Test
    public void testAddProductByQuantityMoreThanOne() {
        try {
            inventory.addProduct(new Egg().getBarcode(), Quality.REGULAR, 2);
        } catch (InvalidStockRequestException ignored) {}
    }

    @Test(expected = FailedTransactionException.class)
    public void testRemoveProductByQuantityMoreThanOne() throws FailedTransactionException {
        inventory.removeProduct(new Egg().getBarcode(), 2);
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
    public void testExistsProductNull() {
        assertFalse(inventory.existsProduct(null));
    }

    @Test
    public void testAddNullProductError() {
        try {
            inventory.addProduct(null, Quality.REGULAR);
        } catch (NullPointerException e) {
            assertEquals("Cannot invoke \"farm.inventory.product.data.Barcode.ordinal()\" because \"barcode\" is null", e.getMessage());
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
        testProduct.add(new Egg(Quality.IRIDIUM));
        testProduct.add(new Egg(Quality.REGULAR));
        testProduct.add(new Egg(Quality.SILVER));
        testProduct.add(new Egg(Quality.GOLD));
        assertEquals(testProduct, inventory.getAllProducts());
    }

    @Test
    public void testRemoveProductByBarcode() {
        Product egg = new Egg(Quality.REGULAR);
        Product milk = new Milk(Quality.REGULAR);
        inventory.addProduct(egg.getBarcode(), egg.getQuality());
        inventory.addProduct(milk.getBarcode(), milk.getQuality());

        List<Product> removedProducts = inventory.removeProduct(egg.getBarcode());
        assertEquals(1, removedProducts.size());
        assertEquals(egg.getBarcode(), removedProducts.get(0).getBarcode());

        List<Product> remainingProducts = inventory.getAllProducts();
        assertEquals(1, remainingProducts.size());
        assertEquals(milk.getBarcode(), remainingProducts.get(0).getBarcode());
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
        assertEquals(Barcode.EGG, removed.get(0).getBarcode());
        assertEquals(1, removed.size());
        assertTrue(inventory.getAllProducts().isEmpty());
    }

    @Test
    public void testRepeatedAddAndRemoveSameProduct() {
        for (int i = 0; i < 10; i++) {
            inventory.addProduct(Barcode.EGG, Quality.REGULAR);
            List<Product> removed = inventory.removeProduct(Barcode.EGG);
            assertEquals(1, removed.size());
            assertEquals(Barcode.EGG, removed.get(0).getBarcode());
            assertTrue(inventory.getAllProducts().isEmpty());
        }
    }

    @Test
    public void testRemoveNonExistentProduct() {
        inventory.addProduct(Barcode.MILK, Quality.REGULAR);
        List<Product> removedProducts = inventory.removeProduct(Barcode.EGG);
        assertTrue(removedProducts.isEmpty());
        assertEquals(1, inventory.getAllProducts().size());
    }

    @Test
    public void testAddProductWithSameBarcodeDifferentQuality() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.addProduct(Barcode.EGG, Quality.SILVER);
        inventory.addProduct(Barcode.EGG, Quality.GOLD);

        List<Product> allProducts = inventory.getAllProducts();
        assertEquals(3, allProducts.size());

        assertTrue(allProducts.stream().anyMatch(p -> p.getQuality() == Quality.REGULAR));
        assertTrue(allProducts.stream().anyMatch(p -> p.getQuality() == Quality.SILVER));
        assertTrue(allProducts.stream().anyMatch(p -> p.getQuality() == Quality.GOLD));
    }

    @Test
    public void testRemoveMultipleSameProduct() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);

        List<Product> removed = inventory.removeProduct(Barcode.EGG);
        assertEquals(1, removed.size());  // Only one instance should be removed at a time
        assertEquals(Barcode.EGG, removed.get(0).getBarcode());

        List<Product> remainingProducts = inventory.getAllProducts();
        assertEquals(1, remainingProducts.size());  // One instance should remain
    }

    @Test
    public void testAddProductWithNullQuality() {
        inventory.addProduct(Barcode.EGG, null);
    }


    @Test
    public void testRemoveProductWithSameBarcode() {
        Product egg1 = new Egg(Quality.REGULAR);
        Product egg2 = new Egg(Quality.REGULAR);
        inventory.addProduct(egg1.getBarcode(), egg1.getQuality());
        inventory.addProduct(egg2.getBarcode(), egg2.getQuality());

        List<Product> removedProducts = inventory.removeProduct(egg1.getBarcode());
        assertEquals(1, removedProducts.size());
        assertEquals(egg1.getBarcode(), removedProducts.get(0).getBarcode());

        assertEquals(1, inventory.getAllProducts().size());
        assertEquals(egg2.getBarcode(), inventory.getAllProducts().get(0).getBarcode());

        removedProducts = inventory.removeProduct(egg2.getBarcode());
        assertEquals(1, removedProducts.size());
        assertEquals(egg2.getBarcode(), removedProducts.get(0).getBarcode());

        assertTrue(inventory.getAllProducts().isEmpty());
    }

    @Test
    public void testAddLargeNumberOfProducts() {
        for (int i = 0; i < 10000; i++) {
            inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        }
        assertEquals(10000, inventory.getAllProducts().size());
    }

    @Test
    public void testRemoveProductsInDifferentOrder() {
        inventory.addProduct(Barcode.MILK, Quality.REGULAR);
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.addProduct(Barcode.JAM, Quality.REGULAR);

        List<Product> removedMilk = inventory.removeProduct(Barcode.MILK);
        List<Product> removedEgg = inventory.removeProduct(Barcode.EGG);

        assertEquals(1, removedMilk.size());
        assertEquals(Barcode.MILK, removedMilk.get(0).getBarcode());

        assertEquals(1, removedEgg.size());
        assertEquals(Barcode.EGG, removedEgg.get(0).getBarcode());

        List<Product> remainingProducts = inventory.getAllProducts();
        assertEquals(1, remainingProducts.size());
        assertEquals(Barcode.JAM, remainingProducts.get(0).getBarcode());
    }

    @Test
    public void testRemoveProductTwice() {
        inventory.addProduct(Barcode.EGG, Quality.REGULAR);
        inventory.removeProduct(Barcode.EGG);
        List<Product> secondRemoval = inventory.removeProduct(Barcode.EGG);
        assertTrue(secondRemoval.isEmpty());
    }
}