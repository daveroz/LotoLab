#!/usr/bin/env node
/*
 * LotoLab's launcher icon, in the family style of Worldly and SAB Monitor (a graphite stage over a tilted street
 * grid, a vignette, a hairline edge, a dark disc ringed in amber with three coloured dots and an amber marker),
 * lit the way Worldly's widgets are since 1.55: light sources in a dark, hazy room. The histogram of draws is a
 * small wall of square lamps with warm, overexposed cores (white-hot only at the top), blooming into the gaps and
 * scattering in a haze above each lit column; the fair-draw curve is a thread of light with a tight glow and a wide
 * bloom; the drawn ball is lit like a lamp; two banks of fog drift over the stage, and film grain lies over it all.
 *
 * Writes assets/icon.svg (the 512 × 512 master) and the adaptive icon's layers as PNGs in every density
 * (app/src/main/res/mipmap-*dpi/ic_launcher_{background,foreground}.webp), plus the themed-icon silhouette
 * (app/src/main/res/drawable/ic_launcher_monochrome.xml). Run from the repository root:
 *
 *     node scripts/render_icon.js            (needs Playwright with Chromium: npm i playwright)
 *     node scripts/render_icon.js preview.png  also writes a preview of the icon under four launcher masks
 */
const fs = require('fs');
const path = require('path');

// ---------------------------------------------------------------- the light (Worldly's Scene, for its amber)
const KEY = '#eb9438';  // the key light
const HOT = '#ffb64e';  // the key overexposed: the warm core of a lit lamp
const DEEP = '#d8750b'; // the key richer and deeper: light seen through haze (bloom, halo, fog)
const LED = '#faf2e7';  // the near-white of an LED's core
const RING = '#df8b35'; // the family's amber line work

// ---------------------------------------------------------------- the family's stage
// The street grid of Worldly's and SAB Monitor's icons (same lines, extended so the adaptive layer's margins are
// covered too), turned -16° about the centre.
const H_LINES = [[-270, 0], [-200, 0], [-138, 0], [-84, 1], [-14, 0], [32, 0], [78, 0], [124, 1], [186, 0], [232, 0],
  [286, 0], [332, 1], [378, 0], [448, 0], [518, 0], [564, 1], [618, 0], [664, 0], [734, 0], [790, 1]];
const V_LINES = [[-270, 0], [-200, 0], [-150, 1], [-100, 0], [-42, 0], [8, 0], [84, 1], [134, 0], [192, 0], [242, 0],
  [300, 1], [366, 0], [442, 0], [500, 0], [550, 1], [616, 0], [674, 0], [724, 0], [790, 1]];

function grid() {
  const line = (x1, y1, x2, y2, thick) =>
    `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${thick ? '#3a4560' : '#252d40'}" stroke-width="${thick ? 9 : 4}"/>`;
  return `<g transform="rotate(-16 256 256)" stroke-linecap="square" fill="none">`
    + H_LINES.map(([y, t]) => line(-340, y, 860, y, t)).join('')
    + V_LINES.map(([x, t]) => line(x, -340, x, 860, t)).join('')
    + `</g>`
    + `<line x1="-200" y1="563.3" x2="720" y2="26.7" stroke="#3a4560" stroke-width="10"/>` // the avenue
    + `<g transform="rotate(-16 256 256)"><rect x="40" y="318" width="110" height="64" rx="6" fill="#18241f" opacity=".9"/></g>`; // the park
}

// ---------------------------------------------------------------- the lamps: a histogram of draws
const LAMP = 26, PITCH = 34, ROWS = 5;
const LIT = [2, 3, 5, 4, 3, 1];           // lit lamps per column: more draws in the middle, as a fair draw gives
const WALL_X = 256 - (LIT.length * PITCH - (PITCH - LAMP)) / 2;
const BASE = 334;                          // the bottom of the wall
const lampX = i => WALL_X + i * PITCH;
const lampY = k => BASE - LAMP - k * PITCH; // k = 0 is the bottom row

