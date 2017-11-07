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
		assertThat(login.getNumFallos(), is(1)); //No se puede hacer porque numFallos no es visible

	}
	
	/**
	 * Usuario desconocido (excepción)
	 */
	@Test //(expected = ExcepcionUsuarioDesconocido.class)
	public void testUsuarioDesconocidoExcepcion() {
		when(repo.buscar("juan")).thenThrow(ExcepcionUsuarioDesconocido.class);		
		try {
			login.acceder("juan", "1234");
			fail("Se esperaba excepcion propia ExcepcionUsuarioDesconocido.");
		} catch (ExcepcionUsuarioDesconocido e) {
			verify(repo).buscar("juan");
			assertThat(login.getNumFallos(), is(0));
			
		}
	}
	
	/**
	 * Se bloquea la cuenta tras tres intentos fallidos
	 */
	@Test
	public void testCuentaBloqueadaTrasTresIntentosFallidos() {
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		
		login.acceder("pepe", "1235");		
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1235");

		//Pruebo que mi clase despues de tres intentos llama al método 
		//bloquerCuenta() para dejarla bloqueada
		verify(cuenta).bloquearCuenta(); // Esta bloqueada
		verify(cuenta,times(1)).bloquearCuenta(); // Esta bloqueda una vez
		verify(cuenta, atLeastOnce()).bloquearCuenta(); // Esta bloqueada una o más veces
		assertThat(login.getNumFallos(), is(3));
		
		
	}
		
	/*
	 * Se puede acceder tras un fallo
	 */
	@Test
	public void testSePuedeAccederTrasUnFallo() {
		when(cuenta.claveCorrecta("1234")).thenReturn(true);
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1234");
				
		verify(cuenta,times(1)).entrarCuenta(); //verifica que se invoca UNA vez
		verify(cuenta,never()).bloquearCuenta();
		assertThat(login.getNumFallos(), is(1));
	}
	
	/*
	 * Se puede acceder tras dos fallo
	 */
	@Test
	public void testSePuedeAccederTrasDosFallo() {
		when(cuenta.claveCorrecta("1234")).thenReturn(true);
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1234");
				
		verify(cuenta,times(1)).entrarCuenta(); //verifica que se invoca UNA vez
		verify(cuenta,never()).bloquearCuenta();
		assertThat(login.getNumFallos(), is(2));
	}
	
	/**
	 * Se bloquea la cuenta tras cuatro intentos fallidos
	 */
	@Test
	public void testCuentaBloqueadaTrasCuatroIntentosFallidos() {
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		
		login.acceder("pepe", "1235");		
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1235");

		//Pruebo que mi clase despues de tres intentos llama al método 
		//bloquerCuenta() para dejarla bloqueada
		verify(cuenta,times(1)).bloquearCuenta();
		assertThat(login.getNumFallos(), is(4));		
	}
	
	/**
	 * Otros usuarios pueden acceder tras el bloqueo
	 */
	@Test
	public void testOtroUsuarioPuedeAccederTrasElBloqueo() {
		when(cuenta.claveCorrecta("1235")).thenReturn(false);
		ICuenta cuenta2 = mock(ICuenta.class); //Inicializo el objeto Mock
		//Defino a continuación lo que quiero que me de cuanto los solicite
		when(repo.buscar("antonio")).thenReturn(cuenta2);
		when(cuenta2.claveCorrecta("abcd")).thenReturn(true);
		
		login.acceder("pepe", "1235");		
		login.acceder("pepe", "1235");
		login.acceder("pepe", "1235");

		//Pruebo que mi clase despues de tres intentos llama al método 
		//bloquerCuenta() para dejarla bloqueada
		//verify(cuenta,times(1)).bloquearCuenta();
		//assertThat(login.getNumFallos(), is(3));
		
		login.acceder("antonio", "abcd");
		
		verify(cuenta2,times(1)).entrarCuenta(); 
		verify(cuenta2,never()).bloquearCuenta();
		// Lo siguiente es correcto porque el Gestor no pone numFallos a cero
		// hasta que otro usuario no se equivoque al acceder
		assertThat(login.getNumFallos(), is(3));
	}
	
	/*
	 * Se deniega el acceso a las cuentas bloqueadas
	 */
	@Test
	public void testSiEstaBloqueadaSeDeniegaAcceso() {
		when(cuenta.estaBloqueada()).thenReturn(true);
		
		login.acceder("pepe", anyString());
		
		verify(cuenta, never()).entrarCuenta();
		
	}
	
	/*
	 * Se deniega el acceso a las cuentas en uso (excepción)
	 */
	@Test
	public void testSeDeniegaAccesoCuentaEnUso() {
		when(cuenta.claveCorrecta("1234")).thenReturn(true);		
		when(cuenta.estaEnUso()).thenReturn(true);
		
		try {
			login.acceder("pepe", "1234");
			fail("Se esperaba excepcion propia ExcepcionCuentaEnUso().");
		} catch (ExcepcionCuentaEnUso e) {
			assertThat(login.getNumFallos(), is(0));
			
		}
		
	}
}
