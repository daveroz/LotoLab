#!/usr/bin/env node
/*
 * LotoLab's launcher icon: a lottery ball whose band is a ring chart of probabilities, around its number, on the
 * family stage of Worldly's and SAB Monitor's icons (a graphite stage over a tilted street grid, a vignette, a
 * hairline edge) with the family's three coloured dots as the other balls of the draw. Lit the way Worldly's widgets
 * are since 1.55, as light sources in a dark, hazy room: the band's shares glow from a white-hot share down to a deep
 * amber one, blooming into the haze; the ball is dark glass with a gloss; the light warms the stage's air, two banks
 * of fog drift over it, and film grain lies over it all.
 *
 * Writes assets/icon.svg (the 512 × 512 master) and the adaptive icon's layers as WebP in every density
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

// ---------------------------------------------------------------- the ball
const BALL = { x: 256, y: 250, r: 150, band: 40 }; // r: the middle of the band
const SHARES = [0.32, 0.22, 0.18, 0.15, 0.13];     // the ring chart, largest first, from the top clockwise
const SHADES = [LED, HOT, KEY, '#c97624', '#8a4a12'];
const LABEL = 86;                                   // the ball's light disc
const DOTS = [[108, 112, 14, 'cyan'], [410, 120, 11, 'yellow'], [404, 404, 12, 'green']];
const DOT_COLOURS = { cyan: ['#9af0ff', '#2a9db3', '#67e8f9'], yellow: ['#ffe066', '#c79a0c', '#facc15'], green: ['#6ee7a0', '#178a4a', '#22c55e'] };
// Carbon+ Bold's "7" (font units, y up; bounds x 70–488, y 0–580), so the master needs no font
const SEVEN = 'M488 580H70V487H379V477C379 407 170 320 170 169V0H274V174C274 288 488 359 488 494Z';

/** The "7" placed with its middle at (cx, cy), {@code h} tall, as absolute path data (x and y transformed alike). */
function seven(cx, cy, h) {
  const k = h / 580, tx = cx - 279 * k, ty = cy + 290 * k;
  const X = v => +(tx + v * k).toFixed(2), Y = v => +(ty - v * k).toFixed(2);
  return SEVEN.replace(/([MHVCZ])([^MHVCZ]*)/g, (m, cmd, args) => {
    const n = args.trim() ? args.trim().split(/[\s,]+/).map(Number) : [];
    if (cmd === 'H') return 'H' + X(n[0]);
    if (cmd === 'V') return 'V' + Y(n[0]);
    if (cmd === 'Z') return 'Z';
    return cmd + n.map((v, i) => (i % 2 ? Y(v) : X(v))).join(' ');
  });
}

/** The band's arcs: {d, i} for each share, a small gap between them. */
function arcs(cx = BALL.x, cy = BALL.y, r = BALL.r) {
  const out = [];
  let a = -90;
  SHARES.forEach((sh, i) => {
    const sweep = sh * 360 - 6, a0 = (a + 3) * Math.PI / 180, a1 = (a + 3 + sweep) * Math.PI / 180;
    const p = (t, v) => +(v === 'x' ? cx + r * Math.cos(t) : cy + r * Math.sin(t)).toFixed(2);
    out.push({ i, d: `M${p(a0, 'x')} ${p(a0, 'y')}A${r} ${r} 0 ${sweep > 180 ? 1 : 0} 1 ${p(a1, 'x')} ${p(a1, 'y')}` });
    a += sh * 360;
  });
  return out;
}