const CURVE = 'M146,330 C192,328 204,150 256,148 C308,150 320,328 366,330';
const BALL = { x: 256, y: 112, r: 21 };
const DOTS = [[176, 136, 11, '#67e8f9'], [342, 120, 9, '#facc15'], [384, 252, 10, '#22c55e']];

/** The shared definitions; the grain's specks are about {@code pxPerUnit}-scaled pixels, so they stay one or two pixels wide. */
function defs(pxPerUnit = 1) {
  return `
  <linearGradient id="bg" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="512" y2="512"><stop offset="0" stop-color="#1c2230"/><stop offset="1" stop-color="#090b11"/></linearGradient>
  <radialGradient id="vig" gradientUnits="userSpaceOnUse" cx="256" cy="256" r="384"><stop offset=".35" stop-color="#0b0e15" stop-opacity="0"/><stop offset="1" stop-color="#0b0e15" stop-opacity=".85"/></radialGradient>
  <radialGradient id="disc" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="#101520" stop-opacity=".9"/><stop offset="1" stop-color="#0b0e15" stop-opacity=".94"/></radialGradient>
  <radialGradient id="washKey" gradientUnits="userSpaceOnUse" cx="430" cy="70" r="360"><stop offset="0" stop-color="${KEY}" stop-opacity=".2"/><stop offset="1" stop-color="${KEY}" stop-opacity="0"/></radialGradient>
  <radialGradient id="washLow" gradientUnits="userSpaceOnUse" cx="40" cy="470" r="300"><stop offset="0" stop-color="${KEY}" stop-opacity=".09"/><stop offset="1" stop-color="${KEY}" stop-opacity="0"/></radialGradient>
  <radialGradient id="fog" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="${DEEP}" stop-opacity="1"/><stop offset="1" stop-color="${DEEP}" stop-opacity="0"/></radialGradient>
  <radialGradient id="air" gradientUnits="userSpaceOnUse" cx="256" cy="230" r="190"><stop offset="0" stop-color="${DEEP}" stop-opacity=".16"/><stop offset="1" stop-color="${DEEP}" stop-opacity="0"/></radialGradient>
  <linearGradient id="beam" x1="0" y1="1" x2="0" y2="0"><stop offset="0" stop-color="${DEEP}" stop-opacity=".34"/><stop offset="1" stop-color="${DEEP}" stop-opacity="0"/></linearGradient>
  <radialGradient id="core" cx=".5" cy=".45" r=".66"><stop offset="0" stop-color="${HOT}"/><stop offset=".5" stop-color="${KEY}"/><stop offset="1" stop-color="#b8600a"/></radialGradient>
  <radialGradient id="whiteHot" cx=".5" cy=".45" r=".66"><stop offset="0" stop-color="#ffffff"/><stop offset=".3" stop-color="${LED}"/><stop offset=".7" stop-color="${HOT}"/><stop offset="1" stop-color="${KEY}"/></radialGradient>
  <radialGradient id="ballFill" cx=".38" cy=".34" r=".7"><stop offset="0" stop-color="${LED}"/><stop offset=".35" stop-color="${HOT}"/><stop offset=".8" stop-color="${KEY}"/><stop offset="1" stop-color="${DEEP}"/></radialGradient>
  <linearGradient id="rim" gradientUnits="userSpaceOnUse" x1="120" y1="420" x2="400" y2="90"><stop offset="0" stop-color="${RING}"/><stop offset=".6" stop-color="${KEY}"/><stop offset="1" stop-color="${HOT}"/></linearGradient>
  <filter id="soft" x="-20%" y="-20%" width="140%" height="140%"><feGaussianBlur stdDeviation="0.7"/></filter>
  <filter id="halo" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="4"/></filter>
  <filter id="bloom" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="9"/></filter>
  <filter id="wide" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur stdDeviation="22"/></filter>
  <filter id="haze" x="-30%" y="-10%" width="160%" height="120%"><feGaussianBlur stdDeviation="7"/></filter>
  <filter id="grain" x="0" y="0" width="100%" height="100%" color-interpolation-filters="sRGB">
    <feTurbulence type="fractalNoise" baseFrequency="${(0.62 * pxPerUnit).toFixed(4)}" numOctaves="2" seed="7" stitchTiles="stitch" result="n"/>
    <feColorMatrix in="n" type="matrix" result="light" values="0 0 0 0 1  0 0 0 0 1  0 0 0 0 1  0.55 0 0 0 -0.31"/>
    <feColorMatrix in="n" type="matrix" result="dark" values="0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  -0.7 0 0 0 0.3"/>
    <feMerge><feMergeNode in="light"/><feMergeNode in="dark"/></feMerge>
  </filter>`;
}

