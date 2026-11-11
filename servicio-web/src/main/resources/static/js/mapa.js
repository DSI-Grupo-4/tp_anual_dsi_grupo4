/*
 * Componente de mapa de DonaTrack (D-042). Inicializa cada [data-mapa] de la página
 * con Leaflet. Contrato de cada marcador (ver Marcador.java):
 *   { lat, lng, titulo, detalle, actualizadoEn (ISO con zona o null), tipo: "entrega" | "camion" | "sede" }
 * Si el componente tiene data-url y data-intervalo > 0, vuelve a pedir los marcadores
 * cada N segundos (mientras la pestaña está visible) y actualiza mapa y lista.
 */
(function () {
  "use strict";

  var ICONO_POR_TIPO = { entrega: "caja", camion: "camion", sede: "edificio" };

  function formateador(zona) {
    return new Intl.DateTimeFormat("es-AR", {
      timeZone: zona, day: "2-digit", month: "2-digit", year: "numeric",
      hour: "2-digit", minute: "2-digit", hour12: false
    });
  }

  function iniciar(el) {
    if (typeof L === "undefined") return;
    var lienzo = el.querySelector(".mapa__lienzo");
    var estado = el.querySelector(".mapa__estado");
    var lista = el.querySelector(".mapa__items");
    var cantidad = el.querySelector(".mapa__cantidad");
    var vacio = el.querySelector(".mapa__vacio");
    var fechas = formateador(el.dataset.zona || "America/Argentina/Buenos_Aires");
    var centro = [parseFloat(el.dataset.centroLat), parseFloat(el.dataset.centroLng)];
    var zoom = parseInt(el.dataset.zoom, 10) || 11;
    var iconos = el.dataset.iconos;

    lienzo.innerHTML = "";
    // scrollWheelZoom desactivado: la rueda del mouse sigue bajando la página en vez de quedar atrapada en el mapa.
    var mapa = L.map(lienzo, { scrollWheelZoom: false, keyboard: true }).setView(centro, zoom);
    L.tileLayer(el.dataset.tiles, { maxZoom: 19, attribution: el.dataset.atribucion }).addTo(mapa);
    var capa = L.layerGroup().addTo(mapa);
    var enMapa = [];
    // Leaflet pone tabindex en el contenedor: se le da nombre accesible y el foco visible lo da base.css.
    lienzo.setAttribute("role", "region");

    function icono(tipo) {
      var simbolo = ICONO_POR_TIPO[tipo] || "ubicacion";
      return L.divIcon({
        className: "mapa__pin mapa__pin--" + (ICONO_POR_TIPO[tipo] ? tipo : "otro"),
        html: '<svg class="icono" aria-hidden="true" focusable="false"><use href="' + iconos + "#" + simbolo + '"></use></svg>',
        iconSize: [36, 36], iconAnchor: [18, 36], popupAnchor: [0, -32]
      });
    }

    function texto(etiqueta, clase, contenido) {
      var nodo = document.createElement(etiqueta);
      if (clase) nodo.className = clase;
      nodo.textContent = contenido;
      return nodo;
    }

    function fecha(m) {
      return m.actualizadoEn ? "Actualizado: " + fechas.format(new Date(m.actualizadoEn)) : null;
    }

    function popup(m) {
      var caja = document.createElement("div");
      caja.className = "mapa__popup";
      caja.appendChild(texto("p", "mapa__popup-titulo", m.titulo || ""));
      if (m.detalle) caja.appendChild(texto("p", null, m.detalle));
      var f = fecha(m);
      if (f) caja.appendChild(texto("p", "texto-chico texto-secundario", f));
      return caja;
    }

    function irA(indice) {
      var marcador = enMapa[indice];
      if (!marcador) return;
      mapa.setView(marcador.getLatLng(), Math.max(mapa.getZoom(), 14));
      marcador.openPopup();
      lienzo.scrollIntoView({ block: "nearest", behavior: "smooth" });
    }

    function pintarLista(marcadores) {
      lista.innerHTML = "";
      marcadores.forEach(function (m, i) {
        var li = document.createElement("li");
        li.className = "mapa__item";
        var boton = document.createElement("button");
        boton.type = "button";
        boton.className = "boton boton--fantasma mapa__ir";
        boton.setAttribute("data-indice", String(i));
        var pin = document.createElement("span");
        pin.className = "mapa__pin-lista mapa__pin-lista--" + m.tipo;
        pin.setAttribute("aria-hidden", "true");
        boton.appendChild(pin);
        boton.appendChild(texto("span", "mapa__item-titulo", m.titulo || ""));
        li.appendChild(boton);
        if (m.detalle) li.appendChild(texto("span", "mapa__item-detalle", m.detalle));
        var f = fecha(m);
        if (f) li.appendChild(texto("span", "texto-chico texto-secundario", f));
        lista.appendChild(li);
      });
      cantidad.textContent = String(marcadores.length);
      vacio.hidden = marcadores.length > 0;
    }

    function pintar(marcadores, ajustarVista) {
      capa.clearLayers();
      enMapa = marcadores.map(function (m) {
        return L.marker([m.lat, m.lng], { icon: icono(m.tipo), title: m.titulo, alt: m.titulo, keyboard: true, riseOnHover: true })
          .bindPopup(popup(m))
          .addTo(capa);
      });
      if (ajustarVista && enMapa.length) {
        mapa.fitBounds(L.featureGroup(enMapa).getBounds().pad(0.2), { maxZoom: 15 });
      }
      pintarLista(marcadores);
    }

    lista.addEventListener("click", function (e) {
      var boton = e.target.closest(".mapa__ir");
      if (boton) irA(parseInt(boton.getAttribute("data-indice"), 10));
    });

    var datos = [];
    try {
      datos = JSON.parse(el.dataset.marcadores || "[]") || [];
    } catch (ignorado) {
      datos = [];
    }
    pintar(datos, true);

    var url = el.dataset.url;
    var intervalo = parseInt(el.dataset.intervalo, 10) || 0;
    if (url && intervalo > 0) {
      var refrescar = function () {
        if (document.hidden) return;
        fetch(url, { headers: { Accept: "application/json" }, credentials: "same-origin" })
          .then(function (r) {
            if (!r.ok) throw new Error(String(r.status));
            return r.json();
          })
          .then(function (nuevos) {
            pintar(nuevos, false);
            estado.textContent = "Mapa actualizado a las " + fechas.format(new Date()).split(", ")[1] + ".";
          })
          .catch(function () {
            estado.textContent = "No se pudo actualizar el mapa. Se vuelve a intentar en " + intervalo + " segundos.";
          });
      };
      setInterval(refrescar, intervalo * 1000);
    }
  }

  function iniciarTodos() {
    document.querySelectorAll("[data-mapa]").forEach(iniciar);
  }
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", iniciarTodos);
  } else {
    iniciarTodos();
  }
})();
