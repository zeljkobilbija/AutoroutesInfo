package com.zeljkobilbija001.autoroutesinfo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stanje na putu za jednu autoroute: događaji Bison Futé-a (Direction des infrastructures de transport,
 * DATEX II, Licence Ouverte 2.0) – samo državna mreža bez naplate (réseau non concédé). Autoputevi sa
 * naplatom (Vinci, APRR, Sanef…) objavljuju se samo uz pristup „Action b“ i ovde ih zasad nema.
 * Fajl je ~2,7 MB, ali server šalje gzip (~130 KB); čuva se 60 s, a uz ETag se nepromenjen ne preuzima ponovo.
 */
public final class StanjeNaPutu {

    public interface Odgovor {
        /** Poziva se na glavnoj niti; null ako podaci nisu stigli. */
        void stiglo(List<Dogadjaj> dogadjaji);
    }

    private static final String IZVOR =
            "https://tipi.bison-fute.gouv.fr/bison-fute-ouvert/publicationsDIR/Evenementiel-DIR/grt/RRN/content.xml";
    private static final long KES_MS = 60_000L;
    private static final ExecutorService NIT = Executors.newSingleThreadExecutor();
    private static final Pattern SITUACIJA = Pattern.compile(
            "<ns2:situation id=\"([^\"]+)\"[^>]*>([\\s\\S]*?)</ns2:situation>");

    private static String kesTekst, kesEtag;
    private static long kesVreme;

    private StanjeNaPutu() { }

    public static void ucitaj(Context c, String autoput, Odgovor odgovor) {
        Handler glavna = new Handler(Looper.getMainLooper());
        NIT.execute(() -> {
            List<Dogadjaj> l;
            try {
                l = preuzmi(c, autoput);
            } catch (Exception e) {
                l = null;
            }
            List<Dogadjaj> konacno = l;
            glavna.post(() -> odgovor.stiglo(konacno));
        });
    }

    /**
     * Preuzimanje na tekućoj niti (i za proveru u pozadini). Najnovija poruka ide prva bez obzira
     * na važnost (važnost pokazuje boja), poruke bez vremena na kraj; isto vreme: važnija prva.
     */
    public static List<Dogadjaj> preuzmi(Context c, String autoput) throws Exception {
        String xml = podaci();
        long sad = System.currentTimeMillis();
        Autoput ap = Autoputevi.poId(autoput);
        Podaci p = ap == null ? null : Podaci.za(c, ap);
        List<Dogadjaj> l = new ArrayList<>();
        Matcher m = SITUACIJA.matcher(xml);
        while (m.find()) {
            String sit = m.group(2);
            if (!Dogadjaj.putevi(sit).contains(autoput.toUpperCase(java.util.Locale.ROOT))) continue;
            Dogadjaj d = Dogadjaj.izDatex(m.group(1), sit, sad);
            if (d != null) l.add(d.saSmerom(p));
        }
        Collections.sort(l, (a, b) -> a.pocetak != b.pocetak ? Long.compare(b.pocetak, a.pocetak)
                : Integer.compare(a.vrsta, b.vrsta));
        return l;
    }

    private static synchronized String podaci() throws Exception {
        long sad = System.currentTimeMillis();
        if (kesTekst != null && sad - kesVreme < KES_MS) return kesTekst;
        HttpURLConnection v = (HttpURLConnection) new URL(IZVOR).openConnection();
        v.setConnectTimeout(10000);
        v.setReadTimeout(30000);
        v.setRequestProperty("User-Agent", "AutoroutesInfo-Android");
        v.setRequestProperty("Accept", "application/xml, text/xml");
        // gzip: HttpURLConnection ga sam traži i raspakuje (ne postavljati Accept-Encoding ručno)
        if (kesTekst != null && kesEtag != null) v.setRequestProperty("If-None-Match", kesEtag);
        try {
            int kod = v.getResponseCode();
            if (kod == 304 && kesTekst != null) {
                kesVreme = sad;
                return kesTekst;
            }
            if (kod != 200) throw new Exception("HTTP " + kod);
            StringBuilder sb = new StringBuilder(3_000_000);
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(v.getInputStream(), StandardCharsets.UTF_8))) {
                char[] buf = new char[65536];
                int n;
                while ((n = r.read(buf)) > 0) sb.append(buf, 0, n);
            }
            kesTekst = sb.toString();
            kesEtag = v.getHeaderField("ETag");
            kesVreme = sad;
            return kesTekst;
        } finally {
            v.disconnect();
        }
    }
}
