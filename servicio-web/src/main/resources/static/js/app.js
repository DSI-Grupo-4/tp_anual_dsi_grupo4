/*
 * Comportamiento común de DonaTrack. Sin dependencias: se carga en todas
 * las páginas desde el layout. Las vistas no necesitan JS propio para
 * loaders, toasts, modales, menú móvil, ayudas ni pestañas: alcanza con
 * los atributos data-* documentados en el catálogo (/dev/componentes).
 *
 * API pública para scripts de vistas: window.DonaTrack
 *   DonaTrack.toast(mensaje, tipo)        tipo: "exito" | "error" | "advertencia" | "info"
 *   DonaTrack.conLoader(promesa, texto)   muestra el loader si la promesa tarda > 300 ms
 */
(function () {
  "use strict";

  var DEMORA_LOADER_MS = 300; // consigna: latencia perceptible > 300 ms
  var DURACION_TOAST_MS = 6000;

  // ---------------- Loader global ----------------
  var loader = null;
  var temporizadorLoader = null;
  var pendientes = 0;

  function asegurarLoader() {
    if (loader) return loader;
    loader = document.createElement("div");
    loader.className = "loader-global";
    loader.hidden = true;
    loader.innerHTML =
      '<div class="loader-global__caja" role="status" aria-live="polite">' +
      '<span class="spinner" aria-hidden="true"></span>' +
      '<span class="loader-global__texto">Cargando…</span></div>';
    document.body.appendChild(loader);
    return loader;
  }

  function mostrarLoader(texto) {
    pendientes++;
    if (temporizadorLoader) return;
    temporizadorLoader = setTimeout(function () {
      var el = asegurarLoader();
      el.querySelector(".loader-global__texto").textContent = texto || "Cargando…";
      el.hidden = false;
      document.body.setAttribute("aria-busy", "true");
    }, DEMORA_LOADER_MS);
  }

  function ocultarLoader(forzar) {
    pendientes = forzar ? 0 : Math.max(0, pendientes - 1);
    if (pendientes > 0) return;
    clearTimeout(temporizadorLoader);
    temporizadorLoader = null;
    if (loader) loader.hidden = true;
    document.body.removeAttribute("aria-busy");
  }

  function conLoader(promesa, texto) {
    mostrarLoader(texto);
    return Promise.resolve(promesa).finally(function () {
      ocultarLoader(false);
    });
  }

  // Envío de formularios: loader si tarda y botón ocupado (evita doble envío).
  document.addEventListener("submit", function (evento) {
    var form = evento.target;
    if (evento.defaultPrevented || form.hasAttribute("data-sin-loader")) return;
    var boton = evento.submitter || form.querySelector('[type="submit"]');
    if (boton) {
      boton.setAttribute("aria-busy", "true");
      boton.setAttribute("aria-disabled", "true");
      // Se deshabilita después del submit para no perder el name/value del botón.
      setTimeout(function () { boton.disabled = true; }, 0);
    }
    mostrarLoader(form.getAttribute("data-texto-loader") || "Procesando…");
  });

  // Navegación por enlaces internos: loader si la página siguiente tarda.
  document.addEventListener("click", function (evento) {
    var enlace = evento.target.closest("a[href]");
    if (!enlace || evento.defaultPrevented || evento.button !== 0) return;
    if (evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.altKey) return;
    if (enlace.target && enlace.target !== "_self") return;
    if (enlace.hasAttribute("download") || enlace.hasAttribute("data-sin-loader")) return;
    var destino = new URL(enlace.href, location.href);
    if (destino.origin !== location.origin) return;
    if (destino.pathname === location.pathname && destino.search === location.search && destino.hash) return;
    mostrarLoader();
  });

  // Al volver con "atrás" (bfcache) la página se restaura con el loader visible.
  window.addEventListener("pageshow", function () {
    ocultarLoader(true);
    document.querySelectorAll('[aria-busy="true"]').forEach(function (el) {
      el.removeAttribute("aria-busy");
      el.removeAttribute("aria-disabled");
      el.disabled = false;
    });
  });

  // ---------------- Toasts ----------------
  function contenedorToasts() {
    var cont = document.getElementById("toasts");
    if (!cont) {
      cont = document.createElement("div");
      cont.id = "toasts";
      cont.className = "toasts";
      document.body.appendChild(cont);
    }
    return cont;
  }

  function prepararToast(toast) {
    var cerrar = toast.querySelector("[data-cerrar-toast]");
    if (cerrar) cerrar.addEventListener("click", function () { toast.remove(); });
    // Los errores no se cierran solos: el usuario tiene que poder leerlos.
    if (!toast.classList.contains("toast--error")) {
      setTimeout(function () { toast.remove(); }, DURACION_TOAST_MS);
    }
  }

  function toast(mensaje, tipo) {
    tipo = tipo || "info";
    var el = document.createElement("div");
    el.className = "toast toast--" + tipo;
    el.setAttribute("role", tipo === "error" ? "alert" : "status");
    var texto = document.createElement("span");
    texto.className = "toast__mensaje";
    texto.textContent = mensaje;
    var cerrar = document.createElement("button");
    cerrar.type = "button";
    cerrar.className = "boton boton--fantasma boton--icono";
    cerrar.setAttribute("data-cerrar-toast", "");
    cerrar.setAttribute("aria-label", "Cerrar mensaje");
    cerrar.textContent = "✕";
    el.appendChild(texto);
    el.appendChild(cerrar);
    contenedorToasts().appendChild(el);
    prepararToast(el);
    return el;
  }

  // ---------------- Modales (<dialog>) ----------------
  document.addEventListener("click", function (evento) {
    var abrir = evento.target.closest("[data-abrir-modal]");
    if (abrir) {
      var modal = document.getElementById(abrir.getAttribute("data-abrir-modal"));
      if (modal && typeof modal.showModal === "function") {
        modal._disparador = abrir;
        modal.showModal();
      }
      return;
    }
    var cerrar = evento.target.closest("[data-cerrar-modal]");
    if (cerrar) {
      var dialogo = cerrar.closest("dialog");
      if (dialogo) dialogo.close();
    }
  });
  document.addEventListener("close", function (evento) {
    // Devuelve el foco al botón que abrió el modal (WCAG 2.4.3).
    var dialogo = evento.target;
    if (dialogo._disparador) dialogo._disparador.focus();
  }, true);

  // ---------------- Menú lateral en móvil ----------------
  document.addEventListener("click", function (evento) {
    var boton = evento.target.closest("[data-alternar-menu]");
    if (!boton) return;
    var menu = document.getElementById(boton.getAttribute("aria-controls"));
    var abierto = boton.getAttribute("aria-expanded") === "true";
    boton.setAttribute("aria-expanded", String(!abierto));
    if (menu) menu.classList.toggle("abierto", !abierto);
  });

  // ---------------- Ayudas (tooltip) ----------------
  function alternarAyuda(disparador, mostrar) {
    var texto = document.getElementById(disparador.getAttribute("aria-describedby"));
    if (!texto) return;
    texto.hidden = !mostrar;
    disparador.setAttribute("aria-expanded", String(mostrar));
  }
  document.addEventListener("click", function (evento) {
    var disparador = evento.target.closest(".ayuda__disparador");
    document.querySelectorAll('.ayuda__disparador[aria-expanded="true"]').forEach(function (otro) {
      if (otro !== disparador) alternarAyuda(otro, false);
    });
    if (disparador) alternarAyuda(disparador, disparador.getAttribute("aria-expanded") !== "true");
  });
  document.addEventListener("mouseover", function (evento) {
    var disparador = evento.target.closest(".ayuda__disparador");
    if (disparador) alternarAyuda(disparador, true);
  });
  document.addEventListener("mouseout", function (evento) {
    var disparador = evento.target.closest(".ayuda__disparador");
    if (disparador && disparador !== document.activeElement) alternarAyuda(disparador, false);
  });
  document.addEventListener("focusout", function (evento) {
    if (evento.target.matches && evento.target.matches(".ayuda__disparador")) alternarAyuda(evento.target, false);
  });
  document.addEventListener("keydown", function (evento) {
    if (evento.key !== "Escape") return;
    document.querySelectorAll('.ayuda__disparador[aria-expanded="true"]').forEach(function (d) {
      alternarAyuda(d, false);
    });
  });

  // ---------------- Pestañas (patrón ARIA tabs) ----------------
  function seleccionarPestana(pestana) {
    var lista = pestana.closest('[role="tablist"]');
    lista.querySelectorAll('[role="tab"]').forEach(function (otra) {
      var activa = otra === pestana;
      otra.setAttribute("aria-selected", String(activa));
      otra.tabIndex = activa ? 0 : -1;
      var panel = document.getElementById(otra.getAttribute("aria-controls"));
      if (panel) panel.hidden = !activa;
    });
  }
  document.addEventListener("click", function (evento) {
    var pestana = evento.target.closest('[role="tab"]');
    if (pestana) seleccionarPestana(pestana);
  });
  document.addEventListener("keydown", function (evento) {
    var pestana = evento.target.closest && evento.target.closest('[role="tab"]');
    if (!pestana || (evento.key !== "ArrowRight" && evento.key !== "ArrowLeft")) return;
    var todas = Array.prototype.slice.call(pestana.closest('[role="tablist"]').querySelectorAll('[role="tab"]'));
    var i = todas.indexOf(pestana) + (evento.key === "ArrowRight" ? 1 : -1);
    var siguiente = todas[(i + todas.length) % todas.length];
    seleccionarPestana(siguiente);
    siguiente.focus();
    evento.preventDefault();
  });

  // ---------------- Inicio ----------------
  document.addEventListener("DOMContentLoaded", function () {
    // Los toasts que vienen renderizados del servidor (flash attributes) se
    // vuelven a crear dentro de la región viva para que el lector de pantalla
    // los anuncie; sin JS quedan visibles igual.
    document.querySelectorAll(".toast[data-servidor]").forEach(function (original) {
      var texto = original.querySelector(".toast__mensaje").textContent;
      var tipo = original.getAttribute("data-tipo");
      original.remove();
      toast(texto, tipo);
    });
  });

  window.DonaTrack = { toast: toast, conLoader: conLoader };
})();
