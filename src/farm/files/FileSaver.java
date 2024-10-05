package farm.files;

import farm.core.farmgrid.Grid;

import java.io.*;
import java.util.Map;

public class FileSaver {

    /**
     * Saves the contents of a grid into a specified file.
     * @param filename the String filename to write contents to.
     * @param grid the grid to be saved.
     * @param farmType the type of farm (plant, animal).
     * @throws IOException
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
