package com.zeljkobilbija001.autoroutesinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jedna saobraćajna situacija iz Bison Futé (DATEX II, državna mreža bez naplate; Licence Ouverte 2.0).
 * Situacija ima jedan ili više zapisa (Accident, AbnormalTraffic, radovi, zatvaranja…), početnu i krajnju
 * tačku sa koordinatama (od → do = smer vožnje) i opis na francuskom ("De Lyon vers Marseille").
 */
public final class Dogadjaj {

    /** Vrste, poređane po važnosti (manji broj = važnije). */
    public static final int ZATVORENO = 0, UDES = 1, OPASNOST = 2, KOLONA = 3,
            TRAKA = 4, SPORO = 5, PRILAZ = 6, OSTALO = 7;

    public final String id;
    public final int vrsta;
    public final String deonica;   // "Aire de Sérézin – A7 / A46 / A47" (može biti prazno)
    public final String smer;      // "→ Marseille"
    public final String opis;      // francuski tekst Bison Futé-a
    public final int kasnjenjeMin; // Bison Futé ga ne daje: uvek 0
    public final long pocetak;     // ms, 0 = nepoznato
    public final double lat, lon;  // početak (od); NaN ako nema koordinata
    public final double lat2, lon2; // kraj (do, određuje smer); NaN ako je tačka
    private final boolean bezObavestenja;

    private Dogadjaj(String id, int vrsta, String deonica, String smer, String opis, long pocetak,
                     double lat, double lon, double lat2, double lon2, boolean bezObavestenja) {
        this.id = id;
        this.vrsta = vrsta;
        this.deonica = deonica;
        this.smer = smer;
        this.opis = opis;
        this.kasnjenjeMin = 0;
        this.pocetak = pocetak;
        this.lat = lat;
        this.lon = lon;
        this.lat2 = lat2;
        this.lon2 = lon2;
        this.bezObavestenja = bezObavestenja;
    }

    /** Da li zbog ove poruke vredi poslati obaveštenje (udes, kolona, zatvoren put, opasnost). */
    public boolean zaObavestenje() {
        if (bezObavestenja) return false; // vozilo u kvaru: samo u listi (kao u Italiji)
        return vrsta == ZATVORENO || vrsta == UDES || vrsta == OPASNOST || vrsta == KOLONA;
    }

    public boolean imaKoordinate() {
        return !Double.isNaN(lat) && !Double.isNaN(lon);
    }

    // ---------- Čitanje jedne situacije (DATEX II) ----------

    private static final Pattern ZAPIS = Pattern.compile(
            "<ns2:situationRecord [^>]*xsi:type=\"ns2:(\\w+)\"[^>]*>([\\s\\S]*?)</ns2:situationRecord>");
    private static final Pattern PUT = Pattern.compile("<ns2:roadNumber>\\s*([A-Z])0*(\\d+[A-Za-z]?)\\s*</ns2:roadNumber>");
    private static final Pattern POCETAK = Pattern.compile("<ns2:overallStartTime>([^<]+)</ns2:overallStartTime>");
    private static final Pattern KRAJ = Pattern.compile("<ns2:overallEndTime>([^<]+)</ns2:overallEndTime>");
    private static final Pattern KOMENTAR = Pattern.compile(
            "<ns2:generalPublicComment>[\\s\\S]*?<ns2:value[^>]*>([^<]*)</ns2:value>");
    private static final Pattern OD = Pattern.compile("<ns2:from [^>]*>[\\s\\S]*?<ns2:latitude>([-\\d.]+)</ns2:latitude>\\s*<ns2:longitude>([-\\d.]+)</ns2:longitude>");
    private static final Pattern DO = Pattern.compile("<ns2:to [^>]*>[\\s\\S]*?<ns2:latitude>([-\\d.]+)</ns2:latitude>\\s*<ns2:longitude>([-\\d.]+)</ns2:longitude>");
    private static final Pattern TACKA = Pattern.compile("<ns2:latitude>([-\\d.]+)</ns2:latitude>\\s*<ns2:longitude>([-\\d.]+)</ns2:longitude>");
    private static final Pattern MESTO_C = Pattern.compile(
            "<ns2:alertCMethod4(Primary|Secondary)PointLocation>[\\s\\S]*?<ns2:alertCLocationName>[\\s\\S]*?<ns2:value[^>]*>([^<]*)</ns2:value>");
    private static final Pattern VERS = Pattern.compile("^De (.+?) vers (.+)$");

    /** Oznake puteva iz situacije: "A7", "N20". */
    static Set<String> putevi(String sit) {
        Set<String> s = new LinkedHashSet<>();
        Matcher m = PUT.matcher(sit);
        while (m.find()) s.add(m.group(1) + m.group(2).toUpperCase(Locale.ROOT));
        return s;
    }

