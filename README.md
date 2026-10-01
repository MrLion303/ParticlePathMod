# Particle Path Mod

Forge 1.20.1 mod for creating persistent particle paths between block positions.

## Seleccionar un camino

Usa un palo:
- Clic izquierdo sobre un bloque: selecciona el punto A y reinicia la selección actual.
- Clic derecho sobre bloques: añade B, C, D, E... en ese orden.
- Puedes hacer tantos puntos como quieras.
- Para reiniciar el recorrido, vuelve a hacer clic izquierdo en otro bloque: ese bloque pasa a ser el nuevo A.

El palo solamente selecciona puntos; no rompe ni interactúa con los bloques.

## Crear un camino

Después de marcar al menos A y B:
/particlepath create <nombre> <partícula>

Ejemplo:
/particlepath create entrada minecraft:flame

### Colores de dust

Para minecraft:dust ya no necesitas convertir el color manualmente a RGB. Puedes escribir un nombre de color o un código hexadecimal.

Ejemplos:
/particlepath create rojo minecraft:dust red
/particlepath create azul minecraft:dust #0080FF
/particlepath create grande minecraft:dust #FF00FF 2

Se aceptan nombres comunes como red, green, blue, yellow, cyan, magenta, purple, orange, pink, white, black, gray, lime y brown, además de navy, teal, violet, light_blue y dark_blue.

Hexadecimal: #RRGGBB, RRGGBB, #RGB o RGB.

La sintaxis original también sigue funcionando:
/particlepath create rojo minecraft:dust 1 0 0 1

## Mostrar / ocultar

/particlepath show <nombre>

Activa la generación continua de partículas.

/particlepath hide <nombre>

Deja de generar nuevas partículas para ese camino.

Esto permite usar los comandos desde el chat, bloques de comandos, funciones, etc.

## Eliminar

/particlepath remove <nombre>

Elimina completamente el camino guardado.

## Listar

/particlepath list

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