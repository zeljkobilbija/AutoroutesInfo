/*
 * Zvanični kilometri sa francuske Vikipedije (spisak „Sorties“ svake autoroute) → "kal" u podacima.
 *
 *   node alati/kilometraza.js alati/osm/milestone_fr.json   (prvo table iz OSM-a, ako postoje)
 *   node alati/wiki_preuzmi.js                             (stranice u alati/wiki/aNN.wiki)
 *   node alati/km_izlazi.js                                (pa ovo; izveštaj u alati/wiki/izvestaj.txt)
 *
 * Redovi su stavke spiska: "* {{Sortie|16 Nemours}} à {{Unité|73|km}}",
 * "** {{Aire de service|de X|Paris – Lyon|de Y|Lyon – Paris}} à {{Unité|74|km}}",
 * "* {{Échangeur autoroutier|[[Autoroute A77 (France)|A77]]|A6}} à {{Unité|80|km}}".
 * Uparivanje: izlaz po BROJU (ref), čvorište po oznaci autoputa (ref A77), odmorište po imenu.
 * Par važi samo ako bar jedan drugi par 2–80 km dalje ima isti nagib ±1 (kao AutostradeInfo/alati/km_izlazi.js).
 */
'use strict';
const fs = require('fs'), path = require('path');

const ASSETS = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'autoputevi');
const WIKI = path.join(__dirname, 'wiki');

