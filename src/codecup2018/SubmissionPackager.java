package codecup2018;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubmissionPackager {

    private final String outputFileName;
    private static final boolean MINIMIZE = true;

    public static void main(String[] args) throws IOException {
        Path mainClass = Paths.get("src", "codecup2018", "Runner.java");
        List<Path> inputFiles = Arrays.asList(
                Paths.get("src", "codecup2018", "Board.java"),
                Paths.get("src", "codecup2018", "player", "Player.java"),
                Paths.get("src", "codecup2018", "player", "SimpleMaxPlayer.java"),
                Paths.get("src", "codecup2018", "evaluator", "Evaluator.java"),
                Paths.get("src", "codecup2018", "evaluator", "ExpectedValue.java"),
                Paths.get("src", "codecup2018", "movegenerator", "MoveGenerator.java"),
                Paths.get("src", "codecup2018", "movegenerator", "AllMoves.java"),
                mainClass
        );
        Path outputFile = Paths.get("src", "Expy.java");

        new SubmissionPackager(inputFiles, mainClass, outputFile);
    }

    public SubmissionPackager(List<Path> inputFiles, Path mainClass, Path outputFile) throws IOException {
        outputFileName = outputFile.getFileName().toString();
        
        // Gather all necessary imports
        Set<String> imports = gatherNecessaryImports(inputFiles);

        // Write everything
        try (BufferedWriter out = Files.newBufferedWriter(outputFile)) {
            // Imports first
            for (String line : imports) {
                out.write(line);
                out.newLine();
            }

            if (!MINIMIZE) {
                out.newLine();
            }

            // Then the main class
            appendClass(mainClass, out, true);

            // Finally the others
            for (Path file : inputFiles) {
                if (file != mainClass) {
                    appendClass(file, out, false);
                }
            }
        }
    }

    private Set<String> gatherNecessaryImports(List<Path> sourceFiles) throws IOException {
        // Add all imports that aren't of included classes
        HashSet<String> imports = new HashSet<>();

        for (Path file : sourceFiles) {
            try (BufferedReader in = Files.newBufferedReader(file)) {
                String line = in.readLine();
                
                while (line != null) {
                    if (line.contains("import java") || line.contains("import sun")) {
                        imports.add(line);
                    }
                    
                    if (line.contains("{")) {
                        // Class starts; no imports after this
                        break;
                    }
                    
                    line = in.readLine();
                }
            }
        }

        return imports;
    }

    private void appendClass(Path sourceFile, BufferedWriter out, boolean isMainClass) throws IOException {
        try (BufferedReader in = Files.newBufferedReader(sourceFile)) {
            String line = in.readLine();
            
            // Skip to the start of the class
            while (line != null && !line.contains("{")) {
                line = in.readLine();
            }
            
            if (line == null) {
                return;
            }
            
            // Remove 'public' if this is not the main class
            if (isMainClass) {
                // Replace the name of the main class with the name of the output file
                String fileName = sourceFile.getFileName().toString();
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                out.write(line.replace(className, outputFileName.substring(0, outputFileName.lastIndexOf('.'))));
                out.newLine();
            } else {
                out.write(line.trim().substring("public ".length()));
                out.newLine();
            }
            
            // Copy the remainder of the file
            line = in.readLine();
            
            while (line != null) {
                if (MINIMIZE) {
                    // Remove indentation and comments
                    line = pack(line);
                    
                    while (line.contains("/*") && line.contains("*/")) {
                        int start = line.indexOf("/*");
                        int end = line.indexOf("*/", start + 2);
                        
                        if (end != -1) {
                            line = line.substring(0, start).trim() + " " + line.substring(end + 2).trim();
                        }
                    }
                    
                    if (line.contains("/*")) {
                        // Start of a multi-line block comment
                        line = line.substring(0, line.indexOf("/*"));
                        String temp = in.readLine();
                        
                        while (!temp.contains("*/")) {
                            temp = in.readLine();
                        }
                        
                        temp = temp.substring(temp.indexOf("*/") + 2);
                        
                        if (temp.contains("/*")) {
                            System.err.println("WARNING: new block comment starting on the same line where the previous finished. This case isn't handled correctly by the code minimizer. It is recommended that you start the new block comment on the next line instead.");
                        }
                        
                        line += pack(temp);
                    }
                }
                
                // Skip empty lines when minimizing
                if (!MINIMIZE || !line.isEmpty()) {
                    out.write(line);
                    out.newLine();
                }
                
                line = in.readLine();
            }
            
            if (!MINIMIZE) {
                out.newLine();
            }
        }
    }

    private String pack(String line) {
        return line.replaceAll("//.*$", "").replaceAll("([\\(\\)\\{\\}\\[\\],+\\-/%*=?:;<>|\\&\\^!]) ", "$1").replaceAll(" ([\\(\\)\\{\\}\\[\\],+\\-/%*=?:;<>|\\&\\^!])", "$1").trim();
    }
}
