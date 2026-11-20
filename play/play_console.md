# Autoroutes Info – Play Console

Paket: `com.zeljkobilbija001.autoroutesinfo` · prva verzija 1.0 (versionCode 1)
Postupak: podsetnik, poglavlje 4a (A → G). AdMob: JEDNA aplikacija, posle objave „Aggiungi store“ na istoj.

## Crea app
- Nome dell'app: **Autoroutes Info : sorties** (iz `play/opis/fr-FR.txt`)
- Lingua predefinita: **Francese – fr-FR**
- App o gioco: **App** · Senza costi o a pagamento: **Senza costi** (kupovina „Bez reklama“ je unutar aplikacije)
- Čekirati izjave (Norme del programma per gli sviluppatori, leggi sull'esportazione USA)

## Listing prodavnice (Scheda dello store predefinita)
- Tekstovi: `play/opis/fr-FR.txt` (glavni), `play/opis/en-US.txt` (Gestisci traduzioni → Inglese (Stati Uniti) – en-US)
- Ikona 512×512: `play/grafika/ikona_512.png`
- Grafica in primo piano 1024×500: `play/grafika/naslovna_1024x500.png`
- Snimci ekrana telefona: `play/snimci/fr_1..5.png` (francuski listing, aplikacija na francuskom), `en_1..5.png` (engleski prevod)
- Categoria: **Mappe e navigatori** · Tag po želji: Viaggi, Navigazione
- Email za kontakt: isti kao za ostale aplikacije · Sito web: `https://zeljkobilbija.github.io`

## Contenuti dell'app (`…/app/<broj aplikacije>/app-content`)
| Stavka | Odgovor |
|---|---|
| Norme sulla privacy | `https://zeljkobilbija.github.io/autoroutesinfo/privacy.html` |
| Annunci | **Sì, contiene annunci** |
| ID pubblicità | **Sì** → Pubblicità o marketing |
| Accesso all'app | Tutte le funzionalità disponibili senza restrizioni |
| Pubblico di destinazione | **18+** (non per bambini) |
| Classificazione dei contenuti | Upitnik: kategorija „Tutte le altre app“ / uslužna; bez nasilja, seksa, kockanja, korisničkog sadržaja; **ne deli lokaciju sa drugim korisnicima**; ima kupovinu u aplikaciji |
| App di notizie | No |
| App governative | No (podaci Bison Futé su javni, aplikacija nije vladina) |
| Funzionalità finanziarie / Salute | Nessuna |

### Autorizzazioni del servizio in primo piano (FOREGROUND_SERVICE_LOCATION – Vožnja)
- Tip: **Posizione** → čekirati samo **Navigazione**
- Opis (kopirati):
  `Driving mode shows traffic warnings for the motorway the user is driving on; the user starts it with a button and sees a permanent notification; it stops when the user taps Stop or closes the app.`
- **Video** (YouTube, „Non in elenco“), 20–30 s snimak ekrana telefona: početni ekran → „Avvia“ (Modalità guida) → obaveštenje gore → otvoren ekran autoroute → „Stop“. Može i kod kuće (pokazuje pokretanje i obaveštenje).

### Sicurezza dei dati (Data safety) – kao InterstateInfo + lokacija + kupovina
- Korak 2: raccoglie/condivide **Sì**; crittografia in transito **Sì**; „La mia app non consente agli utenti di creare un account“; account esterni **No**; eliminazione dati **No**. Usamljeno „No“ ispod „Rimuovi le risposte“ NE DIRATI
- Korak 3, tipovi:
  - Posizione: **approssimativa** i **precisa** (Modalità guida; obrađuje se na telefonu; AdMob približna)
  - Informazioni personali: **Indirizzo email** (Google ga „vidi“ u bibliotekama AdMob/Play services/Billing; bez toga automatska provera odbija)
  - Informazioni finanziarie: **Cronologia acquisti** (Google Play Billing, „Rimuovi annunci“)
  - Attività nelle app: **Interazioni con l'app**
  - Informazioni e prestazioni dell'app: **Log degli arresti anomali**, **Diagnostica**
  - ID dispositivo o altri ID
- Korak 4 za svaki: Raccolti + Condivisi; nije privremeno; obavezno (email: korisnik može da bira); svrhe: Pubblicità o marketing, Analisi, Prevenzione di frodi, sicurezza e conformità; za lokaciju i dodatno **Funzionalità dell'app**; za kupovinu **Funzionalità dell'app**

## Note di rilascio (prva verzija)
```
<fr-FR>
Première version : sorties, aires de service, stations avec prix moyens, recharge et parkings sur 50 autoroutes françaises, classés par point kilométrique. Mode conduite avec point kilométrique et sens, infos trafic Bison Futé. Fonctionne hors ligne.
</fr-FR>
<en-US>
First release: exits, service areas, fuel stations with average prices, EV charging and parking on 50 French motorways, sorted by kilometre. Driving mode with kilometre and direction, traffic news from Bison Futé. Works offline.
</en-US>
```

## Proizvod „Bez reklama“
- ID prodotto `remove_ads` · Nome `Supprimer les annonces` · Descrizione `Supprime définitivement toutes les annonces de l'application (bannières et plein écran) en un seul achat. Sans abonnement.`
- Icona: `play/grafika/remove_ads_512.png` · ID opzione `buy` · Acquista · **2,99 EUR** → Attiva

## Stanje (20./21.11.2026)
- [x] AdMob preko Chrome-a: app ~9059684986, baner /5120439974, interstitial /9915977795; GDPR poruka (URL privatnosti upisan)
- [x] R8 + pravila, release proveren na telefonu
- [x] Politika privatnosti EN/FR na sajtu
- [x] Tekstovi FR/EN, ikona, naslovna, remove_ads ikona, snimci fr_/en_ (`python alati/uramljivanje.py`)
- [x] Video za Vožnju `play/voznja_video.mp4` (26 s, aplikacija na francuskom); [ ] postaviti na YouTube „Non in elenco“
- [ ] Novi ključ (Create new… → `keystore-kljuceviutoroutesinfo.jks`), potpisan .aab
- [ ] Play Console: Crea app, listing, Contenuti dell'app, interni test, remove_ads, produkcija
- [ ] Posle objave: AdMob „Aggiungi store“ na ISTOJ aplikaciji (~9059684986)