/** The stage: night graphite, the street grid, the washes of the key light, two banks of fog, the vignette. */
function stage(x, y, w, h) {
  return `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="url(#bg)"/>`
    + grid()
    + `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${DEEP}" opacity=".03"/>` // the light's warmth in the air
    + `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="url(#washKey)"/>`
    + `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="url(#washLow)"/>`
    + `<ellipse cx="150" cy="190" rx="390" ry="150" fill="url(#fog)" opacity=".11"/>`  // fog, high on the left
    + `<ellipse cx="370" cy="400" rx="360" ry="125" fill="url(#fog)" opacity=".085"/>` // fog, low on the right
    + `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="url(#vig)"/>`;
}

/** The disc and everything lit on it. */
function disc() {
  const lit = [], dark = [];
  LIT.forEach((n, i) => {
    for (let k = 0; k < ROWS; k++) (k < n ? lit : dark).push([lampX(i), lampY(k), i, k, n]);
  });
  const rect = ([x, y], extra = '') => `<rect x="${x}" y="${y}" width="${LAMP}" height="${LAMP}" rx="4" ${extra}/>`;
  const litRects = lit.map(l => rect(l)).join('');
  // a dark lamp next to a lit one catches a little of its light
  const near = ([, , i, k]) => (LIT[i] === k) || (i > 0 && LIT[i - 1] > k) || (i < LIT.length - 1 && LIT[i + 1] > k);
  return `<circle cx="256" cy="256" r="184" fill="url(#disc)"/>`
    + `<circle cx="256" cy="256" r="184" fill="url(#air)"/>`
    // the family's inner ring and the baseline, faint, behind the light
    + `<g fill="none" stroke="${RING}"><circle cx="256" cy="256" r="126" stroke-width="5" stroke-opacity=".28"/>`
    + `<path d="M88 ${BASE + 12}H424" stroke-width="4" stroke-opacity=".42"/></g>`
    // haze above each lit column: its light scattered in the air
    + LIT.map((n, i) => `<rect x="${lampX(i) - 4}" y="${lampY(n - 1) - 120}" width="${LAMP + 8}" height="122" fill="url(#beam)" filter="url(#haze)"/>`).join('')
    // the wall's wide glow and the tight bloom filling the gaps between lit lamps
    + `<g fill="${DEEP}" filter="url(#wide)" opacity=".45">${litRects}</g>`
    + `<g fill="${DEEP}" filter="url(#bloom)" opacity=".5">${litRects}</g>`
    + `<g fill="${DEEP}" filter="url(#halo)" opacity=".55">${litRects}</g>`
    // dark lamps: glass, catching their neighbours' light
    + dark.map(d => rect(d, `fill="${near(d) ? '#3a2a1a' : '#1a2130'}" fill-opacity=".55" stroke="${near(d) ? '#6a4520' : '#2a3346'}" stroke-opacity=".8" stroke-width="1.5"`)).join('')
    // lit lamps: warm cores with soft edges, white-hot at the top of the tallest columns
    + `<g filter="url(#soft)">` + lit.map(l => rect(l, `fill="url(#${l[3] === l[4] - 1 && l[4] >= 4 ? 'whiteHot' : 'core'})"`)).join('') + `</g>`
    // the fair-draw curve: a thread of light
    + `<g fill="none" stroke-linecap="round"><path d="${CURVE}" stroke="${DEEP}" stroke-width="12" opacity=".6" filter="url(#bloom)"/>`
    + `<path d="${CURVE}" stroke="${HOT}" stroke-width="6" opacity=".75" filter="url(#halo)"/>`
    + `<path d="${CURVE}" stroke="${LED}" stroke-width="3.5"/></g>`
    // the outer ring, lit: brightest towards the key light at the top right
    + `<g fill="none"><circle cx="256" cy="256" r="184" stroke="${DEEP}" stroke-width="10" opacity=".5" filter="url(#bloom)"/>`
    + `<circle cx="256" cy="256" r="184" stroke="${HOT}" stroke-width="8" opacity=".35" filter="url(#halo)"/>`
    + `<circle cx="256" cy="256" r="184" stroke="url(#rim)" stroke-width="7"/></g>`
    // the other balls in the drum: the family's three dots, each glowing softly
    + DOTS.map(([x, y, r, c]) => `<circle cx="${x}" cy="${y}" r="${r * 2}" fill="${c}" opacity=".28" filter="url(#halo)"/>`
      + `<circle cx="${x}" cy="${y}" r="${r}" fill="${c}" fill-opacity=".92"/>`).join('')
    // the drawn ball: the family's marker, lit like a lamp
    + `<circle cx="${BALL.x}" cy="${BALL.y}" r="${BALL.r + 6}" fill="${DEEP}" opacity=".85" filter="url(#wide)"/>`
    + `<circle cx="${BALL.x}" cy="${BALL.y}" r="${BALL.r + 3}" fill="${HOT}" opacity=".6" filter="url(#halo)"/>`
    + `<circle cx="${BALL.x}" cy="${BALL.y}" r="${BALL.r}" fill="url(#ballFill)" stroke="${LED}" stroke-width="5"/>`;
}

