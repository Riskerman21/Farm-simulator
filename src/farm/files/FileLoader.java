package farm.files;

import farm.core.farmgrid.FarmGrid;
import farm.core.farmgrid.Grid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class that loads a saved text file of the grid into a gird in the game
 */
public class FileLoader {
    /**
     * Constructor for the FileLoader
     */
    public FileLoader() {
    }

    /**
     * Loads contents of the specified file into a Grid.
     * @param filename the String filename to read contents from.
     * @return a grid instance.
     * @throws IOException if the file doesn't exist
     */
    public Grid load(String filename) throws IOException {
        String farmType;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            farmType = reader.readLine();
            int rows = 0;
            int columns = 0;
            List<List<String>> info = new ArrayList<>();


            if ((line = reader.readLine()) != null) {
                rows = Integer.parseInt(line.split(": ")[1]);
            }

            if ((line = reader.readLine()) != null) {
                columns = Integer.parseInt(line.split(": ")[1]);
            }

            if ((line = reader.readLine()) != null) {
                int index = line.indexOf(": ");
                String data = "";
                if (index != -1) {
                    data = line.substring(index + 2).trim();
                }

                if (!data.isEmpty()) {
                    data = data.substring(2, data.length() - 2);
                    String[] entries = data.split("], \\[");

                    for (String entry : entries) {
                        String[] attributes = entry.split(", ");
                        List<String> itemInfo = new ArrayList<>();
                        for (String attr : attributes) {
                            itemInfo.add(attr.trim());
                        }
                        info.add(itemInfo);
                    }
                }
            }

            Grid grid = new FarmGrid(rows, columns, farmType);
            int i = 0;
            int j = 0;
            for (List<String> tile : info) {
                if (j == rows) {
                    j = 0;
                    i++;
                }
                if (!tile.getFirst().equals("ground")) {
                    grid.place(i, j, tile.get(1).charAt(0));
                }
                j++;
            }
            return grid;
        }
    }
}
