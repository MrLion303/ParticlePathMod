# Particle Path Mod

Forge 1.20.1 mod for creating persistent particle paths between block positions.

## Créditos

- Autor: **LAYON**
- Créditos / proyecto: **NegativeStudios**

## Requisitos

- Minecraft 1.20.1
- Minecraft Forge 47.x
- Java 17

## Seleccionar un camino

Usa un palo:
- Clic izquierdo sobre un bloque: selecciona el punto A y reinicia la selección actual.
- Clic derecho sobre bloques: añade B, C, D, E... en ese orden.
- Puedes hacer tantos puntos como quieras.
- Para reiniciar el recorrido, vuelve a hacer clic izquierdo en otro bloque: ese bloque pasa a ser el nuevo A.
- Los mensajes de selección aparecen en **dorado** en el chat/overlay.
- El palo solamente selecciona puntos; no rompe ni interactúa con los bloques.

Ejemplo de mensajes:
- **Punto A seleccionado: 125, 64, -230**
- **Punto B seleccionado: 140, 70, -215**
- **Punto C seleccionado: 155, 68, -190**

## Crear un camino

Después de marcar al menos A y B:

`/particlepath create <nombre> <partícula>`

Ejemplo:

`/particlepath create entrada minecraft:flame`

### Autocompletado de partículas

El argumento de partícula tiene **autocompletado con las partículas registradas actualmente en Minecraft/Forge**, usando identificadores como:

`minecraft:flame`  
`minecraft:smoke`  
`minecraft:heart`  
`minecraft:dust`

Es el mismo tipo de listado de identificadores que puedes consultar al escribir el argumento de partículas del comando vanilla `/particle`.

El autocompletado muestra los IDs de las partículas registradas; después puedes escribir la configuración adicional que corresponda a esa partícula.

### Colores de dust

Para `minecraft:dust` puedes escribir un nombre de color o un código hexadecimal.

Ejemplos:

`/particlepath create rojo minecraft:dust red`  
`/particlepath create azul minecraft:dust #0080FF`  
`/particlepath create grande minecraft:dust #FF00FF 2`

Se aceptan nombres comunes como red, green, blue, yellow, cyan, magenta, purple, orange, pink, white, black, gray, lime y brown, además de navy, teal, violet, light_blue y dark_blue.

Hexadecimal: #RRGGBB, RRGGBB, #RGB o RGB.

La sintaxis original también sigue funcionando:

`/particlepath create rojo minecraft:dust 1 0 0 1`

## Modificar la partícula de un camino

Puedes cambiar la partícula de un camino existente sin tener que volver a marcar sus puntos:

`/particlepath modify <nombre> <partícula>`

Ejemplo:

`/particlepath modify entrada minecraft:flame`

También acepta los colores personalizados de `dust`:

`/particlepath modify entrada minecraft:dust #00FFFF 1.5`

El camino conserva todos sus puntos y únicamente cambia la partícula almacenada.

## Autocompletado de caminos

Los comandos que necesitan un nombre de camino tienen autocompletado con los caminos guardados actualmente:

`/particlepath show <nombre>`  
`/particlepath hide <nombre>`  
`/particlepath modify <nombre> <partícula>`  
`/particlepath remove <nombre>`

Así no necesitas memorizar los nombres de los caminos.

## Mostrar / ocultar

`/particlepath show <nombre>`

Activa la generación continua de partículas.

`/particlepath hide <nombre>`

Deja de generar nuevas partículas para ese camino.

Esto permite usar los comandos desde el chat, bloques de comandos, funciones, etc.

## Eliminar

`/particlepath remove <nombre>`

Elimina completamente el camino guardado.

## Listar

`/particlepath list`

Muestra los caminos existentes.

## Estructura

Cada camino guarda:
- Nombre.
- Partícula.
- Lista ordenada de puntos.
- Estado visible/oculto.

La representación es una polilínea: A→B→C→D→E. Cada segmento se rellena con partículas a intervalos regulares.

## Permisos

Los comandos requieren nivel de permiso **2**.

## Comandos disponibles

`/particlepath create <nombre> <partícula>` — crea un camino con la selección actual.  
`/particlepath modify <nombre> <partícula>` — cambia la partícula de un camino existente.  
`/particlepath show <nombre>` — muestra un camino.  
`/particlepath hide <nombre>` — oculta un camino.  
`/particlepath remove <nombre>` — elimina un camino.  
`/particlepath list` — lista los caminos guardados.