const grain = (x, y, w, h) => `<rect x="${x}" y="${y}" width="${w}" height="${h}" filter="url(#grain)"/>`;

/** The 512 × 512 master: the rounded square, its hairline edge of cool light. */
function master() {
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" width="512" height="512"><defs>${defs()}
  <clipPath id="sq"><rect width="512" height="512" rx="112"/></clipPath>
</defs><g clip-path="url(#sq)">${stage(0, 0, 512, 512)}${disc()}${grain(0, 0, 512, 512)}
<rect x="1.5" y="1.5" width="509" height="509" rx="110.5" fill="none" stroke="#aadcff" stroke-opacity=".16" stroke-width="3"/></g></svg>
`;
}

// The adaptive icon's layers are 108 dp; the launcher's mask shows the middle 72 dp, so the master's 512 units span
// the middle two thirds of a 768-unit layer.
const LAYER = 'viewBox="-128 -128 768 768"';
// Each layer carries its own grain (the foreground's only over the disc), so the transparent parts stay clean and
// the files small.
const background = px => `<svg xmlns="http://www.w3.org/2000/svg" ${LAYER}><defs>${defs(px / 768)}</defs>${stage(-128, -128, 768, 768)}`
  + `${grain(-128, -128, 768, 768)}</svg>`;
const foreground = px => `<svg xmlns="http://www.w3.org/2000/svg" ${LAYER}><defs>${defs(px / 768)}<clipPath id="inDisc"><circle cx="256" cy="256" r="188"/></clipPath></defs>`
  + `${disc()}<g clip-path="url(#inDisc)">${grain(-128, -128, 768, 768)}</g></svg>`;

/** The themed icon (Android 13+): the ring, the lit lamps, the curve and the ball, as one silhouette. */
function monochrome() {
  const t = v => +((v + 128) * 108 / 768).toFixed(2);
  const s = v => +(v * 108 / 768).toFixed(2);
  const lamps = [];
  LIT.forEach((n, i) => { for (let k = 0; k < n; k++) lamps.push(`M${t(lampX(i))},${t(lampY(k))}h${s(LAMP)}v${s(LAMP)}h${-s(LAMP)}z`); });
  const curve = CURVE.replace(/-?\d+(\.\d+)?/g, m => String(t(+m))); // x and y scale alike
  const circle = (cx, cy, r) => `M${t(cx - r)},${t(cy)}a${s(r)},${s(r)} 0,1 1,${s(2 * r)},0a${s(r)},${s(r)} 0,1 1,${-s(2 * r)},0z`;
  return `<?xml version="1.0" encoding="utf-8"?>
<!-- The themed-icon silhouette (generated by scripts/render_icon.js): ring, lamps, curve and ball -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#00000000" android:strokeColor="#FFFFFFFF" android:strokeWidth="${s(9)}"
        android:pathData="${circle(256, 256, 184)}" />
    <path android:fillColor="#FFFFFFFF" android:pathData="${lamps.join('')}" />
    <path android:fillColor="#00000000" android:strokeColor="#FFFFFFFF" android:strokeWidth="${s(6)}" android:strokeLineCap="round"
        android:pathData="${curve}" />
    <path android:fillColor="#FFFFFFFF" android:pathData="${circle(BALL.x, BALL.y, BALL.r + 3)}" />
</vector>
`;
}

// ---------------------------------------------------------------- rendering
const DENSITIES = { mdpi: 1, hdpi: 1.5, xhdpi: 2, xxhdpi: 3, xxxhdpi: 4 };

/** Renders {@code svg} at {@code px} × {@code px} into {@code file} as WebP (lossy, alpha kept). */
async function render(page, svg, px, file) {
  const uri = 'data:image/svg+xml;base64,' + Buffer.from(svg).toString('base64');
  const data = await page.evaluate(async ({ uri, px }) => {
    const img = new Image();
    img.src = uri;
    await img.decode();
    const c = document.createElement('canvas');
    c.width = c.height = px;
    c.getContext('2d').drawImage(img, 0, 0, px, px);
    return c.toDataURL('image/webp', 0.92);
  }, { uri, px });
  if (!data.startsWith('data:image/webp')) throw new Error('this Chromium cannot write WebP');
  fs.writeFileSync(file, Buffer.from(data.split(',')[1], 'base64'));
}

(async () => {
  const root = path.resolve(__dirname, '..');
  const res = path.join(root, 'app/src/main/res');
  fs.writeFileSync(path.join(root, 'assets/icon.svg'), master());
  fs.writeFileSync(path.join(res, 'drawable/ic_launcher_monochrome.xml'), monochrome());
  const { chromium } = require('playwright');
  const browser = await chromium.launch();
  const page = await browser.newPage({ deviceScaleFactor: 1 });
  for (const [d, k] of Object.entries(DENSITIES)) {
    const dir = path.join(res, `mipmap-${d}`);
    fs.mkdirSync(dir, { recursive: true });
    const px = Math.round(108 * k);
    await render(page, background(px), px, path.join(dir, 'ic_launcher_background.webp'));
    await render(page, foreground(px), px, path.join(dir, 'ic_launcher_foreground.webp'));
  }
  const preview = process.argv[2];
  if (preview) {
    // the two layers stacked under a squircle, a circle, a rounded square and a teardrop, beside the master
    const b = 'data:image/svg+xml;base64,' + Buffer.from(background(300)).toString('base64');
    const f = 'data:image/svg+xml;base64,' + Buffer.from(foreground(300)).toString('base64');
    const m = 'data:image/svg+xml;base64,' + Buffer.from(master()).toString('base64');
    const masks = ['border-radius:50%', 'border-radius:22%', 'border-radius:32% 32% 32% 32% / 32% 32% 32% 32%', 'border-radius:50% 50% 50% 12%'];
    const icon = (mask, size) => `<div style="width:${size}px;height:${size}px;${mask};overflow:hidden;position:relative">`
      + `<img src="${b}" style="position:absolute;width:150%;height:150%;left:-25%;top:-25%">`
      + `<img src="${f}" style="position:absolute;width:150%;height:150%;left:-25%;top:-25%"></div>`;
    const html = `<html><body style="margin:0;padding:24px;background:#5b6370;display:flex;gap:24px;align-items:center;flex-wrap:wrap;width:1400px">`
      + `<img src="${m}" width="400" height="400">` + masks.map(x => icon(x, 200)).join('')
      + `<div style="display:flex;gap:16px;align-items:center">` + masks.map(x => icon(x, 48)).join('') + `</div></body></html>`;
    await page.setViewportSize({ width: 1448, height: 460 });
    await page.setContent(html);
    await page.waitForTimeout(300);
    await page.screenshot({ path: preview, fullPage: true });
  }
  await browser.close();
})();
