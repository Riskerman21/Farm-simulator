package farm.files;

import farm.core.farmgrid.Grid;

import java.io.*;

/**
 * A class that saves information from the gird into a text file to be stored
 */
public class FileSaver {

    /**
     * Saves the contents of a grid into a specified file.
     * @param filename the String filename to write contents to.
     * @param grid the grid to be saved.
     * @throws IOException if the file doesn't exist
     */
    public void save(String filename, Grid grid) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(grid.toString());
            writer.newLine();
            writer.write("Rows: " + String.valueOf(grid.getRows()));
            writer.newLine();
            writer.write("Columns: " + String.valueOf(grid.getColumns()));
            writer.newLine();
            writer.write("Stats: " + grid.getStats().toString());
            writer.newLine();
            writer.flush();
        }
    }
}
