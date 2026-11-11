// Revisión de accesibilidad y comportamiento de servicio-web. Ver servicio-web/README.md, "Accesibilidad".
//
// Uso (servicio-web levantado con el perfil dev):
//   npm install && npm run revisar
// Variables opcionales:
//   WEB_URL       URL de servicio-web (por defecto http://localhost:8085)
//   CHROMIUM      ruta del navegador (por defecto /usr/bin/chromium)
//   KC_AUTH_URL   URL del endpoint de autorización de un realm con loginTheme=donatrack, para revisar
//                 también el login de Keycloak (ej. http://localhost:8180/realms/donatrack/protocol/openid-connect)
//   KC_QUERY      query string con client_id y redirect_uri válidos para ese realm
//   SALIDA        archivo JSON con el detalle (por defecto revision.json)
//   PAGINAS_EXTRA rutas propias separadas por coma (ej. /admin/donantes,/donante/incentivos)
const path = require("path");
const fs = require("fs");
const puppeteer = require("puppeteer-core");
const axeSource = fs.readFileSync(require.resolve("axe-core/axe.min.js"), "utf8");
const WEB = process.env.WEB_URL || "http://localhost:8085";
const KC = process.env.KC_AUTH_URL;
const Q = process.env.KC_QUERY || "";

const paginas = [
  ["Landing", WEB + "/"], ["Galería", WEB + "/galeria"], ["Mapa", WEB + "/mapa"], ["Ayuda", WEB + "/ayuda"],
  ["Privacidad", WEB + "/legal/privacidad"], ["Términos", WEB + "/legal/terminos"], ["Registro", WEB + "/registro"],
  ["Admin (en construcción)", WEB + "/admin/donaciones"], ["Panel donante", WEB + "/donante"],
  ["Catálogo", WEB + "/dev/componentes"], ["404", WEB + "/no-existe"],
].concat(KC ? [["Keycloak login", KC + "/auth?" + Q], ["Keycloak registro", KC + "/registrations?" + Q]] : [])
 .concat((process.env.PAGINAS_EXTRA || "").split(",").filter(Boolean).map(r => [r, WEB + r]));

async function axe(page) {
  await page.evaluate(axeSource);
  return page.evaluate(async () => {
    const r = await axe.run(document, { runOnly: { type: "tag", values: ["wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22aa"] } });
    return r.violations.map(v => ({ id: v.id, impacto: v.impact, ayuda: v.help, nodos: v.nodes.slice(0, 5).map(n => n.target.join(" ")) }));
  });
}

// Recorre con Tab y verifica que cada elemento enfocado tenga indicador visible.
async function foco(page, maximo) {
  await page.evaluate(() => { document.activeElement && document.activeElement.blur(); window.scrollTo(0, 0); });
  const sinFoco = [], recorridos = [];
  for (let i = 0; i < maximo; i++) {
    await page.keyboard.press("Tab");
    const info = await page.evaluate(() => {
      const el = document.activeElement;
      if (!el || el === document.body) return null;
      const cs = getComputedStyle(el);
      const visible = (cs.outlineStyle !== "none" && parseFloat(cs.outlineWidth) > 0) || (cs.boxShadow && cs.boxShadow !== "none");
      const desc = el.tagName.toLowerCase() + (el.id ? "#" + el.id : "") + (el.className && typeof el.className === "string" ? "." + el.className.trim().split(/\s+/).join(".") : "");
      return { desc: desc.slice(0, 80), texto: (el.innerText || el.getAttribute("aria-label") || el.value || "").trim().slice(0, 40), visible };
    });
    if (!info) break;
    recorridos.push(info.texto || info.desc);
    if (!info.visible) sinFoco.push(info);
  }
  return { recorridos: recorridos.length, primero: recorridos[0], sinFoco };
}

