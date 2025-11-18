
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Travail {

    public static String notationLiberale(double note) {
        if (note >= 90) {
            return "A";
        }
        if (note >= 85) {
            return "A-";
        }
        if (note >= 80) {
            return "B+";
        }
        if (note >= 75) {
            return "B";
        }
        if (note >= 70) {
            return "B-";
        }
        if (note >= 65) {
            return "C+";
        }
        if (note >= 60) {
            return "C";
        }
        if (note >= 50) {
            return "D";
        }
        return "E";
    }

    public static void main(String[] args) {
        String input = "notes_technique_informatique.csv";
        if (args.length > 0) {
            input = args[0];
        }
        String output = "resultats_notes.txt";
        List<String[]> rows = new ArrayList<>();
        Map<String, Integer> echecsParCours = new LinkedHashMap<>();
        int totalLignes = 0;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(input))) {
            String line = br.readLine();
            if (line == null) {
                System.err.println("Fichier vide.");
                return;
            }
            while ((line = br.readLine()) != null) {
                totalLignes++;
                String[] parts = splitCsvLine(line);
                if (parts.length < 6) {
                    continue;
                }
                String prenom = parts[0].trim();
                String nom = parts[1].trim();
                String codeCours = parts[2].trim();
                String nomCours = parts[3].trim();
                String notationStr = parts[4].trim();
                String noteStr = parts[5].trim();
                double note;
                try {
                    note = Double.parseDouble(noteStr);
                } catch (NumberFormatException e) {
                    continue;
                }
                String notationLib = notationLiberale(note);
                rows.add(new String[]{prenom, nom, codeCours, nomCours, noteStr, notationLib});
                echecsParCours.putIfAbsent(codeCours, 0);
                if (note < 60) {
                    echecsParCours.put(codeCours, echecsParCours.get(codeCours) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lecture fichier: " + e.getMessage());
            return;
        }
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(output))) {
            bw.write("Prenom\tNom\tCodeCours\tNomCours\tNote%\tNotationLiberale");
            bw.newLine();
            for (String[] r : rows) {
                bw.write(String.join("\t", r));
                bw.newLine();
            }
            bw.newLine();
            bw.write("Résumé des échecs par cours:");
            bw.newLine();
            for (Map.Entry<String, Integer> e : echecsParCours.entrySet()) {
                bw.write(e.getKey() + " : " + e.getValue() + " échecs");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur écriture fichier: " + e.getMessage());
            return;
        }
        System.out.println("Traitement terminé. Lignes valides: " + rows.size());
        System.out.println("Fichier de sortie: " + output);
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }
}
