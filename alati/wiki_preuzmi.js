/*
 * Preuzima wikitext stranica francuskih autoputeva sa fr.wikipedia u alati/wiki/aNN.wiki
 * (index.php?action=raw, pauza 1,5 s; prati #REDIRECTION). Postojeće fajlove preskače (--sve: ponovo sve).
 *   node alati/wiki_preuzmi.js
 */
'use strict';
const fs = require('fs'), path = require('path');
const ASSETS = path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'autoputevi');
const WIKI = path.join(__dirname, 'wiki');
fs.mkdirSync(WIKI, { recursive: true });
const sve = process.argv.includes('--sve');
const pauza = ms => new Promise(r => setTimeout(r, ms));
async function raw(naslov) {
  const u = 'https://fr.wikipedia.org/w/index.php?title=' + encodeURIComponent(naslov.replace(/ /g, '_')) + '&action=raw';
  const r = await fetch(u, { headers: { 'User-Agent': 'AutoroutesInfo-tool/1.0' } });
  return r.ok ? r.text() : null;
}
(async () => {
  for (const f of fs.readdirSync(ASSETS).filter(f => f.endsWith('.json'))) {
    const cilj = path.join(WIKI, f.replace('.json', '.wiki'));
    if (!sve && fs.existsSync(cilj)) continue;
    const id = JSON.parse(fs.readFileSync(path.join(ASSETS, f), 'utf8')).id;
    let t = null;
    for (const nas of [`Autoroute ${id} (France)`, `Autoroute ${id} (France métropolitaine)`]) {
      t = await raw(nas); await pauza(1500);
      for (let k = 0; t && k < 2; k++) {
        const m = t.match(/^#REDIRECT(?:ION)?\s*\[\[([^\]]+)\]\]/i);
        if (!m) break;
        t = await raw(m[1]); await pauza(1500);
      }
      if (t && t.length > 500) break;
    }
    if (t && t.length > 500) { fs.writeFileSync(cilj, t); console.log(id, t.length); }
    else console.log(id, 'NEMA');
  }
})();
