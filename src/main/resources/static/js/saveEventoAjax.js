$(document).ready(function() {

	$("#cuil").change(function(){

	  $("#nombreCliente").val(null);
	  $("#apellido").val(null);
	  $("#email").val(null);
	  $("#celular").val(null);
	  $("#empresa").val(null);
	  $("#sexo").val(null);

		data = {
			cuil: $("#cuil").val()
		};
		
		$.ajax({
			type: 'GET',
			url: "http://localhost:8080/api/eventos/buscarClientePorCuil",
			data : data,
			contentType: "application/json",
			success : function(cliente) {
				
				if(cliente != ""){
					$("#nombreCliente").val(cliente.nombre);
					$("#apellido").val(cliente.apellido);
					$("#email").val(cliente.email);
					$("#celular").val(cliente.celular);
					$("#empresa").val(cliente.empresa);
					$("#sexo").val(cliente.sexo.id);
					 
					$("#clienteEncontrado").removeClass("d-none");
					$("#clienteNoEncontrado").addClass("d-none");
				}else{
					$("#nombreCliente").val();
					$("#apellido").val();
					$("#email").val();
					$("#celular").val();
					$("#empresa").val();
					$("#sexo").val();
					
					$("#clienteEncontrado").addClass("d-none");
					$("#clienteNoEncontrado").removeClass("d-none");
				}
			}
		});
	});
	
	$("#date").change(function(){
		horarioDisponible();
		listaEventosByDia();
		precioEventoBySubTipoEventoYFecha();
	});

	$("#time_start_hour").change(function(){
		horarioDisponible();
		listaEventosByDia();

	});
	
	$("#time_start_minute").change(function(){
		horarioDisponible();
		listaEventosByDia();
	});
	
	
	$("#time_end_hour").change(function(){
		horarioDisponible();
	});
	
	
	$("#time_end_minute").change(function(){
		horarioDisponible();
	});

	function horarioDisponible(){
		data = {
			fecha: $("#date").val(),
			inicio: $("#time_start_hour").val() + ":" + $("#time_start_minute").val(),
			fin: $("#time_end_hour").val() + ":" + $("#time_end_minute").val(),
			resto24: resto24
		};
		
		$.ajax({
			type: 'GET',
			url: "http://localhost:8080/api/eventos/horarioDisponible",
			data : data,
			contentType: "application/json",
			success : function(response) {
				if(response){
					$("#fechaNoDisponible").addClass("d-none");
					$("#fechaDisponible").removeClass("d-none");
				}else{
					$("#fechaNoDisponible").removeClass("d-none");
					$("#fechaDisponible").addClass("d-none");
				}

			}
		});
	}

	function listaEventosByDia(){
		data = {
			fecha: $("#date").val()
		};

		$.ajax({
			type: 'GET',
			url: "http://localhost:8080/api/eventos/listaEventosByDia",
			data : data,
			contentType: "application/json",
			success : function(listaEvento) {

				// Limpia las fechas que se agregaron anteriormente
				$('#ulEvento').remove();

				if(listaEvento != ""){
					// Obtiene el div donde ira la lista de servicios
					var listaEventoDiv = document.getElementById("listaEvento");
					
					// Crea el ul que contendra a los li
					var ul = document.createElement('ul');
					ul.id = "ulEvento"
			
					listaEvento.forEach(function(fecha) {
						var li = document.createElement("li");
						li.appendChild(document.createTextNode(fecha));
						ul.appendChild(li);
					});
					listaEventoDiv.appendChild(ul);
				}
			}
		});
	}
	
					
	function precioEventoBySubTipoEventoYFecha(){
		data = {
			fecha: $("#date").val(),
			subTipoEventoId: $("#subTipoEvento").val()
		};

		$.ajax({
			type: 'GET',
			url: "http://localhost:8080/api/eventos/precioEventoBySubTipoEventoYFecha",
			data : data,
			contentType: "application/json",
			success : function(precio) {
				$("#presupuesto").val(parseInt(precio))
				presupuesto();
			}
		});
	}

});