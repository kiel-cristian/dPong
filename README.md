<h2>Tarea Sistemas Distribuidos 2013-2</h2>

<h3>Configuración</h3>

<h4>Servidor(es)</h4>
<p align="justify">
El juego se puede correr de dos maneras: en un sólo servidor, o en varios servidores que se comunican a través de un balanceador de carga.</p>

<h5>Parámetros:</h5>

<p><b>-n <num>:</b> Número mínimo de jugadores con el que comienza una partida. Por defecto <num> es 2.</p>
<p><b>-b <addr>:</b> Opción para vincular al servidor a un balanceador de carga. Por defecto <addr> es localhost.</p>

<h5>Ejemplos:</h5>
<p>Lanzar un servidor que comienza las partidas con 3 jugadores y que está conectado al balanceador en localhost:<br>
<code>java cl.dcc.cc5303.Server -n 3 -b</code></p>
<p>Lanzar un servidor sin balanceador que comienza las partidas con 2 jugadores:<br>
<code>java cl.dcc.cc5303.Server</code></p>

<h4>Balanceador de carga</h4>
<p>El balanceador no recibe parámetros. Se ejecuta simplemente con:<br>
<code>java cl.dcc.cc5303.LoadBalancer</code></p>

<h4>Clientes</h4>

<h5>Parámetros:</h5>
<p><b>-a <addr>:</b> Dirección del servidor (o balanceador). Si se omite es localhost.</p>
<p><b>-n <num>:</b> ID de servidor, dentro del balanceador, al cual conectarse. Si se omite, el balanceador elige el servidor de menor carga.</p>

<h5>Ejemplo:</h5>
<p>Conectarse al servidor de ID 1, cuyo balanceador está en localhost:<br>
<code>java cl.dcc.cc5303.Client -n 1</code></p>

<h3>Juego</h3>
<h5>Se tomaron las siguientes convenciones:</h5>

<p align="justify"> 1.- El ultimo jugador que toca la pelota es el que gana el punto.</p>
<p align="justify"> 2.- Si nadie toca la pelota y entra en algún arco, no es punto de nadie. </p>
<p align="justify"> 3.- Si en la partida hay más de 2 jugadores jugando, y alguno de estos se sale, la partida continua. Sin embargo si hay dos jugadores jugando y alguno de estos se sale el juego se congela.</p>
<p align="justify"> 4.- Si un jugador pasa más de 3000ms sin actividad es desconectado del servidor.</p>
<p align="justify"> 5.- En cada rebote de la pelota esta aumenta su velocidad.</p>
<p align="justify"> 6.- Cada vez que algún jugador marca un gol el juego se reinicia y el jugador que lo marco, aumenta su marcador en uno.</p>
<p align="justify"> 7.- El juego termina cuando algún jugador mete 10 goles.</p>


<p align="right"> Integrantes : Cristian Carreño<br>
Sergio Maass<br>
Agustin Lopez
</p>