    /** null ako situacija nije aktuelna (planirani radovi u budućnosti, istekla) ili nije stvarna. */
    static Dogadjaj izDatex(String id, String sit, long sad) {
        if (sit.contains("<ns2:informationStatus>") && !sit.contains("<ns2:informationStatus>real")) return null;
        long pocetak = 0, kraj = 0;
        Matcher mp = POCETAK.matcher(sit);
        while (mp.find()) {
            long t = vreme(mp.group(1));
            if (t > 0 && (pocetak == 0 || t < pocetak)) pocetak = t;
        }
        Matcher mk = KRAJ.matcher(sit);
        while (mk.find()) kraj = Math.max(kraj, vreme(mk.group(1)));
        if (pocetak > sad + 10 * 60_000L) return null; // planirano, još nije počelo
        if (kraj > 0 && kraj < sad) return null;

        int vrsta = OSTALO;
        boolean samoKvar = true;
        List<String> naziviZapisa = new ArrayList<>();
        Matcher mz = ZAPIS.matcher(sit);
        while (mz.find()) {
            String tip = mz.group(1), z = mz.group(2);
            int v = vrsta(tip, z);
            vrsta = Math.min(vrsta, v);
            if (!(tip.equals("VehicleObstruction") && z.contains("brokenDownVehicle"))
                    && v <= KOLONA) samoKvar = false;
            String n = naziv(tip, z);
            if (!naziviZapisa.contains(n)) naziviZapisa.add(n);
        }

        // opis: vrste zapisa + javni komentari (bez internih oznaka službe, npr. "DIR Centre-Est/District Lyon")
        Set<String> komentari = new LinkedHashSet<>();
        String smer = "";
        Matcher mc = KOMENTAR.matcher(sit);
        while (mc.find()) {
            String k = xml(mc.group(1)).trim();
            if (k.isEmpty() || k.startsWith("DIR ") || k.startsWith("Point particulier")) continue;
            Matcher v = VERS.matcher(k);
            if (v.matches() && smer.isEmpty()) smer = "→ " + v.group(2).trim();
            komentari.add(k);
        }
        StringBuilder opis = new StringBuilder(String.join(" · ", naziviZapisa));
        for (String k : komentari) opis.append(opis.length() > 0 ? ". " : "").append(k);

        // deonica: mesta po tabeli lokacija (sekundarno = početak, primarno = kraj)
        String prim = "", sek = "";
        Matcher ml = MESTO_C.matcher(sit);
        while (ml.find()) {
            if ("Primary".equals(ml.group(1)) && prim.isEmpty()) prim = xml(ml.group(2)).trim();
            if ("Secondary".equals(ml.group(1)) && sek.isEmpty()) sek = xml(ml.group(2)).trim();
        }
        String deonica = sek.isEmpty() || sek.equals(prim) ? prim : prim.isEmpty() ? sek : sek + " – " + prim;

        double lat = Double.NaN, lon = Double.NaN, lat2 = Double.NaN, lon2 = Double.NaN;
        Matcher mo = OD.matcher(sit), md = DO.matcher(sit);
        if (mo.find() && md.find()) {
            lat = broj(mo.group(1));
            lon = broj(mo.group(2));
            lat2 = broj(md.group(1));
            lon2 = broj(md.group(2));
            if (Math.abs(lat - lat2) < 1e-5 && Math.abs(lon - lon2) < 1e-5) lat2 = lon2 = Double.NaN;
        } else {
            Matcher mt = TACKA.matcher(sit);
            if (mt.find()) {
                lat = broj(mt.group(1));
                lon = broj(mt.group(2));
            }
        }
        return new Dogadjaj(id, vrsta, deonica, smer, opis.toString(), pocetak, lat, lon, lat2, lon2, samoKvar);
    }

    /**
     * Događaj u jednoj tački sa smerom samo u tekstu ("De Marseille vers Lyon"): druga tačka 5 km dalje
     * u tom smeru duž autoputa (krajevi autoputa ili izlaz sa tim imenom), da ga Vožnja ne javlja i
     * vozačima iz suprotnog smera. Bez podataka ili ako se smer ne prepozna, ostaje kakav jeste.
     */
    Dogadjaj saSmerom(Podaci p) {
        if (p == null || !Double.isNaN(lat2) || !imaKoordinate() || smer.length() < 3) return this;
        String cilj = smer.substring(2).trim();
        Stavka naj = null;
        double nd = Double.MAX_VALUE;
        for (Stavka s : p.stavke) {
            if (!s.imaKoordinate()) continue;
            double d = km(lat, lon, s.lat, s.lon);
            if (d < nd) {
                nd = d;
                naj = s;
            }
        }
        if (naj == null || nd > 3 || naj.deonica < 0 || naj.deonica >= p.deonice.size()) return this;
        Deonica d = p.deonice.get(naj.deonica);
        Set<String> r = reci(cilj);
        boolean plus = !java.util.Collections.disjoint(r, reci(d.plus));
        boolean minus = !java.util.Collections.disjoint(r, reci(d.minus));
        if (plus == minus) {
            // grad nije kraj autoputa: izlaz sa tim imenom ispred ili iza
            Stavka c = null;
            for (Stavka s : p.stavke) {
                if (s.deonica == naj.deonica && Math.abs(s.km - naj.km) > 1
                        && !java.util.Collections.disjoint(r, reci(s.naziv))) {
                    if (c == null || Math.abs(s.km - naj.km) < Math.abs(c.km - naj.km)) c = s;
                }
            }
            if (c != null) {
                plus = c.km > naj.km;
                minus = !plus;
            }
        }
        if (plus == minus) return this;
        double[] t = naKm(p, naj.deonica, naj.km + (plus ? 5 : -5));
        if (t == null) return this;
        return new Dogadjaj(id, vrsta, deonica, smer, opis, pocetak, lat, lon, t[0], t[1], bezObavestenja);
    }

