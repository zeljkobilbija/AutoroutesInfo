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
            new Autoput("A1", "Paris – Lille", "autoputevi/a1.json"),
            new Autoput("A2", "Combles – Bruxelles", "autoputevi/a2.json"),
            new Autoput("A4", "Paris – Strasbourg", "autoputevi/a4.json"),
            new Autoput("A5", "Melun – Troyes – Langres", "autoputevi/a5.json"),
            new Autoput("A6", "Paris – Lyon", "autoputevi/a6.json"),
            new Autoput("A7", "Lyon – Marseille", "autoputevi/a7.json"),
            new Autoput("A8", "Aix-en-Provence – Nice – Menton", "autoputevi/a8.json"),
            new Autoput("A9", "Orange – Montpellier – Le Perthus", "autoputevi/a9.json"),
            new Autoput("A10", "Paris – Orléans – Tours – Bordeaux", "autoputevi/a10.json"),
            new Autoput("A11", "Ponthévrard – Le Mans – Angers – Nantes", "autoputevi/a11.json"),
            new Autoput("A13", "Paris – Rouen – Caen", "autoputevi/a13.json"),
            new Autoput("A16", "L'Isle-Adam – Amiens – Calais – Dunkerque", "autoputevi/a16.json"),
            new Autoput("A19", "Sens – Courtenay – Artenay", "autoputevi/a19.json"),
            new Autoput("A20", "Vierzon – Limoges – Brive – Montauban", "autoputevi/a20.json"),
            new Autoput("A23", "Lille – Valenciennes", "autoputevi/a23.json"),
            new Autoput("A25", "Lille – Dunkerque", "autoputevi/a25.json"),
            new Autoput("A26", "Calais – Reims – Troyes", "autoputevi/a26.json"),
            new Autoput("A28", "Abbeville – Rouen – Le Mans – Tours", "autoputevi/a28.json"),
            new Autoput("A29", "Le Havre – Amiens – Saint-Quentin", "autoputevi/a29.json"),
            new Autoput("A31", "Beaune – Nancy – Metz – Luxembourg", "autoputevi/a31.json"),
            new Autoput("A35", "Strasbourg – Colmar – Mulhouse – Bâle", "autoputevi/a35.json"),
            new Autoput("A36", "Beaune – Besançon – Mulhouse", "autoputevi/a36.json"),
            new Autoput("A39", "Dijon – Bourg-en-Bresse", "autoputevi/a39.json"),
            new Autoput("A40", "Mâcon – Genève – Chamonix", "autoputevi/a40.json"),
            new Autoput("A41", "Grenoble – Annecy – Genève", "autoputevi/a41.json"),
            new Autoput("A42", "Lyon – Pont-d'Ain", "autoputevi/a42.json"),
            new Autoput("A43", "Lyon – Chambéry – Fréjus", "autoputevi/a43.json"),
            new Autoput("A48", "Coiranne – Grenoble", "autoputevi/a48.json"),
            new Autoput("A49", "Valence – Grenoble", "autoputevi/a49.json"),
            new Autoput("A50", "Marseille – Toulon", "autoputevi/a50.json"),
            new Autoput("A51", "Marseille – Aix – Sisteron", "autoputevi/a51.json"),
            new Autoput("A54", "Nîmes – Arles – Salon", "autoputevi/a54.json"),
            new Autoput("A57", "Toulon – Le Cannet-des-Maures", "autoputevi/a57.json"),
            new Autoput("A61", "Toulouse – Narbonne", "autoputevi/a61.json"),
            new Autoput("A62", "Bordeaux – Toulouse", "autoputevi/a62.json"),
            new Autoput("A63", "Espagne – Bayonne – Bordeaux", "autoputevi/a63.json"),
            new Autoput("A64", "Bayonne – Pau – Toulouse", "autoputevi/a64.json"),
            new Autoput("A65", "Langon – Pau", "autoputevi/a65.json"),
            new Autoput("A66", "Toulouse – Pamiers", "autoputevi/a66.json"),
            new Autoput("A68", "Toulouse – Albi", "autoputevi/a68.json"),
            new Autoput("A71", "Orléans – Bourges – Clermont-Ferrand", "autoputevi/a71.json"),
            new Autoput("A72", "Nervieux – Saint-Étienne", "autoputevi/a72.json"),
            new Autoput("A75", "Clermont-Ferrand – Millau – Béziers", "autoputevi/a75.json"),
            new Autoput("A77", "Nemours – Nevers", "autoputevi/a77.json"),
            new Autoput("A81", "Le Mans – Laval – La Gravelle", "autoputevi/a81.json"),
            new Autoput("A83", "Nantes – Niort", "autoputevi/a83.json"),
            new Autoput("A84", "Caen – Rennes", "autoputevi/a84.json"),
            new Autoput("A85", "Angers – Tours – Vierzon", "autoputevi/a85.json"),
            new Autoput("A87", "Angers – Cholet – La Roche-sur-Yon", "autoputevi/a87.json"),
            new Autoput("A89", "Bordeaux – Clermont-Ferrand – Lyon", "autoputevi/a89.json")
    );

    public static Autoput poId(String id) {
        for (Autoput a : SVE) if (a.id.equals(id)) return a;
        return null;
    }
}
