(function () {
  'use strict';
  // Confirmación antes de enviar; se ejecuta en captura para que el loader común no se active al cancelar.
  document.addEventListener('submit', function (event) {
    var form=event.target;
    if (!form.dataset.confirmar || form.dataset.confirmado==='true') return;
    event.preventDefault();
    var trigger=event.submitter;
    var dialog=document.createElement('dialog'); dialog.className='admin-confirmacion';
    dialog.setAttribute('aria-labelledby','confirmar-titulo');
    var title=document.createElement('h2'); title.id='confirmar-titulo'; title.textContent='Confirmar acción';
    var description=document.createElement('p'); description.textContent=form.dataset.confirmar;
    var actions=document.createElement('div'); actions.className='admin-acciones';
    var cancel=document.createElement('button'); cancel.type='button'; cancel.className='boton boton--secundario'; cancel.textContent='Cancelar';
    var accept=document.createElement('button'); accept.type='button'; accept.className='boton boton--primario'; accept.textContent='Confirmar';
    actions.append(cancel,accept); dialog.append(title,description,actions); document.body.append(dialog);
    cancel.addEventListener('click',function(){dialog.close();});
    accept.addEventListener('click',function(){form.dataset.confirmado='true'; dialog.close(); form.requestSubmit(trigger);});
    dialog.addEventListener('close',function(){dialog.remove(); if(trigger) trigger.focus();});
    dialog.showModal(); cancel.focus();
  },true);
  function renumerar(list) {
    list.querySelectorAll('[data-fila]').forEach(function(row,i){
      var prefix=list.dataset.lista;
      row.querySelector('legend').textContent=(prefix==='items'?'Bien ':prefix==='contactos'?'Contacto ':'Representante ')+(i+1);
      row.querySelectorAll('[name]').forEach(function(el){
        if(el.name==='preferido') {el.value=String(i); el.id='preferido-'+i; el.closest('label').htmlFor=el.id; return;}
        el.name=el.name.replace(/\[\d+\]/,'['+i+']');
        var old=el.id; var next=el.name.replace(/[^a-zA-Z0-9]/g,'-'); el.id=next;
        row.querySelectorAll('label').forEach(function(label){if(label.htmlFor===old) label.htmlFor=next;});
        el.removeAttribute('aria-describedby'); el.removeAttribute('aria-invalid');
      });
      row.querySelectorAll('.campo__error').forEach(function(e){e.remove();});
      row.querySelector('[data-quitar]').disabled=list.querySelectorAll('[data-fila]').length===1;
    });
  }
  document.addEventListener('click',function(event){
    var add=event.target.closest('[data-agregar]'), remove=event.target.closest('[data-quitar]');
    if(!add&&!remove) return;
    var list=(add||remove).closest('[data-lista]'); var rows=list.querySelector('[data-filas]');
    if(add) {
      var limit=Number(list.dataset.maximo || (list.dataset.lista==='contactos'?20:50));
      if(rows.children.length>=limit) {window.DonaTrack.toast('Alcanzaste el máximo de renglones.','advertencia');return;}
      var row=rows.firstElementChild.cloneNode(true);
      row.querySelectorAll('input,select,textarea').forEach(function(input){if(input.type==='radio') input.checked=false; else input.value=''; input.disabled=false;});
      rows.append(row); renumerar(list); actualizarItem(row); row.querySelector('input,select').focus();
    } else {
      if(rows.children.length<=1) return;
      var preferred=remove.closest('[data-fila]').querySelector('input[type=radio]:checked');
      remove.closest('[data-fila]').remove(); renumerar(list);
      if(preferred) {var first=list.querySelector('input[type=radio]'); if(first) first.checked=true;}
      list.querySelector('[data-agregar]').focus();
    }
  });
  var categorias={SILLA:'MOBILIARIO',MESA:'MOBILIARIO',BANCO:'MOBILIARIO',COLCHON:'MOBILIARIO',FIDEOS_SECOS:'ALIMENTOS',ARROZ:'ALIMENTOS',LEGUMBRES_SECAS:'ALIMENTOS',ACEITE_VEGETAL:'ALIMENTOS',TOMATE:'ALIMENTOS',FRUTA:'ALIMENTOS',CAMPERA:'VESTIMENTA',REMERA:'VESTIMENTA',PANTALON:'VESTIMENTA',ROPA_INFANTIL:'VESTIMENTA',FRAZADA:'VESTIMENTA'};
  function actualizarItem(row) {
    var category=row.querySelector('[data-categoria]'); if(!category) return;
    var sub=row.querySelector('[data-subcategoria]'), unit=row.querySelector('[data-unidad]');
    // Ayuda inmediata; Donaciones sigue siendo la autoridad que valida la combinación.
    sub.setCustomValidity(sub.value&&category.value&&categorias[sub.value]!==category.value?'Esta subcategoría no corresponde a la categoría elegida.':'');
    var food=category.value==='ALIMENTOS', condition=row.querySelector('[data-condicion]'), expiry=row.querySelector('[data-vencimiento]');
    condition.disabled=food; condition.required=!!category.value&&!food; condition.closest('.campo').hidden=food;
    expiry.disabled=!!category.value&&!food; expiry.required=food; expiry.closest('.campo').hidden=!!category.value&&!food;
    var allowed=!sub.value?['UNIDAD','KILOGRAMO','CAJA','PAQUETE','LITRO']:categorias[sub.value]!=='ALIMENTOS'?['UNIDAD']:sub.value==='ACEITE_VEGETAL'?['LITRO','UNIDAD','CAJA']:['UNIDAD','KILOGRAMO','CAJA','PAQUETE'];
    Array.from(unit.options).forEach(function(o){o.disabled=!!o.value&&!allowed.includes(o.value);});
    unit.setCustomValidity(unit.value&&!allowed.includes(unit.value)?'Elegí una unidad compatible con el bien.':'');
    var quantity=row.querySelector('[data-cantidad]'); quantity.step=['KILOGRAMO','LITRO'].includes(unit.value)?'0.001':'1'; quantity.min=quantity.step;
  }
  document.addEventListener('change',function(event){var row=event.target.closest('[data-lista="items"] [data-fila]');if(row) actualizarItem(row);});
  document.querySelectorAll('[data-lista="items"] [data-fila]').forEach(actualizarItem);
  document.querySelectorAll('[data-lista]').forEach(function(list){ if(list.querySelectorAll('[data-fila]').length===1) list.querySelector('[data-quitar]').disabled=true; });
  var errors=document.querySelector('[data-resumen-errores]'); if(errors) errors.focus();
})();
