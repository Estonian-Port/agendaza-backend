document.addEventListener('DOMContentLoaded', function() {
  var calendarEl = document.getElementById('calendar');

  var calendar = new FullCalendar.Calendar(calendarEl, {
    customButtons: {
   	 agregarEvento: {
    	text: 'Nuevo Evento',
    	click: function() {
			window.location = '/saveEvento/0'
    		}
    	},
    agregarUsuario: {
    	text: 'Nuevo usuario',
    	click: function() {
			window.location = '/abmUsuario'
    		}
    	},
    agregarSalon: {
    	text: 'Nuevo Salon',
    	click: function() {
			window.location = '/abmSalon'
    		}
    	},    
    agregarTipoEvento: {
    	text: 'Nuevo Tipo de Evento',
    	click: function() {
			window.location = '/abmTipoEvento'
    		}
    	},
        agregarSubTipoEvento: {
    	text: 'Nuevo Sub Tipo de Evento',
    	click: function() {
			window.location = '/abmSubTipoEvento'
    		}
    	},
    volverSalones: {
    	text: 'Volver Salones',
    	click: function() {
			window.location = '/'
    		}
    	}
  	},
  	locale: 'es',
    initialView: 'dayGridMonth',
    themeSystem: "bootstrap",
    headerToolbar: {
	 left: 'agregarEvento,volverSalones',
     center: 'title',
     right: 'prevYear,prev,next,nextYear today'
    },
    footerToolbar: {
		left: 'agregarUsuario,agregarSalon,agregarTipoEvento,agregarSubTipoEvento'
	}, events: {
        url : '/api/eventos/all'
      }
  });

  calendar.render();
	
});