// Mide el área clickeable de los controles visibles (consigna: 48x48).
async function areas(page) {
  return page.evaluate(() => {
    const chicos = [];
    document.querySelectorAll("a[href], button, input:not([type=hidden]), select, textarea, summary, [role=tab]").forEach(el => {
      const r = el.getBoundingClientRect();
      if (r.width === 0 || r.height === 0) return;
      const cs = getComputedStyle(el);
      if (cs.visibility === "hidden") return;
      let alto = r.height, ancho = r.width;
      // Enlaces dentro de un texto: base.css amplía su zona con ::after.
      const despues = getComputedStyle(el, "::after");
      if (despues.content !== "none" && despues.position === "absolute") {
        alto = Math.max(alto, parseFloat(despues.height) || 0); ancho = Math.max(ancho, parseFloat(despues.width) || 0);
      }
      if (alto < 47.5 || ancho < 47.5) {
        const texto = (el.innerText || el.getAttribute("aria-label") || el.getAttribute("title") || el.type || "").trim().slice(0, 40);
        chicos.push(`${el.tagName.toLowerCase()} "${texto}" ${Math.round(ancho)}x${Math.round(alto)}`);
      }
    });
    return chicos;
  });
}

(async () => {
  const browser = await puppeteer.launch({ executablePath: process.env.CHROMIUM || "/usr/bin/chromium", headless: true, args: ["--no-sandbox"] });
  const salida = { paginas: {}, interaccion: {} };
  for (const [nombre, url] of paginas) {
    for (const [disp, vp] of [["escritorio", { width: 1280, height: 900 }], ["movil", { width: 390, height: 844, isMobile: true, hasTouch: true }]]) {
      const page = await browser.newPage();
      await page.setViewport(vp);
      const resp = await page.goto(url, { waitUntil: "networkidle2", timeout: 30000 }).catch(e => null);
      await new Promise(r => setTimeout(r, 600));
      const r = { status: resp ? resp.status() : "error", axe: await axe(page), areasChicas: await areas(page) };
      if (disp === "escritorio") r.foco = await foco(page, 60);
      salida.paginas[`${nombre} (${disp})`] = r;
      await page.close();
    }
  }

  const it = salida.interaccion;
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 900 });
  // Saltar al contenido
  await page.goto(WEB + "/ayuda", { waitUntil: "networkidle2" });
  await page.keyboard.press("Tab");
  it.saltarEsPrimero = await page.evaluate(() => document.activeElement.classList.contains("saltar-contenido"));
  await page.keyboard.press("Enter");
  it.saltarLlevaAMain = await page.evaluate(() => location.hash === "#contenido");

  // Catálogo: toast, modal, pestañas, loader
  await page.goto(WEB + "/dev/componentes", { waitUntil: "networkidle2" });
  await page.click('[data-toast-demo="exito"]');
  it.toastExito = await page.$eval("#toasts", el => el.innerText.includes("Cambios guardados") && !!el.querySelector('[role="status"]'));
  await page.click('[data-toast-demo="error"]');
  it.toastErrorEsAlert = await page.$eval("#toasts", el => !!el.querySelector('.toast--error[role="alert"]'));
  await page.click('[data-abrir-modal="modal-demo"]');
  it.modalAbre = await page.$eval("#modal-demo", d => d.open);
  await page.keyboard.press("Escape");
  it.modalCierraConEsc = await page.$eval("#modal-demo", d => !d.open);
  it.modalDevuelveFoco = await page.evaluate(() => document.activeElement.getAttribute("data-abrir-modal") === "modal-demo");
  await page.focus("#tab-don");
  await page.keyboard.press("ArrowRight");
  it.pestanasConFlechas = await page.evaluate(() => document.activeElement.id === "tab-ben" && !document.getElementById("panel-ben").hidden && document.getElementById("panel-don").hidden);
  await page.click("[data-loader-demo]");
  await new Promise(r => setTimeout(r, 120));
  it.loaderOcultoAntesDe300ms = await page.evaluate(() => { const l = document.querySelector(".loader-global"); return !l || getComputedStyle(l).display === "none"; });
  await new Promise(r => setTimeout(r, 500));
  it.loaderVisibleDespuesDe300ms = await page.evaluate(() => { const l = document.querySelector(".loader-global"); return !!l && getComputedStyle(l).display !== "none"; });
  await new Promise(r => setTimeout(r, 1300));
  it.loaderSeOculta = await page.evaluate(() => { const l = document.querySelector(".loader-global"); return !l || getComputedStyle(l).display === "none"; });
  await page.evaluate(() => document.querySelectorAll(".toast").forEach(t => t.remove()));
  await page.hover(".ayuda__disparador");
  it.tooltipConHover = await page.$eval("#c-cant-tip", t => !t.hidden);
  await page.mouse.move(5, 5);
  await page.focus(".ayuda__disparador");
  it.tooltipConFoco = await page.$eval("#c-cant-tip", t => !t.hidden);

  // Mapa: marcadores, popup y lista
  await page.goto(WEB + "/mapa", { waitUntil: "networkidle2" });
  it.marcadores = await page.$$eval(".leaflet-marker-icon", m => m.length);
  it.marcadoresConTitulo = await page.$$eval(".leaflet-marker-icon", m => m.every(x => x.getAttribute("title") && x.tabIndex >= 0));
  await page.click(".leaflet-marker-icon");
  await new Promise(r => setTimeout(r, 400));
  it.popupAlClic = await page.$eval(".leaflet-popup-content", p => p.innerText.split("\n")[0]).catch(() => null);
  await page.click(".mapa__lista summary");
  await page.click('.mapa__ir[data-indice="1"]');
  await new Promise(r => setTimeout(r, 600));
  it.popupDesdeLista = await page.$eval(".leaflet-popup-content", p => p.innerText.split("\n")[0]).catch(() => null);
  await page.focus(".leaflet-marker-icon");
  await page.keyboard.press("Enter");
  await new Promise(r => setTimeout(r, 400));
  it.popupConTeclado = await page.$eval(".leaflet-popup-content", p => p.innerText.split("\n")[0]).catch(() => null);

  // Menú móvil
  await page.setViewport({ width: 390, height: 844, isMobile: true, hasTouch: true });
  await page.goto(WEB + "/admin/donaciones", { waitUntil: "networkidle2" });
  it.menuMovilOculto = await page.$eval("#menu-lateral", m => getComputedStyle(m).visibility === "hidden");
  await page.click(".encabezado-privado__alternar");
  await new Promise(r => setTimeout(r, 300));
  it.menuMovilAbre = await page.$eval("#menu-lateral", m => getComputedStyle(m).visibility === "visible");
  it.menuMovilAriaExpanded = await page.$eval(".encabezado-privado__alternar", b => b.getAttribute("aria-expanded"));
  await page.goto(WEB + "/", { waitUntil: "networkidle2" });
  await page.click(".encabezado-publico__alternar");
  it.navPublicaMovilAbre = await page.$eval("#nav-publica", n => getComputedStyle(n).display !== "none");
  // Sin desborde horizontal en móvil
  it.desbordeHorizontal = {};
  for (const u of ["/", "/galeria", "/mapa", "/ayuda", "/legal/privacidad", "/registro", "/admin/donaciones", "/dev/componentes"]) {
    await page.goto(WEB + u, { waitUntil: "networkidle2" });
    it.desbordeHorizontal[u] = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
  }
  await browser.close();
  const archivo = process.env.SALIDA || "revision.json";
  fs.writeFileSync(archivo, JSON.stringify(salida, null, 2));
  // Resumen en consola; el detalle queda en el JSON.
  let violaciones = 0, sinFoco = 0;
  for (const [nombre, r] of Object.entries(salida.paginas)) {
    violaciones += r.axe.length;
    sinFoco += r.foco ? r.foco.sinFoco.length : 0;
    for (const v of r.axe) console.log(`AXE  ${nombre}: ${v.id} (${v.impacto}) ${v.nodos.join(" | ")}`);
    if (r.foco) for (const f of r.foco.sinFoco) console.log(`FOCO ${nombre}: ${f.desc} sin indicador visible`);
    for (const a of r.areasChicas) console.log(`AREA ${nombre}: ${a}`);
  }
  const fallas = Object.entries(salida.interaccion).filter(([k, v]) => v === false || v === null).map(([k]) => k);
  console.log(`\n${Object.keys(salida.paginas).length} páginas x dispositivo · violaciones axe: ${violaciones} · sin foco visible: ${sinFoco} · interacción con fallas: ${fallas.join(", ") || "ninguna"}`);
  console.log(`Detalle en ${archivo}`);
  process.exitCode = violaciones || sinFoco || fallas.length ? 1 : 0;
})().catch(e => { console.error(e); process.exit(1); });