    /** Tačka na autoputu na datom km (linearno između susednih mesta sa koordinatama). */
    private static double[] naKm(Podaci p, int deonica, double km) {
        Stavka pre = null, posle = null;
        for (Stavka s : p.stavke) {
            if (s.deonica != deonica || !s.imaKoordinate()) continue;
            if (s.km <= km && (pre == null || s.km > pre.km)) pre = s;
            if (s.km >= km && (posle == null || s.km < posle.km)) posle = s;
        }
        if (pre == null && posle == null) return null;
        if (pre == null) return new double[]{posle.lat, posle.lon};
        if (posle == null || posle.km - pre.km < 0.01) return new double[]{pre.lat, pre.lon};
        double u = (km - pre.km) / (posle.km - pre.km);
        return new double[]{pre.lat + u * (posle.lat - pre.lat), pre.lon + u * (posle.lon - pre.lon)};
    }

    private static double km(double la1, double lo1, double la2, double lo2) {
        double x = Math.toRadians(lo2 - lo1) * Math.cos(Math.toRadians((la1 + la2) / 2));
        double y = Math.toRadians(la2 - la1);
        return 6371 * Math.sqrt(x * x + y * y);
    }

    private static Set<String> reci(String s) {
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        Set<String> r = new java.util.HashSet<>();
        for (String w : n.split("[^a-z0-9]+")) {
            if (w.length() >= 3 && !w.equals("sur") && !w.equals("les") && !w.equals("des")) r.add(w);
        }
        return r;
    }

    static int vrsta(String tip, String z) {
        switch (tip) {
            case "Accident":
                return UDES;
            case "AbnormalTraffic":
                return z.contains("slowTraffic") || z.contains("heavyTraffic") ? SPORO : KOLONA;
            case "RoadOrCarriagewayOrLaneManagement":
                if (z.contains("SlipRoad") || z.contains("exit") && z.contains("Closed")) return PRILAZ;
                if (z.contains("roadClosed") || z.contains("carriagewayClosures")) return ZATVORENO;
                return TRAKA;
            case "VehicleObstruction":
            case "GeneralObstruction":
            case "AnimalPresenceObstruction":
            case "EnvironmentalObstruction":
            case "InfrastructureDamageObstruction":
            case "PoorEnvironmentConditions":
            case "WeatherRelatedRoadConditions":
            case "NonWeatherRelatedRoadConditions":
                return OPASNOST;
            case "MaintenanceWorks":
            case "ConstructionWorks":
            case "RoadworksManagement":
                return TRAKA;
            default:
                return OSTALO;
        }
    }

    /** Francuski naziv zapisa za opis. */
    private static String naziv(String tip, String z) {
        switch (tip) {
            case "Accident": return "Accident";
            case "AbnormalTraffic":
                return z.contains("slowTraffic") || z.contains("heavyTraffic") ? "Ralentissement" : "Bouchon";
            case "RoadOrCarriagewayOrLaneManagement":
                if (z.contains("roadClosed") || z.contains("carriagewayClosures")) return "Route fermée";
                if (z.contains("laneClosures")) return "Voie(s) fermée(s)";
                return "Restriction de circulation";
            case "VehicleObstruction":
                return z.contains("brokenDownVehicle") ? "Véhicule en panne" : "Véhicule arrêté";
            case "GeneralObstruction": return "Obstacle";
            case "AnimalPresenceObstruction": return "Animaux sur la chaussée";
            case "EnvironmentalObstruction": return "Obstacle naturel";
            case "InfrastructureDamageObstruction": return "Dégâts sur la chaussée";
            case "PoorEnvironmentConditions":
            case "WeatherRelatedRoadConditions": return "Conditions météo";
            case "NonWeatherRelatedRoadConditions": return "Chaussée dangereuse";
            case "MaintenanceWorks":
            case "ConstructionWorks": return "Travaux";
            case "SpeedManagement": return "Limitation de vitesse";
            case "ReroutingManagement": return "Déviation";
            default: return "Information";
        }
    }

    private static double broj(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static long vreme(String iso) {
        String s = iso.trim();
        for (String f : new String[]{"yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX"}) {
            try {
                Date d = new SimpleDateFormat(f, Locale.US).parse(s);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private static String xml(String t) {
        return t.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&#39;", "'").replace("&apos;", "'").replace("&amp;", "&");
    }
}