function ocisti(s) {
  let t = s.replace(/<ref[^>]*\/>/g, '').replace(/<ref[\s\S]*?<\/ref>/g, '');
  t = t.replace(/\[\[(?:Fichier|File|Image):[^\]]*\]\]/gi, ' ')
    .replace(/\[\[[^\]|]*\|([^\]]*)\]\]/g, '$1').replace(/\[\[([^\]]*)\]\]/g, '$1')
    .replace(/\{\{[^{}]*\}\}/g, ' ')
    .replace(/<[^>]+>/g, ' ').replace(/'{2,3}/g, '').replace(/&nbsp;/g, ' ');
  return t.replace(/\s+/g, ' ').trim();
}
const bezClana = s => ocisti(s).replace(/^(?:de la |de l'|de l’|du |des |de |d'|d’|la |le |les |l')/i, '').trim();
const normRef = s => String(s).toLowerCase().replace(',', '.').replace(/\s+/g, '');

// ---- 1. Redovi: {v: S|R|K, refs, ime, km} ----
function redovi(tekst) {
  const out = [];
  for (const l of tekst.split('\n')) {
    if (!/^\*/.test(l)) continue;
    const mk = l.match(/\{\{[Uu]nité\|\s*([\d  .,]+?)\s*\|\s*km\s*\}\}/);
    if (!mk) continue;
    const km = parseFloat(mk[1].replace(/[  ]/g, '').replace(',', '.'));
    if (!isFinite(km)) continue;
    for (const m of l.matchAll(/\{\{Sortie\|((?:[^{}]|\{\{[^{}]*\}\})*)\}\}/g)) {
      const t = ocisti(m[1]);
      const mb = t.match(/^(\d+(?:[.,]\d+)?[a-z]?(?:\s*\/\s*\d+(?:[.,]\d+)?[a-z]?)*)\s*(.*)$/i);
      if (mb) out.push({ v: 'S', refs: mb[1].split('/').map(normRef), ime: mb[2], km });
    }
    for (const m of l.matchAll(/\{\{Aire de (?:service|repos)\|([^{}]*)/g)) {
      // |ime1|smer1|ime2|smer2 – imena su na neparnim mestima
      const delovi = m[1].split('|');
      for (let i = 0; i < delovi.length; i += 2) if (delovi[i] && delovi[i].trim()) out.push({ v: 'R', ime: bezClana(delovi[i]), km });
    }
    for (const m of l.matchAll(/\{\{Échangeur autoroutier\|((?:[^{}]|\{\{[^{}]*\}\})*)\}\}/g)) {
      const prvi = m[1].split(/\|(?![^\[]*\]\])/)[0];
      const r = ocisti(prvi).match(/\b(A\s?\d+[a-z]?)\b/i);
      if (r) out.push({ v: 'K', refs: [normRef(r[1])], ime: ocisti(prvi), km });
    }
  }
  return out;
}

// ---- 2. Sličnost imena (odmorišta) ----
const STOP = new Set(['de', 'du', 'des', 'la', 'le', 'les', 'l', 'd', 'et', 'en', 'sur', 'aire', 'service', 'repos',
  'sortie', 'echangeur', 'parking', 'a', 'au', 'aux']);
const reci = s => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
  .split(/[^a-z0-9]+/).filter(w => w.length >= 2 && !STOP.has(w));
function slicnost(a, b) {
  const A = new Set(reci(a)), B = new Set(reci(b));
  if (!A.size || !B.size) return 0;
  let z = 0;
  for (const w of A) if (B.has(w)) z++;
  const SM = ['nord', 'sud', 'est', 'ouest', 'centre'];
  const sa = SM.filter(w => A.has(w)).join(), sb = SM.filter(w => B.has(w)).join();
  if (sa && sb && sa !== sb) return 0;
  return z / Math.min(A.size, B.size) - 0.01 * Math.abs(A.size - B.size);
}

// provera čitanja: node alati/km_izlazi.js --redovi a6
if (process.argv[2] === '--redovi') {
  for (const r of redovi(fs.readFileSync(path.join(WIKI, process.argv[3] + '.wiki'), 'utf8'))) console.log(r.km, r.v, (r.refs || []).join('/'), r.ime);
  process.exit(0);
}

function upari(wr, stavke) {
  const parovi = [];
  for (const r of wr) {
    if (r.v === 'S' || r.v === 'K') {
      for (const x of stavke) {
        if (!x.ref) continue;
        const xr = normRef(x.ref);
        if (r.refs.includes(xr) && (r.v === 'K') === /^a/.test(xr)) parovi.push([x.km, r.km, r.ime, x.n]);
      }
    } else {
      let naj = 0, ko = null;
      for (const x of stavke) {
        if (x.t !== 'R' && x.t !== 'P') continue;
        const s = slicnost(r.ime, x.n || '');
        if (s > naj) { naj = s; ko = x; }
      }
      if (ko && naj >= 0.99) parovi.push([ko.km, r.km, r.ime, ko.n]);
    }
  }
  return parovi;
}

// ---- 3. Po autostradi ----
const izvestaj = [];
for (const f of fs.readdirSync(ASSETS).filter(f => f.endsWith('.json'))) {
  const p = path.join(ASSETS, f);
  const wp = path.join(WIKI, f.replace('.json', '.wiki'));
  if (!fs.existsSync(wp)) { izvestaj.push(`${f}: nema stranice`); continue; }
  const d = JSON.parse(fs.readFileSync(p, 'utf8'));
  if (d.sekcije.length !== 1) { izvestaj.push(`${f}: ${d.sekcije.length} deonice, preskočeno`); continue; }
  const sek = d.sekcije[0];
  const wr = redovi(fs.readFileSync(wp, 'utf8'));
  const parovi = upari(wr, d.stavke);
  // provera nagiba sa drugim parovima
  const oceni = lista => lista.map(([n, z]) => {
    let plus = 0, minus = 0;
    for (const [qn, qz] of lista) {
      const dn = qn - n;
      if (Math.abs(dn) < 2 || Math.abs(dn) > 80) continue;
      const tol = Math.max(1.5, 0.04 * Math.abs(dn));
      if (Math.abs((qz - z) - dn) <= tol) plus++;
      else if (Math.abs((qz - z) + dn) <= tol) minus++;
    }
    return plus === minus ? 0 : (plus > minus ? 1 : -1) * Math.max(plus, minus);
  });
  let lista = parovi;
  for (let k = 0; k < 2; k++) {
    const o = oceni(lista);
    lista = lista.filter((_, i) => o[i] !== 0).map((q, i) => q);
  }
  const o = oceni(lista);
  let kal = lista.map((q, i) => [+q[0].toFixed(2), +q[1].toFixed(1), Math.sign(o[i])]).filter(q => q[2] !== 0);
  kal.sort((a, b) => a[0] - b[0]);
  kal = kal.filter((q, i) => i === 0 || q[0] - kal[i - 1][0] > 0.3);
  // izlaz koji se ne slaže ni sa jednim susedom (npr. A22 Vipiteno 20 pored 16) ispada
  const slaze = (a, b) => a[2] === b[2] && Math.abs((b[1] - a[1]) - a[2] * (b[0] - a[0])) <= Math.max(1.5, 0.04 * Math.abs(b[0] - a[0]));
  for (let k = 0; k < 3; k++) kal = kal.filter((q, i) => (i > 0 && slaze(kal[i - 1], q)) || (i < kal.length - 1 && slaze(q, kal[i + 1])));
  if (kal.length < 3) { izvestaj.push(`${f}: Vikipedija ${wr.length} redova, upareno ${parovi.length}, važi ${kal.length} – ostaju table iz OSM-a (${(sek.kal || []).length})`); continue; }
  // Table pored puta ponekad broje od drugog početka (A71 nastavlja A10: +99 km) ili obrnuto (A84).
  // Ako se bar 5 tabli iz OSM-a (60 %) slaže sa Vikipedijom uz isti pomak / obrnut smer, km se poravnaju sa tablama.
  let pomak = '';
  {
    const tab = (sek.kal || []).filter(q => !kal.some(w => Math.abs(w[0] - q[0]) < 0.3));
    if (tab.length >= 5) {
      const kand = { same: [], flip: [] };
      for (const q of tab) {
        let b = kal[0]; for (const w of kal) if (Math.abs(w[0] - q[0]) < Math.abs(b[0] - q[0])) b = w;
        const p = b[1] + b[2] * (q[0] - b[0]);
        if (q[2] === b[2]) kand.same.push(q[1] - p); else kand.flip.push(q[1] + p);
      }
      let naj = null;
      for (const tip of ['same', 'flip']) for (const v of kand[tip]) {
        const n = kand[tip].filter(x => Math.abs(x - v) <= 1.5).length;
        if (!naj || n > naj.n) naj = { tip, v, n };
      }
      if (naj && naj.n >= Math.max(5, 0.6 * tab.length)) {
        const lst = kand[naj.tip].filter(x => Math.abs(x - naj.v) <= 1.5).sort((a, b) => a - b);
        const c = +lst[Math.floor(lst.length / 2)].toFixed(1);
        if (naj.tip === 'flip') { kal = kal.map(q => [q[0], +(c - q[1]).toFixed(1), -q[2]]); pomak = ` (table broje obrnuto: ${c} − km)`; }
        else if (Math.abs(c) > 1.5) { kal = kal.map(q => [q[0], +(q[1] + c).toFixed(1), q[2]]); pomak = ` (pomak prema tablama ${c > 0 ? '+' : ''}${c} km)`; }
      }
    }
  }
  // table iz OSM-a koje se slažu sa Vikipedijom
  // Prednost imaju table iz OSM-a (prave table pored puta; A9 skoro svaki km, Vikipedija tu odstupa 1–6 km).
  // Vikipedija ostaje gde nema table bliže od 5 km ili gde se sa tablama slaže do 1,5 km.
  const pred = n => { let b = kal[0]; for (const q of kal) if (Math.abs(q[0] - n) < Math.abs(b[0] - n)) b = q; return b[1] + b[2] * (n - b[0]); };
  const sveOsm = sek.kal || [];
  const osm = sveOsm.filter(q => Math.abs(pred(q[0]) - q[1]) <= 1.5);
  const predOsm = n => { let b = sveOsm[0]; for (const q of sveOsm) if (Math.abs(q[0] - n) < Math.abs(b[0] - n)) b = q; return b; };
  const wiki = kal.filter(w => {
    if (!sveOsm.length) return true;
    const b = predOsm(w[0]);
    if (Math.abs(b[0] - w[0]) > 5) return true;
    return Math.abs(b[1] + b[2] * (w[0] - b[0]) - w[1]) <= 1.5 && !sveOsm.some(q => Math.abs(q[0] - w[0]) < 0.3);
  });
  if (process.env.DEBUG === d.id) {
    console.log('Vikipedija:', kal.map(q => q.join('/')).join(' '));
    console.log('OSM (naš/tabla/predviđeno):', sveOsm.map(q => `${q[0]}/${q[1]}/${pred(q[0]).toFixed(1)}`).join(' '));
  }
  sek.kal = [...sveOsm, ...wiki].sort((a, b) => a[0] - b[0]);
  const L = Math.max(...d.stavke.map(x => x.km));
  const fin = sek.kal;
  let rupa = Math.max(fin[0][0], L - fin[fin.length - 1][0]);
  for (let i = 1; i < fin.length; i++) rupa = Math.max(rupa, fin[i][0] - fin[i - 1][0]);
  izvestaj.push(`${f}: Vikipedija ${wr.length} redova, upareno ${parovi.length}, važi ${kal.length}, u podatke ${wiki.length} + ${sveOsm.length} tabli iz OSM-a (${osm.length} se slaže sa Vikipedijom), najveća rupa ${rupa.toFixed(0)} km${pomak}`);
  fs.writeFileSync(p, JSON.stringify(d));
}
fs.writeFileSync(path.join(WIKI, 'izvestaj.txt'), izvestaj.join('\n') + '\n');
console.log(izvestaj.join('\n'));
