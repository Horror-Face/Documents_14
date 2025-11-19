import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Travail {

    static final String DEFAULT_INPUT = "notes_technique_informatique.csv";
    static final String DEFAULT_OUTPUT = "resultats_notes.txt";

    record StudentResult(String prenom, String nom, String codeCours, String nomCours, double note, String notation) {}

    public static void main(String[] args) {
        String input = args.length > 0 ? args[0] : DEFAULT_INPUT;
        String output = DEFAULT_OUTPUT;

        List<StudentResult> results = new ArrayList<>();
        Map<String, Integer> echecsParCours = new LinkedHashMap<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(input))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                Optional<StudentResult> opt = parseLine(line);
                if (opt.isEmpty()) continue;
                StudentResult sr = opt.get();
                results.add(sr);
                echecsParCours.putIfAbsent(sr.codeCours(), 0);
                if (sr.note() < 60) {
                    echecsParCours.computeIfPresent(sr.codeCours(), (k, v) -> v + 1);
                }
            }
        } catch (NoSuchFileException e) {
            System.err.println("Fichier introuvable: " + input);
            return;
        } catch (IOException e) {
            System.err.println("Erreur lecture: " + e.getMessage());
            return;
        }

        results.sort(Comparator.comparing(StudentResult::codeCours).thenComparing(StudentResult::nom).thenComparing(StudentResult::prenom));

        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(output))) {
            bw.write("Prenom\tNom\tCodeCours\tNomCours\tNote%\tNotationLiberale");
            bw.newLine();
            for (StudentResult r : results) {
                bw.write(String.join("\t",
                        r.prenom(), r.nom(), r.codeCours(), r.nomCours(),
                        String.format(Locale.ROOT, "%.2f", r.note()), r.notation()));
                bw.newLine();
            }
            bw.newLine();
            bw.write("Résumé des échecs par cours:");
            bw.newLine();
            for (var e : echecsParCours.entrySet()) {
                bw.write(e.getKey() + " : " + e.getValue() + " échecs");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erreur écriture: " + e.getMessage());
            return;
        }

        long valides = results.size();
        long total = valides;
        long passes = results.stream().filter(r -> r.note() >= 60).count();
        OptionalDouble moyenne = results.stream().mapToDouble(StudentResult::note).average();

        System.out.println("Traitement terminé. Lignes valides: " + total);
        System.out.println("Réussite: " + passes + " / " + total);
        System.out.println("Moyenne générale: " + (moyenne.isPresent() ? String.format(Locale.ROOT, "%.2f", moyenne.getAsDouble()) : "N/A"));
        System.out.println("Fichier créé: " + output);
    }

    private static Optional<StudentResult> parseLine(String line) {
        String[] parts = splitCsvLine(line);
        if (parts.length < 6) return Optional.empty();
        String prenom = parts[0].trim();
        String nom = parts[1].trim();
        String codeCours = parts[2].trim();
        String nomCours = parts[3].trim();
        String noteStr = parts[5].trim();
        double note;
        try {
            note = Double.parseDouble(noteStr);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        String notation = notationLiberale(note);
        return Optional.of(new StudentResult(prenom, nom, codeCours, nomCours, note, notation));
    }

    public static String notationLiberale(double note) {
        if (note >= 90) return "A";
        if (note >= 85) return "A-";
        if (note >= 80) return "B+";
        if (note >= 75) return "B";
        if (note >= 70) return "B-";
        if (note >= 65) return "C+";
        if (note >= 60) return "C";
        if (note >= 50) return "D";
        return "E";
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