/** The shared definitions; the grain's specks are about {@code pxPerUnit}-scaled pixels, so they stay one or two pixels wide. */
function defs(pxPerUnit = 1) {
  return `
  <linearGradient id="bg" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="512" y2="512"><stop offset="0" stop-color="#1c2230"/><stop offset="1" stop-color="#090b11"/></linearGradient>
  <radialGradient id="vig" gradientUnits="userSpaceOnUse" cx="256" cy="256" r="384"><stop offset=".35" stop-color="#0b0e15" stop-opacity="0"/><stop offset="1" stop-color="#0b0e15" stop-opacity=".85"/></radialGradient>
  <radialGradient id="washKey" gradientUnits="userSpaceOnUse" cx="430" cy="70" r="360"><stop offset="0" stop-color="${KEY}" stop-opacity=".2"/><stop offset="1" stop-color="${KEY}" stop-opacity="0"/></radialGradient>
  <radialGradient id="washLow" gradientUnits="userSpaceOnUse" cx="40" cy="470" r="300"><stop offset="0" stop-color="${KEY}" stop-opacity=".09"/><stop offset="1" stop-color="${KEY}" stop-opacity="0"/></radialGradient>
  <radialGradient id="fog" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="${DEEP}" stop-opacity="1"/><stop offset="1" stop-color="${DEEP}" stop-opacity="0"/></radialGradient>
  <radialGradient id="darkBall" cx=".36" cy=".3" r=".75"><stop offset="0" stop-color="#3a3d45"/><stop offset=".5" stop-color="#16171b"/><stop offset="1" stop-color="#08090b"/></radialGradient>
  <radialGradient id="label" cx=".42" cy=".36" r=".7"><stop offset="0" stop-color="#fffaf3"/><stop offset=".8" stop-color="#f6ead9"/><stop offset="1" stop-color="#e3cdb0"/></radialGradient>
  ${Object.entries(DOT_COLOURS).map(([k, [c, d]]) => `<radialGradient id="${k}Ball" cx=".36" cy=".32" r=".72"><stop offset="0" stop-color="#fffaf2"/><stop offset=".22" stop-color="${c}"/><stop offset=".78" stop-color="${d}"/><stop offset="1" stop-color="#101418"/></radialGradient>`).join('')}
  <filter id="halo" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="4"/></filter>
  <filter id="bloom" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="9"/></filter>
  <filter id="wide" x="-60%" y="-60%" width="220%" height="220%"><feGaussianBlur stdDeviation="22"/></filter>
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

/** The ball and the other balls of the draw. */
function disc() {
  const { x, y, r, band } = BALL;
  let out = `<circle cx="${x}" cy="${y}" r="${r + 30}" fill="${DEEP}" opacity=".35" filter="url(#wide)"/>`
    // the ball: dark glossy glass
    + `<circle cx="${x}" cy="${y}" r="${r + band / 2 + 6}" fill="url(#darkBall)"/>`;
  // the band: a share of the ring for each, glowing from white-hot down to a deep amber
  for (const { d, i } of arcs()) {
    out += `<path d="${d}" fill="none" stroke="${DEEP}" stroke-width="${band + 10}" opacity="${i === 0 ? 0.7 : 0.35}" filter="url(#bloom)"/>`
      + `<path d="${d}" fill="none" stroke="${SHADES[i]}" stroke-width="${band}"/>`;
  }
  // the ball's light disc and its number, and the gloss of a sphere
  out += `<circle cx="${x}" cy="${y}" r="${LABEL}" fill="url(#label)"/>`
    + `<path d="${seven(x, y + 2, 66)}" fill="#2a1606"/>`
    + `<path d="M${x - 120} ${y - 70} A 140 140 0 0 1 ${x - 40} ${y - 132}" fill="none" stroke="#ffffff" stroke-opacity=".3" stroke-width="10" stroke-linecap="round"/>`;
  // the other balls of the draw: the family's three dots, glossy and glowing softly
  for (const [dx, dy, dr, k] of DOTS) {
    const glow = DOT_COLOURS[k][2];
    out += `<circle cx="${dx}" cy="${dy}" r="${dr * 1.5}" fill="${glow}" opacity=".3" filter="url(#wide)"/>`
      + `<circle cx="${dx}" cy="${dy}" r="${dr + 3}" fill="${glow}" opacity=".35" filter="url(#halo)"/>`
      + `<circle cx="${dx}" cy="${dy}" r="${dr}" fill="url(#${k}Ball)"/>`;
  }
  return out;
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
const foreground = px => `<svg xmlns="http://www.w3.org/2000/svg" ${LAYER}><defs>${defs(px / 768)}<clipPath id="inDisc"><circle cx="${BALL.x}" cy="${BALL.y}" r="${BALL.r + BALL.band / 2 + 6}"/></clipPath></defs>`
  + `${disc()}<g clip-path="url(#inDisc)" opacity=".4">${grain(-128, -128, 768, 768)}</g></svg>`; // fainter on the ball's light parts

/** The themed icon (Android 13+): the band's shares, the light disc with its 7 cut out, and the three dots. */
function monochrome() {
  const t = v => +((v + 128) * 108 / 768).toFixed(2);
  const sc = v => +(v * 108 / 768).toFixed(2);
  const inLayer = d => d.replace(/-?\d+(\.\d+)?/g, m => String(t(+m))); // absolute coordinates; x and y scale alike
  const circle = (cx, cy, r) => `M${t(cx - r)},${t(cy)}a${sc(r)},${sc(r)} 0,1 1,${sc(2 * r)},0a${sc(r)},${sc(r)} 0,1 1,${-sc(2 * r)},0z`;
  const band = arcs().map(({ d }) => {
    // arcs carry radii and flags that mustn't move: rebuild each in layer units
    const m = d.match(/M([\d.-]+) ([\d.-]+)A([\d.]+) [\d.]+ 0 (\d) 1 ([\d.-]+) ([\d.-]+)/);
    return `M${t(+m[1])},${t(+m[2])}A${sc(+m[3])},${sc(+m[3])} 0 ${m[4]} 1 ${t(+m[5])},${t(+m[6])}`;
  }).join('');
  return `<?xml version="1.0" encoding="utf-8"?>
<!-- The themed-icon silhouette (generated by scripts/render_icon.js): the ring chart, the 7 and the dots -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#00000000" android:strokeColor="#FFFFFFFF" android:strokeWidth="${sc(BALL.band)}"
        android:pathData="${band}" />
    <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd"
        android:pathData="${circle(BALL.x, BALL.y, LABEL)}${inLayer(seven(BALL.x, BALL.y + 2, 66))}" />
    <path android:fillColor="#FFFFFFFF" android:pathData="${DOTS.map(([x, y, r]) => circle(x, y, r)).join('')}" />
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

module.exports = { defs, stage, grain, KEY, HOT, DEEP, LED, LAYER };

if (require.main === module) (async () => {
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
