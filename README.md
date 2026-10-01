# Particle Path Mod

Forge 1.20.1 mod for creating persistent particle paths between block positions.

## Seleccionar un camino

Usa un palo:

- **Clic izquierdo sobre un bloque:** selecciona el punto **A** y reinicia la selección actual.
- **Clic derecho sobre bloques:** añade **B, C, D, E...** en ese orden.
- Puedes hacer tantos puntos como quieras.
- Para reiniciar el recorrido, vuelve a hacer **clic izquierdo** en otro bloque: ese bloque pasa a ser el nuevo A.

El palo solamente selecciona puntos; no rompe ni interactúa con los bloques.

## Crear un camino

Después de marcar al menos A y B:

`/particlepath create <nombre> <partícula>`

Ejemplo:

`/particlepath create entrada minecraft:flame`

También admite partículas con argumentos, por ejemplo las partículas de polvo:

`/particlepath create rojo minecraft:dust 1 0 0 1`

El camino queda guardado en los datos del mundo.

## Mostrar / ocultar

`/particlepath show <nombre>`

Activa la generación continua de partículas.

`/particlepath hide <nombre>`

Deja de generar nuevas partículas para ese camino.

Ejemplos:

`/particlepath show entrada`

`/particlepath hide entrada`

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

## Requisitos

- Minecraft 1.20.1
- Minecraft Forge 47.x
- Java 17
