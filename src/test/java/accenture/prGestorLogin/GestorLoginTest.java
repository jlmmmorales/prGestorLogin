package accenture.prGestorLogin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GestorLoginTest {
	GestorLogin login;
	IRepositorioCuentas repo; // collaborator
	ICuenta cuenta; // collaborator

	@Before
	public void setUp() throws Exception {
		repo = mock(IRepositorioCuentas.class); //Inicializo el objetos Mock
		cuenta = mock(ICuenta.class); //Inicializo el objeto Mock
		//Defino a continuación lo que quiero que me de cuanto los solicite
		when(repo.buscar("pepe")).thenReturn(cuenta);
			
		login = new GestorLogin(repo);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAccesoConcedidoALaPrimera() {
		when(cuenta.claveCorrecta("1234")).thenReturn(true);
		
		login.acceder("pepe", "1234");
		
		verify(cuenta,times(1)).entrarCuenta(); //verifica que se invoca UNA vez
		verify(cuenta,never()).bloquearCuenta(); //verificar que no se ha bloqueado la cuenta
	}
	
	/**
	 * Se deniega el acceso la primera vez
	 */
	@Test
	public void testAccesoDenegadoALaPrimera() {
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		
		login.acceder("pepe", "1235");
		
		verify(cuenta,never()).entrarCuenta();
		verify(cuenta,never()).bloquearCuenta();
		//assertThat(login.numFallos, is(1)); //No se puede hacer porque numFallos no es visible

	}
	
	/**
	 * Usuario desconocido (excepción)
	 */
	@Test
	public void testUsuarioDesconocidoExcepcion() {
		when(repo.buscar("juan")).thenThrow(new ExcepcionUsuarioDesconocido());		
	}

}
