package com.zeljkobilbija001.autoroutesinfo;

import java.util.Arrays;
import java.util.List;

/**
 * Spisak francuskih autoroutes. Novi autoput: dodaj JSON u assets/autoputevi
 * (node alati/autoroutes_skripta.js A6) i upiši ime datoteke umesto null.
 */
public final class Autoputevi {

    private Autoputevi() { }

    public static final List<Autoput> SVE = Arrays.asList(
            new Autoput("A1", "Paris – Lille", null),
            new Autoput("A2", "Combles – Bruxelles", null),
            new Autoput("A4", "Paris – Strasbourg", null),
            new Autoput("A5", "Paris – Langres", null),
            new Autoput("A6", "Paris – Lyon", null),
            new Autoput("A7", "Lyon – Marseille", null),
            new Autoput("A8", "Aix-en-Provence – Nice – Menton", null),
            new Autoput("A9", "Orange – Montpellier – Le Perthus", null),
            new Autoput("A10", "Paris – Orléans – Tours – Bordeaux", null),
            new Autoput("A11", "Paris – Le Mans – Angers – Nantes", null),
            new Autoput("A13", "Paris – Rouen – Caen", null),
            new Autoput("A16", "L'Isle-Adam – Amiens – Calais – Dunkerque", null),
            new Autoput("A19", "Artenay – Courtenay", null),
            new Autoput("A20", "Vierzon – Limoges – Brive – Montauban", null),
            new Autoput("A23", "Lille – Valenciennes", null),
            new Autoput("A25", "Lille – Dunkerque", null),
            new Autoput("A26", "Calais – Reims – Troyes", null),
            new Autoput("A28", "Abbeville – Rouen – Le Mans – Tours", null),
            new Autoput("A29", "Le Havre – Amiens – Saint-Quentin", null),
            new Autoput("A31", "Beaune – Nancy – Metz – Luxembourg", null),
            new Autoput("A35", "Lauterbourg – Strasbourg – Bâle", null),
            new Autoput("A36", "Beaune – Besançon – Mulhouse", null),
            new Autoput("A39", "Dijon – Bourg-en-Bresse", null),
            new Autoput("A40", "Mâcon – Genève – Chamonix", null),
            new Autoput("A41", "Grenoble – Annecy – Genève", null),
            new Autoput("A42", "Lyon – Pont-d'Ain", null),
            new Autoput("A43", "Lyon – Chambéry – Fréjus", null),
            new Autoput("A48", "Lyon – Grenoble", null),
            new Autoput("A49", "Grenoble – Valence", null),
            new Autoput("A50", "Marseille – Toulon", null),
            new Autoput("A51", "Marseille – Aix – Sisteron", null),
            new Autoput("A54", "Nîmes – Arles – Salon", null),
            new Autoput("A57", "Toulon – Le Cannet-des-Maures", null),
            new Autoput("A61", "Toulouse – Narbonne", null),
            new Autoput("A62", "Bordeaux – Toulouse", null),
            new Autoput("A63", "Bordeaux – Bayonne – Espagne", null),
            new Autoput("A64", "Bayonne – Pau – Toulouse", null),
            new Autoput("A65", "Langon – Pau", null),
            new Autoput("A66", "Toulouse – Pamiers", null),
            new Autoput("A68", "Toulouse – Albi", null),
            new Autoput("A71", "Orléans – Bourges – Clermont-Ferrand", null),
            new Autoput("A72", "Clermont-Ferrand – Saint-Étienne", null),
            new Autoput("A75", "Clermont-Ferrand – Millau – Béziers", null),
            new Autoput("A77", "Nemours – Nevers", null),
            new Autoput("A81", "Le Mans – Laval – Rennes", null),
            new Autoput("A83", "Nantes – Niort", null),
            new Autoput("A84", "Caen – Rennes", null),
            new Autoput("A85", "Angers – Tours – Vierzon", null),
            new Autoput("A87", "Angers – Cholet – La Roche-sur-Yon", null),
            new Autoput("A89", "Bordeaux – Clermont-Ferrand – Lyon", null)
    );

    public static Autoput poId(String id) {
        for (Autoput a : SVE) if (a.id.equals(id)) return a;
        return null;
    }
}
